package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtltcube implements File_Information{
    @Override
    public void run(Information info){

        String scfile = info.getScfile();

        String evfile = Paths.get(info.getOutput_dir(), "gti.fits").toString();
        String outfile = Paths.get(info.getOutput_dir(), "ltcube.fits").toString();
        List<String> args = Arrays.asList("evfile="+evfile, "scfile="+scfile,
                "outfile="+outfile, "dcostheta=0.025", "binsz=1", "zmax=90");

        String output_directory = Paths.get(info.getOutput_dir(), "gtltcube").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtltcube").toString();
        GtProcess gtltcube = GtProcess.newInstance("gtltcube", args,
                output_directory, err_directory);
        gtltcube.setLog_file_path(info.getOutput_dir());

        if(!this.isExist(outfile))
            gtltcube.run();
    }

    @Override
    public void run(){}

    private boolean isExist(String testFilePath){
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();

    }
}
