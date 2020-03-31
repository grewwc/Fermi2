package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import fermi.Stars;
import fermi.Utils;
import pulsar_information.Information;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gtmodel implements File_Information {
    @Override
    public void run() {
    }

    @Override
    public void run(Information info, boolean powlaw) {
        final String workDir = System.getProperty("user.dir");
        final String absXmlPath = Paths.get(workDir,
                info.getOutput_dir(), "input.xml").toString();
        run(info);
        Utils.changeToPowerLaw(info, absXmlPath);
    }

    @Override
    public void run(Information info) {
        String workDir = System.getProperty("user.dir");
        String outdir = info.getOutput_dir();
        String r = String.valueOf(info.getR());
        String s = String.valueOf(info.getS());

        //the following 2 files are different source files.
        //
        // 1st is "make3FGLxml.py"
        //
        // 2nd is "makeFL8Yxml.py"
        //
        // usually I use the 1st one, because there is not updated "Extended source files" for
        // this file.
        String makeSource_1 = "make3FGLxml.py";
        String makeSource_2 = "makeFL8Yxml.py";


        String point_source_file_1 = "gll_psc_v16.fit";
        String point_source_file_2 = "gll_psc_8year_v6.fit";

        //in this version, "-v TRUE" has been removed.
        List<String> args = Arrays.asList(makeSource_2,
                point_source_file_2, outdir + "/gti.fits", "-o",
                outdir + "/input.xml", "-G", "gll_iem_v06.fits", "-g", "gll_iem_v06",
                "-I", "iso_P8R2_SOURCE_V6_v06.txt", "-i", "iso_P8R2_SOURCE_V6_v06 ",
                "-e", "Templates/", "-s " + s, "-p", "FALSE", "-r " + r, "-ED", "-ON");

        GtProcess model = GtProcess.newInstance("python", args,
                outdir + "/Gtmodel", outdir + "/Gtmodel",
                "Gtmodel.out", "Gtmodel.err");
        model.setLog_file_path(info.getOutput_dir());

        String input_xml_path = Paths.get(outdir, "input.xml").toString();
        if (!new File(input_xml_path).exists())
            model.run();

        move_roi_file(info);

        final String absXmlPath = Paths.get(workDir,
                info.getOutput_dir(), "input.xml").toString();
        Stars star = Utils.getStarName(input_xml_path);
        freeSource(absXmlPath, star);

        //don't change the scale of the fixed parameters
//        if (scfile.contains("p1937sc00"))
//            change_scale(info);
    }

    private void move_roi_file(Information info) {
        File f = new File(".");
        File[] res = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".reg") && pathname.getName().startsWith("ROI_");
            }
        });

        if (res != null) {
            for (File file : res) {
                try {
                    Files.move(Paths.get(file.getName()), Paths.get(info.getOutput_dir(), file.getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void change_scale(Information info) {
        //freeze the close pulsar
        final double PERCENT = 0.4;
        Pattern p = Pattern.compile("(.*scale=)(\"[-+]?\\d+\\.?\\d*e?[+-]?\\d*)(.*)");
        String outdir = info.getOutput_dir();
        String line;
        List<String> record = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(outdir + "/input.xml"))) {
            while ((line = reader.readLine()) != null) {
                record.add(line);
            }
        } catch (IOException e) {
            System.out.println("wrong when change free");
            e.printStackTrace();
            System.exit(1);
        }

        Matcher res;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outdir + "/input.xml"))) {
            for (String temp : record) {
                if (temp.contains("name=\"Prefactor\"") || temp.contains("name=\"norm\"")) {
                    res = p.matcher(temp);
//                    System.out.println(temp);
                    res.find();
                    String s = res.group(2);
                    s = s.substring(1);
                    Double d = Double.valueOf(s);
                    d *= PERCENT;
                    temp = temp.replace("scale=\"" + s, "scale=\"" + String.format("%.5e", d));
                }
                writer.write(temp + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("wrong when change free");
            System.exit(1);
        }
    }

    /**
     * 如果target source是fixed，就free这个source
     *
     * @param absXmlPath
     */
    public void freeSource(String absXmlPath, Stars star) {
        boolean began = false;
        String noFree = "free=\"0\"";
        String free = "free=\"1\"";

        final String Prefactor = "name=\"Prefactor\"";
        final String Index1 = "name=\"Index1\"";
        final String Expfactor = "name=\"Expfactor\"";
        final String endSignal = "</spectrum>";
        try {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(absXmlPath + "temp"));
                 Scanner in = new Scanner(new FileInputStream(absXmlPath)
                 )
            ) {
                String line;
                while (in.hasNext()) {
                    line = in.nextLine();
                    if (line.contains(star.getName())) {
                        began = true;
                    } else if (began && line.contains(endSignal)) {
                        began = false;
                    } else if (began && line.contains(noFree)) {
                        if (line.contains(Prefactor) || line.contains(Expfactor)
                                || line.contains(Index1)) {
                            line = line.replace(noFree, free);
                        }
                    }
                    out.write(line + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean deleted = new File(absXmlPath).delete();
        if (!deleted) {
            System.out.println("fail to delete 'input.xml'");
            System.exit(-1);
        }
        boolean renamed = new File(absXmlPath + "temp").renameTo(new File(absXmlPath));
        if (!renamed) {
            System.out.println("fail to rename 'input.xml");
            System.exit(-1);
        }
    }
}



















