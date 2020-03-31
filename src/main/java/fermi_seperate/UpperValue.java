package fermi_seperate;

import fermi.Stars;
import fermi.Utils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import pulsar_information.Information;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpperValue {
    private final String outputFile = "gtlike_output_results.dat";
    private final String upperLimitDir = "upper_limit";
    private final String workDir = System.getProperty("user.dir");
    private final String UL_outputxml = "UL_output.xml";
    private String realDir;
    private ArrayList<Integer> binNumbers = new ArrayList<>();
    final double THRESH_HOLD = 9.0;


    private String commandEach(int total, int ith, String starName,
                               final Information totalInfo, boolean firstTime) {
        final String absDataDir = Paths.get(realDir, String.valueOf(ith)).toString();
        File f = new File(Paths.get(realDir, upperLimitDir).toString());
        if (!f.exists()) {
            f.mkdir();
        }
        final String absOutputFile = Paths.get(f.getAbsolutePath(),
                String.valueOf(ith) + ".out").toString();
        final String absErrFile = Paths.get(f.getAbsolutePath(),
                String.valueOf(ith) + ".err").toString();
        final String cmap = Paths.get(absDataDir, "srcmap.fits").toString();
        final String expcube = Paths.get(absDataDir, "ltcube.fits").toString();
        final String bexpmap = Paths.get(absDataDir, "expcube.fits").toString();
        final String srcmdl;
        final Information ithInfo = Utils.getIth(totalInfo, total, ith);
        double emin = ithInfo.getEmin();
        double emax = ithInfo.getEmax();

        if (new File(Paths.get(absDataDir, "output.xml").toString()).exists()) {
            srcmdl = Paths.get(absDataDir, "output.xml").toString();
        } else {
            srcmdl = Paths.get(absDataDir, "output_tmp.xml").toString();
        }
        Path srcmdlFinalPath = Paths.get(srcmdl);
        if (firstTime && new File(Paths.get(absDataDir, UL_outputxml).toString()).exists()) {
            new File(Paths.get(absDataDir, UL_outputxml).toString()).delete();
        }
        if (!new File(Paths.get(absDataDir, UL_outputxml).toString()).exists()) {
            try {
                srcmdlFinalPath = Files.copy(Paths.get(srcmdl), Paths.get(absDataDir, UL_outputxml));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        final String upperLimitXml = Paths.get(f.getAbsolutePath(),
                "ul_" + String.valueOf(ith) + ".xml").toString();

        if (!new File(Paths.get(f.getAbsolutePath(),
                "iso_P8R2_SOURCE_V6_v06.txt").toString()).exists()) {
            try {
                Files.copy(Paths.get("/home/wwc129/fermi2/iso_P8R2_SOURCE_V6_v06.txt"),
                        Paths.get(f.getAbsolutePath(), "iso_P8R2_SOURCE_V6_v06.txt"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String script = "" +
                "import sys\n" +
                "sys.stdout = open(\"" + absOutputFile + "\", \"w\")\n" +
                "sys.stderr = open(\"" + absErrFile + "\", \"w\")\n" +
                "from BinnedAnalysis import *\n" +
                "#input_file_belowline \n" +
                "like = binnedAnalysis(cmap=\"" + cmap + "\", irfs=\"P8R2_SOURCE_V6\", expcube=\"" + expcube + "\", bexpmap=\"" + bexpmap + "\", srcmdl= \"" + srcmdlFinalPath.toString() + "\", optimizer=\"NEWMINUIT\")\n" +
                "like.fit(covar=True)\n" +
                "print \"TS:\", like.Ts(\"" + starName + "\"), \"\\n\"\n" +
                "like.logLike.writeXml(\" " + upperLimitXml + "\")\n" +
                "from UpperLimits import UpperLimits\n" +
                "ul = UpperLimits(like)\n" +
                "#input_name_object_ul(import_info_from_xml)\n" +
                "ul[\"" + starName + "\"].compute(emin=" + emin + ", emax=" + emax + ", delta=1.65) \n" +
                "print \"ul:\", ul[\"" + starName + "\"].results\n" +
                "sys.stdout.close()\n" +
                "sys.stderr.close()\n";
        String res = Paths.get(f.getAbsolutePath(), "ul_" + ith + ".py").toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(res))) {
            writer.write(script);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private ArrayList<ProcessBuilder> getAllPBs(int bins, String starName,
                                                final Information info, boolean firstTime) {
        ArrayList<ProcessBuilder> pbs = new ArrayList<>();
        outer:
        for (int i = 1; i <= bins; i++) {
            final String temp = Paths.get(realDir, String.valueOf(i),
                    outputFile).toString();
            if (!new File(temp).exists()) {
                String command = commandEach(bins, i, starName, info, firstTime);
                pbs.add(new ProcessBuilder("python", command));
                binNumbers.add(i);
            } else {
                Path tempFile = Paths.get(temp);
                try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
                    String line;
                    boolean began = false;
                    while ((line = reader.readLine()) != null) {
                        if (!began && line.contains(starName)) {
                            began = true;
                        } else if (began && line.contains("TS value")) {
                            double tsValue = Utils.parseDouble(line);
                            if (tsValue == Double.NaN) {
                                System.out.println("tsvalue parsing error");
                                System.exit(-1);
                            }
                            if (tsValue < THRESH_HOLD) {
                                String command = commandEach(bins, i, starName, info, firstTime);
                                pbs.add(new ProcessBuilder("python", command));
                                binNumbers.add(i);
                                continue outer;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pbs;
    }

    /**
     * runOnce should always be the first function to be called
     *
     * @param info
     * @param bins
     */
    private void runOnce(final Information info, int bins, boolean firstTime) {
        final String baseDir = info.getOutput_dir();
        realDir = Paths.get(workDir, baseDir).toString();
        final String fileName = Paths.get(realDir, "input.xml").toString();
        Stars starName = Utils.getStarName(fileName);
        if (starName == null) {
            System.out.println("don't find the target star");
            System.exit(-1);
        }

        ArrayList<ProcessBuilder> pbs = getAllPBs(bins, starName.getName(), info, firstTime);
        for (int i = 0; i < pbs.size(); i++) {
            new runEachProcessBuilder(pbs.get(i)).run();
        }
//        try {
//            service.wait();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void run(final Information info, int bins) {
        //fist run with full free sources
        runOnce(info, bins, true);


        //second run with high TS value sources
        for (int ith : binNumbers) {
            if (!isConverged(ith)) {
                freezeParametersWithLowTS(ith, false);
            }
        }

        runOnce(info, bins, false);

        //third run with only target source
        for (int ith : binNumbers) {
            if (!isConverged(ith)) {
                freezeParametersWithLowTS(ith, true);
            }
        }
        System.out.println("the program reaches this line");
        runOnce(info, bins, false);
    }

    private synchronized void freezeParametersWithLowTS(int ith, boolean onlyTarget) {
        Pattern p = Pattern.compile("\"(.*?)\"");
        List<String> allStars = Arrays.asList(Stars.j0218.getName(),
                Stars.b1821.getName(), Stars.j1939.getName());

        Path absUL_outputxml = Paths.get(realDir, String.valueOf(ith), UL_outputxml);
        Path absUL_outputxmlTemp = Paths.get(absUL_outputxml.toString() + "temp");

        String line;
        final String endSign = "</source>";
        final String free = "free=\"1\"";
        final String fix = "free=\"0\"";
        List<String> lowTS = getSourceNameWithLowTS(ith);
        List<String> allSource = getAllSourceNames();
        boolean changePeriod = false;
        try (BufferedReader xmlReader = Files.newBufferedReader(absUL_outputxml);
             BufferedWriter xmlWriter = Files.newBufferedWriter(absUL_outputxmlTemp)) {
            while ((line = xmlReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    String sourceName = m.group(1);
                    if (!onlyTarget) {
                        if (lowTS.contains(sourceName) && !allStars.contains(sourceName)) {
                            changePeriod = true;
                        }
                    } else {
                        if (allSource.contains(sourceName) && !allStars.contains(sourceName)) {
                            changePeriod = true;
                        }
                    }
                }

                if (changePeriod) {
                    line = line.replace(free, fix);
                }

                if (line.contains(endSign)) {
                    changePeriod = false;
                }

                xmlWriter.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new File(absUL_outputxml.toString()).delete();
        new File(absUL_outputxmlTemp.toString()).renameTo(
                new File(absUL_outputxml.toString()));
    }

//    private void freezeParametersWithLowTS(String path) {
//        Pattern p = Pattern.compile("\"(.*?)\"");
//        Path absUL_outputxml = Paths.get(path);
//        Path absUL_outputxmlTemp = Paths.get(path + "temp");
//
//        String line;
//        final String endSign = "</source>";
//        final String free = "free=\"1\"";
//        final String fix = "free=\"0\"";
//
//        List<String> lowTS = getSourceNameWithLowTS("gtlike_output_results.dat");
//        boolean changePeriod = false;
//        for (String name : lowTS) {
//            System.out.println(name);
//        }
//        try (BufferedReader xmlReader = Files.newBufferedReader(absUL_outputxml);
//             BufferedWriter xmlWriter = Files.newBufferedWriter(absUL_outputxmlTemp)) {
//            while ((line = xmlReader.readLine()) != null) {
//                Matcher m = p.matcher(line);
//                if (m.find() && line.contains("<source ")) {
//                    String sourceName = m.group(1);
//                    if (lowTS.contains(sourceName)) {
//                        changePeriod = true;
//                    }
//                }
//
//                if (changePeriod) {
//                    line = line.replace(free, fix);
//                    System.out.println("changed");
//                }
//
//                if (line.contains(endSign)) {
//                    changePeriod = false;
//                }
//
//                xmlWriter.write(line + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
////        new File(absUL_outputxml.toString()).delete();
////        new File(absUL_outputxmlTemp.toString()).renameTo(
////                new File(absUL_outputxml.toString()));
//    }

//    private List<String> getSourceNameWithLowTS(String absGtlikeOutput) {
//
//        List<String> res = new ArrayList<>();
//        List<String> names = getAllSourceNames("input.xml");
//        String line;
//        final String TS = "TS value";
//        int i = 0;
//        String starNow = null;
//        try (BufferedReader reader = Files.newBufferedReader(Paths.get(absGtlikeOutput))) {
//            while ((line = reader.readLine()) != null) {
//                if (line.contains(names.get(i))) {
//                    starNow = names.get(i++);
//                } else if (line.contains(TS)) {
//                    double tsvalue = Utils.parseDouble(line);
//                    if (tsvalue == Double.NaN) {
//                        System.out.println("parse double error");
//                        System.exit(-1);
//                    }
//                    if (tsvalue < THRESH_HOLD && starNow != null) {
//                        res.add(starNow);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return res;
//    }


    private List<String> getSourceNameWithLowTS(int ith) {
        Path absGtlikeOutput = Paths.get(realDir, String.valueOf(ith), outputFile);
        List<String> res = new ArrayList<>();
        List<String> names = getAllSourceNames();
        String line;
        final String TS = "TS value";
        int i = 0;
        String starNow = null;
        try (BufferedReader reader = Files.newBufferedReader(absGtlikeOutput)) {
            while ((line = reader.readLine()) != null && i < names.size()) {
                if (line.contains(names.get(i))) {
                    starNow = names.get(i++);
                } else if (line.contains(TS)) {
                    double tsvalue = Utils.parseDouble(line);
                    if (tsvalue == Double.NaN) {
                        System.out.println("parse double error");
                        System.exit(-1);
                    }
                    if (tsvalue < THRESH_HOLD && starNow != null) {
                        res.add(starNow);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

//    @SuppressWarnings("unchecked")
//    private static List<String> getAllSourceNames(String path) {
//        List<String> res = new ArrayList<>();
//        SAXReader reader = new SAXReader();
//        Document document = null;
//        try {
//            document = reader.read(path);
//        } catch (DocumentException e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
//        List<Node> nodes = document.selectNodes("//source_library/source");
//        for (Node node : nodes) {
//            Element ele = (Element) node;
//            res.add(ele.attributeValue("name"));
//        }
//        return res;
//    }

    @SuppressWarnings("unchecked")
    private List<String> getAllSourceNames() {
        final String path = Paths.get(realDir, "output.xml").toString();
        List<String> res = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(path);
        } catch (DocumentException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        List<Node> nodes = document.selectNodes("//source_library/source");
        for (Node node : nodes) {
            Element ele = (Element) node;
            res.add(ele.attributeValue("name"));
        }
        return res;
    }

    private boolean isConverged(int ith) {
        if (realDir == null) {
            System.out.println("you don't assign the 'realDir'.");
            System.exit(-1);
        }
        final String outFile = Paths.get(realDir, upperLimitDir,
                String.valueOf(ith) + ".out").toString();
        File out = new File(outFile);
        if (!out.exists()) {
            System.out.println("have no original output file");
            System.exit(-1);
        }

        int numOfChars = 0;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(outFile))) {
            while (reader.read() != -1)
                numOfChars++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(numOfChars);
        return numOfChars > 5;
    }

    private void copyOutputXML(final Information info, int ith) {
        String srcmdl;
        final String absDataDir = Paths.get(realDir, String.valueOf(ith))
                .toString();
        if (new File(Paths.get(absDataDir, "output.xml").toString()).exists()) {
            srcmdl = Paths.get(absDataDir, "output.xml").toString();
        } else {
            srcmdl = Paths.get(absDataDir, "output_tmp.xml").toString();
        }
        if (!new File(Paths.get(absDataDir, UL_outputxml).toString()).exists()) {
            try {
                Files.copy(Paths.get(srcmdl), Paths.get(absDataDir, UL_outputxml));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
    }
}

class runEachProcessBuilder implements Runnable {
    private ProcessBuilder pb;

    runEachProcessBuilder(ProcessBuilder pb) {
        this.pb = pb;
    }

    @Override
    public void run() {
        pb.inheritIO();
        try {
            final Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}








