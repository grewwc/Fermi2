package fermi_seperate;

import fermi.File_Information;
import pulsar_information.Information;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.Buffer;
import java.nio.file.Paths;

public class Delete_Source implements File_Information {
    @Override
    public void run() {

    }

    @Override
    public void run(Information info) {
        String pulsar = which_pulsar(info);
        String starName = "";
        if (pulsar != null) {
            switch (pulsar) {
                case "0218":
                    starName = "_3FGLJ0218.1+4233"; //the name can be changed in the later version
                    //so be careful here
                    break;
                case "1937":
                    starName = "J1939+2134";    //我自己取的名字
                    break;
                case "1821":
                    starName = "_3FGLJ1824.6-2451";         //此处的名字小心
                    break;
                default:
                    break;
            }
            if (info.getDelete_source_for_tsmap()) {
                change_xml_file(starName, info);
            }
        }
    }

    private String which_pulsar(Information info) {
        if (info.getScfile().contains("0218")) {
            return "0218";
        }
        if (info.getScfile().contains("1937")) {
            return "1937";
        }
        if (info.getScfile().contains("1821")) {
            return "1821";
        }
        return null;
    }

    private void write_input_xml_file(StringBuilder sb, String xml_file_name) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xml_file_name))) {
            writer.write(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void change_xml_file(String starName, Information info) {
        String line;
        StringBuilder sb = new StringBuilder();
        String input_xml_file_name = Paths.get(info.getOutput_dir(), "input.xml").toString();
        boolean found_source = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(input_xml_file_name))) {
            while ((line = reader.readLine()) != null) {
                if (line.contains(starName)) {
                    found_source = true;
                }

                if (found_source && line.trim().equals("</source>")) {
                    found_source = false;
                    continue;
                }

                if (!found_source) {
                    sb.append(line + "\n");
                }
            }
            write_input_xml_file(sb, input_xml_file_name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}















