package fermi;

import pulsar_information.Information;

public interface File_Information {
     default String information_file_path(){
         return "information.json";
     }

     default String information_file_path(String filename){
         return filename;
     }

     void run();
     void run(Information info);
     default void run(Information info, boolean flux){}
}
