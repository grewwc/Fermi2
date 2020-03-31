package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtexpcube2 implements File_Information{
    @Override
    public void run(Information info ) {

        double ra = info.getRa();
        double dec = info.getDec();
        double emin = info.getEmin();
        double emax = info.getEmax();

        String infile = Paths.get(info.getOutput_dir(), "ltcube.fits").toString();
        String outfile = Paths.get(info.getOutput_dir(), "expcube.fits").toString();
        List<String> args = Arrays.asList("infile="+infile, "cmap=none",
                "outfile="+outfile, "irfs=P8R2_SOURCE_V6", "nxpix=1800", "nypix=900",
                "binsz=0.2", "coordsys=CEL", "xref="+ra, "yref="+dec, "axisrot=0",
                "proj=AIT", "emin="+emin, "emax="+emax, "enumbins=30");
        String output_directory = Paths.get(info.getOutput_dir(), "expcube").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "expcube").toString();
        GtProcess gtexpcube2 = GtProcess.newInstance("gtexpcube2", args,
                output_directory, err_directory);
        gtexpcube2.setLog_file_path(info.getOutput_dir());
        if(!this.isExist(outfile))
            gtexpcube2.run();
    }

    @Override
    public void run(){}

    private boolean isExist(String testFilePath){
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();

    }
}
