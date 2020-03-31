package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtmktime implements File_Information {
    @Override
    public void run() {
    }

    @Override
    public void run(Information info) {

        String scfile = info.getScfile();

        String evfile = Paths.get(info.getOutput_dir(), "filtered.fits").toString();
        String outfile = Paths.get(info.getOutput_dir(), "gti.fits").toString();
        List<String> args = Arrays.asList("scfile=" + scfile,
                "filter=(DATA_QUAL>0)&&(LAT_CONFIG==1)&&(ABS(ROCK_ANGLE)<52)",
                "roicut=no", "evfile=" + evfile, "outfile=" + outfile);

        String output_directory = Paths.get(info.getOutput_dir(), "gtmktime").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtmktime").toString();
        GtProcess gtmktime = GtProcess.newInstance("gtmktime", args,
                output_directory, err_directory);
        gtmktime.setLog_file_path(info.getOutput_dir());

        if (!this.isExist(outfile))
            gtmktime.run();
    }

    private boolean isExist(String testFilePath){
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();

    }
}
