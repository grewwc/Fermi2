import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import fermi.SubInformation;
import fermi.Utils;
import fermi_seperate.*;
import pulsar_information.Information;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Main {

    @Parameter(names = "-gen", description = "generate defalut \"information.json\" file.")
    private boolean gen = false;
    @Parameter(names = "-help", description = "show command line choices.")
    private boolean help = false;
    @Parameter(names = "-f", description = "choose information file, default is \"information.json\"")
    private String filename = "information.json";
    @Parameter(names = "-continue", description = "use output.xml file as intput.xml file to continue")
    private int continueTimes = 1;
    @Parameter(names = "-force", description = "force to skip the first time to calculate the cmap...")
    private boolean isForced = false;
    @Parameter(names = "-flux", description = "generate flux point, should give number of energy binds " +
            "\nWarning: DON'T use with 'continue' and 'isForced' flag!!!")
    private int energyBins = 1;
    @Parameter(names = "-ul", description = "generate upper limit for flux points")
    private int fluxPoints = 1;
    @Parameter(names = "-pl", description = "use powerlaw without cutoff")
    private boolean powlaw = false;
    @Parameter(names = "-test", description = "test")
    private boolean isTest = false;

    private Logger logger = Logger.getLogger(Main.class.getName());
    public boolean isFirstTime = true;

    public static void main(String... args) {
        Main main = new Main();
        JCommander jCommander = JCommander.newBuilder().addObject(main).build();
        jCommander.parse(args);
        jCommander.setProgramName("Fermi Analysis");
        if (main.isTest) {
            try {
                UpperValue.main(null);
            } catch (Exception e) {
                System.exit(0);
            }
            return;
        }
        Information info = Information.getInformation(main.filename);
        if (main.gen) {
            info.generate_default_file(main.filename);
            return;
        }
        if (main.help) {
            jCommander.usage();
            return;
        }
        final int totalTimes = main.continueTimes;
        final String subDir = "history";
        int originalHistory = main.checkOriginal(info.getOutput_dir(), subDir);
        if (main.fluxPoints != 1) {
            UpperValue ul = new UpperValue();
            ul.run(info, main.fluxPoints);
            return;
        }

        if (main.energyBins == 1) {
            while (main.continueTimes >= 1) {
                main.process(info);
                main.continueTimes--;
                main.isFirstTime = false;
                logHistory(info.getOutput_dir(),
                        String.valueOf(totalTimes - main.continueTimes + originalHistory),
                        subDir);
            }
        } else {
            main.fluxBalanced(info, main.energyBins);
        }

//        Pattern p = Pattern.compile("(.*scale=)(\"\\d+\\.?\\d*e?[+-]?\\d*)(.*)");
//        String raw = "<parameter error=\"0.0003981776919\" free=\"1\" max=\"10\" min=\"0\" name=\"Prefactor\" scale=\"1\" value=\"0.362517799\" />";
//        Matcher m = p.matcher(raw);
//        if(raw.contains("scale=")){
//            m.find();
//            String match = m.group(2);
//            match = match.substring(1);
//            double d = Double.valueOf(match);
//            d *= 0.4;
//            raw = raw.replace("scale=\""+match, "scale=\"" + String.format("%.5e", d));
//            System.out.println(m.group(2));
//            System.out.println(raw);
//        }else{
//            System.out.println("not");
//        }
    }

    private void process(Information info) {
        clean(info);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        try {
            FileHandler handler = new FileHandler(
                    Paths.get(System.getProperty("user.dir"), info.getOutput_dir(),
                            "log", "Main.log").toString());
            handler.setLevel(Level.ALL);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException e) {
            logger.severe(e.getMessage());
            System.exit(250);
        }
        logger.entering(this.getClass().getName(), "process");
        new Copy_information_file().run(info);
        new Gtselect().run(info);
        new Gtmktime().run(info);
        new Gtbin_cmap().run(info);
        new Gtbin_ccube().run(info);

        if (isFirstTime && !isForced) {
            if (energyBins == 1 && !powlaw)
                new Gtmodel().run(info);
            else
                new Gtmodel().run(info, true);
        } else {
            try {
                final Messenger msg = new Messenger("initial");
                Thread t2 = new Thread(new changeOutputToInput(info, msg));
                Thread t1 = new Thread(new cleanWithPython(info, msg));
                t2.start();
                t1.start();
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                logger.severe(e.getMessage());
                e.printStackTrace();
            }
        }
//        new Delete_Source().run(info);
        new Gtltcube().run(info);
        new Gtexpcube2().run(info);
        new Gtsrcmaps().run(info);
        new Gtlike().run(info);
        for (Handler h : logger.getHandlers()) {
            h.close();
        }
        logger.exiting(this.getClass().getName(), "process");
    }

    private static void clean(Information info) {
        String path = System.getProperty("user.dir");
        String absPath = Paths.get(path, info.getOutput_dir(), "log").toString();
        File f = new File(absPath);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    private static class cleanWithPython implements Runnable {
        private Information info;
        private final Messenger msg;

        cleanWithPython(Information info, Messenger msg) {
            this.info = info;
            this.msg = msg;
        }

        @Override
        public void run() {
            String path = System.getProperty("user.dir");
            String nthFile = parseInteger(info);
            String cmd = "python " + Paths.get(path, "clean.py").toString() + " " + nthFile;
            synchronized (msg) {
                try {
                    while (!msg.getMsg().equals("changed to temp")) {
                        msg.wait();
                    }
                    Process p = Runtime.getRuntime().exec(cmd);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            p.getInputStream()
                    ));
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(
                            p.getErrorStream()
                    ));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    do {
                        line = errReader.readLine();
                    } while (line != null);
                    msg.setMsg("cleaned");
                    msg.notifyAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class changeOutputToInput implements Runnable {
        final private Information info;
        private final Messenger msg;

        changeOutputToInput(Information info, Messenger msg) {
            this.info = info;
            this.msg = msg;
        }

        @Override
        public void run() {
            String curDir = System.getProperty("user.dir");
            String outXml = Paths.get(curDir, info.getOutput_dir(), "output.xml").toString();
            String tempInXml = Paths.get(curDir, info.getOutput_dir(), "wwc129").toString();
            String inXml = Paths.get(curDir, info.getOutput_dir(), "input.xml").toString();
            File f = new File(outXml);
            if (!f.exists() && f.isFile()) {
                System.out.println("output.xml not exist in the output directory");
            } else {
                synchronized (msg) {
                    f.renameTo(new File(tempInXml));
                    msg.setMsg("changed to temp");
                    msg.notifyAll();
                }
                synchronized (msg) {
                    try {
                        while (!msg.getMsg().equals("cleaned")) {
                            msg.wait();
                        }
                        File fTemp = new File(tempInXml);
                        fTemp.renameTo(new File(inXml));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private static String parseInteger(Information info) {
        String fileName = info.getFile_name();
        int len = fileName.length();
        for (int i = len - 1; i > 0; i--) {
            if (Character.isDigit(fileName.charAt(i))) {
                return String.valueOf(fileName.charAt(i));
            }
        }
        return null;
    }

    private static class Messenger {
        volatile private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Messenger(String msg) {

            this.msg = msg;
        }
    }

    /**
     * @param outputDir
     * @param data       记录第几次计算, 在"subDirbase"中也会用到
     * @param subDirBase history directory 的相对路径， 和"outputDir" 相加后才是有效路径
     */
    public static void logHistory(String outputDir, String data, String subDirBase) {
        String subDir = Paths.get(subDirBase, "H_" + data).toString();
        File dir = new File(Paths.get(outputDir, subDir).toString());

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String xmlFileSrc = Paths.get(outputDir, "output.xml").toString();
        String xmlFileDest = Paths.get(outputDir, subDir, "output.xml").toString();
        String resultsFileSrc = Paths.get(outputDir, "gtlike_output_results.dat").toString();
        String resultsFileDest = Paths.get(outputDir, subDir, "gtlike_output_results.dat").toString();
        copyFile(xmlFileSrc, xmlFileDest);
        copyFile(resultsFileSrc, resultsFileDest);
    }

    /**
     * @param src  绝对路径
     * @param dest 绝对路径
     */
    static void copyFile(String src, String dest) {

        try (BufferedReader reader = new BufferedReader(new FileReader(src));
             BufferedWriter writer = new BufferedWriter(new FileWriter(dest))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        } catch (Exception e) {

        }
    }


    void fluxBalanced(Information info, int N) {
        ExecutorService service = Executors.newFixedThreadPool(N / 2);
        Information[] infos = new Information[N];
        for (int i = 0; i < N; i++) {
            infos[i] = SubInformation.getIth(info, N, i + 1);
        }
        for (Information e : infos) {
            service.submit(() -> process(e));
        }
        service.shutdown();
        try {
            service.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the integer from file name based on symbol "_"
     *
     * @param outputDir
     * @param subDir
     * @return
     */
    int checkOriginal(String outputDir, String subDir) {
        File f = new File(Paths.get(outputDir, subDir).toString());
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.entering(this.getClass().getName(), "checkOriginal");
        if (!f.exists()) {
            logger.warning("there is no original history file");
            return 0;
        }
        int res = -1;
        File[] files = f.listFiles();
        if (files == null) {
            logger.warning("there is no 'history' directory!");
            return 0;
        }
        for (File file : files) {
            if (!file.isDirectory()) {
                logger.warning("files should be directory!");
                continue;
            }
            int pos = file.getName().indexOf('_');
            if (pos != -1) {
                String subName = file.getName().substring(pos + 1);
                try {
                    int temp = Integer.valueOf(subName);
                    if (temp > res) {
                        res = temp;
                    }
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    return -1;
                }
            }
        }
        logger.exiting(this.getClass().getName(), "checkOriginal");
        return res;
    }

}

































