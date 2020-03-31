package fermi;

import org.dom4j.*;
import org.dom4j.io.*;
import pulsar_information.Information;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Information info = new Information();

        changeToPowerLaw(info, "input.xml");
    }

    public static Stars getStarName(String fileName) {
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get(fileName)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(Stars.j0218.getName())) {
                    return Stars.j0218;
                }

                if (line.contains(Stars.j1939.getName())) {
                    return Stars.j1939;
                }

                if (line.contains(Stars.b1821.getName())) {
                    return Stars.b1821;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized void changeToPowerLaw(final Information info,
                                                     String xml) {
        Stars star = Utils.getStarName(xml);
        if (star == null) {
            System.out.println("PSR name is not right");
            System.exit(-1);
        }
        String xmlTemp = xml + "temp";
        final String endSign = "</source>";
        boolean began = false;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(xml));
             PrintWriter writer = new PrintWriter(xmlTemp)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!began) {
                    if (line.contains(star.getName())) {
                        began = true;
                    } else if (line.contains("</source_library>")) {
                    } else {
                        writer.write(line + "\n");
                    }
                } else if (line.contains(endSign)) {
                    began = false;
                }
            }
            generateXML(info, star, writer);
            writer.write("</source_library>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            new File(xml).delete();
            new File(xmlTemp).renameTo(new File(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateXML(
            final Information info, final Stars star, Writer dest) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("source");
        root.addAttribute("name", star.getName());
        root.addAttribute("type", "PointSource");

        Element spectrum = root.addElement("spectrum");
        spectrum.addAttribute("apply_edisp", "true");
        spectrum.addAttribute("type", "PowerLaw");

        Element p1 = spectrum.addElement("parameter");
        p1.addAttribute("free", "1");
        p1.addAttribute("max", "1000.0");
        p1.addAttribute("min", "0.001");
        p1.addAttribute("name", "Prefactor");
        p1.addAttribute("scale", "1e-9");
        p1.addAttribute("value", "1");
        Element p2 = spectrum.addElement("parameter");
        p2.addAttribute("free", "1");
        p2.addAttribute("max", "-1.0");
        p2.addAttribute("min", "-4.0");
        p2.addAttribute("name", "Index");
        p2.addAttribute("scale", "1.0");
        p2.addAttribute("value", "-2.5");
        Element p3 = spectrum.addElement("parameter");
        p3.addAttribute("free", "0");
        p3.addAttribute("max", "2000.0");
        p3.addAttribute("min", "30.0");
        p3.addAttribute("name", "Scale");
        p3.addAttribute("scale", "1.0");
        p3.addAttribute("value", "1000.0");

        Element spatial = root.addElement("spatialModel");
        spatial.addAttribute("type", "SkyDirFunction");

        Element p1_ = spatial.addElement("parameter");
        p1_.addAttribute("free", "0");
        p1_.addAttribute("max", "360.0");
        p1_.addAttribute("min", "-360.0");
        p1_.addAttribute("name", "RA");
        p1_.addAttribute("scale", "1.0");
        p1_.addAttribute("value", String.valueOf(info.getRa()));
        Element p2_ = spatial.addElement("parameter");
        p2_.addAttribute("free", "0");
        p2_.addAttribute("max", "90.0");
        p2_.addAttribute("min", "-90.0");
        p2_.addAttribute("name", "DEC");
        p2_.addAttribute("scale", "1.0");
        p2_.addAttribute("value", String.valueOf(info.getDec()));
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setSuppressDeclaration(true);
            format.setIndentSize(4);
            XMLWriter writer = new XMLWriter(dest, format);
            writer.write(document);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static double parseDouble(String str) {
        Pattern p = Pattern.compile("\\d+\\.\\d*");
        Matcher m = p.matcher(str);
        double res = Double.NaN;
        if (m.find())
            res = Double.parseDouble(m.group());
        return res;
    }

    public static Information getIth(final Information totalInfo,
                                     int total, int ith) {
        double emax = totalInfo.getEmax(), emin = totalInfo.getEmin();
        double h = Math.pow(emax / emin, 1.0 / total);
        double e1, e2;

        e1 = emin * Math.pow(h, ith - 1);
        e2 = emin * Math.pow(h, ith);

        Information res = totalInfo.clone();
        res.setEmin(e1);
        res.setEmax(e2);
        res.setOutput_dir(totalInfo.getOutput_dir() + "/" + ith);
        return res;
    }
}










