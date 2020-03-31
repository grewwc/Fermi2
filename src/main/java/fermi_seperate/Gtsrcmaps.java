package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtsrcmaps implements File_Information {
    @Override
    public void run() {
    }

    @Override
    public void run(Information info) {
        String scfile = info.getScfile();

        String expcube = Paths.get(info.getOutput_dir(), "ltcube.fits").toString();
        String cmap = Paths.get(info.getOutput_dir(), "ccube.fits").toString();
        String srcmdl = Paths.get(info.getOutput_dir(), "input.xml").toString();
        String bexpmap = Paths.get(info.getOutput_dir(), "expcube.fits").toString();
        String outfile = Paths.get(info.getOutput_dir(), "srcmap.fits").toString();

        List<String> args = Arrays.asList("scfile=" + scfile, "expcube="+expcube,
                "cmap="+cmap, "srcmdl="+srcmdl  , "bexpmap="+bexpmap, "outfile="+outfile, "irfs=CALDB",
                "debug=yes");

        String output_directory = Paths.get(info.getOutput_dir(), "gtsrcmaps").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtsrcmaps").toString();
        GtProcess gtsrcmaps = GtProcess.newInstance("gtsrcmaps", args,
                output_directory, err_directory);
        gtsrcmaps.setLog_file_path(info.getOutput_dir());

        if(!this.isExist(outfile))
            gtsrcmaps.run();

    }

    private boolean isExist(String testFilePath){
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();

    }
}
