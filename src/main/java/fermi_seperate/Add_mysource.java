package fermi_seperate;

import fermi.File_Information;
import pulsar_information.Information;
import java.io.*;
import java.nio.file.Paths;


@Deprecated
public class Add_mysource implements File_Information {

    private final String default_file_path = "add_mysource.txt";
    private String default_input_xml_path = "./more_fermi/input.xml";

    private final String the_line_to_strip = "</source_library>";

    @Override
    public void run(Information info){
        String file_path = info.getPath_of_addMysource();
        StringBuilder sb_xml = new StringBuilder();
        StringBuilder sb_add = new StringBuilder();
        StringBuilder sb_total = new StringBuilder();

        if(file_path.equals("")){
            file_path = default_file_path;
        }
        default_input_xml_path = Paths.get(info.getOutput_dir(), "input.xml").toString();
        try(BufferedReader reader_xml = new BufferedReader(new FileReader(default_input_xml_path));
            BufferedReader reader_add = new BufferedReader(new FileReader(file_path)) ){
            String line;
            while((line=reader_xml.readLine())!=null){
                if(!line.equals(the_line_to_strip)){
                    sb_xml.append(line+"\n");
                }
            }

            while((line=reader_add.readLine())!=null){
                sb_add.append(line+"\n");
            }

            sb_total = sb_xml.append(sb_add);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(250);
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(default_input_xml_path))){
            writer.write(sb_total.toString());
        }catch(IOException e){
            e.printStackTrace();
            System.exit(250);
        }
    }

    @Override
    public void run(){}
}












