package fermi_seperate;


import fermi.File_Information;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.*;

public class Copy_information_file implements File_Information {
    @Override
    public void run() {
    }

    @Override
    public void run(Information info) {
        File src = new File(info.getFile_name());
        File dist = new File((Paths.get(info.getOutput_dir(), src.toString())).toString());
        try {
            Files.copy(src.toPath(), dist.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
        }
    }

}
