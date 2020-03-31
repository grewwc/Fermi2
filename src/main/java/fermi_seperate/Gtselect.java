package fermi_seperate;

import fermi.GtProcess;
import fermi.File_Information;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class Gtselect implements File_Information {
    @Override
    public void run(Information info) {
        double ra = info.getRa();
        double dec = info.getDec();
        double tmin = info.getTmin();
        double tmax = info.getTmax();
        double rad = info.getRad();
        double emin = info.getEmin();
        double emax = info.getEmax();

        String outfile = Paths.get(info.getOutput_dir(), "filtered.fits").toString();

        List<String> args = Arrays.asList("evclass=128", "evtype=3",
                "infile=Prepare/gti.fits", "outfile=" + outfile,
                "ra=" + ra, "dec=" + dec, "tmin=" + tmin, "tmax=" + tmax, "rad=" + rad, "emin=" + emin,
                "emax=" + emax, "zmax=90");

        String output_directory = Paths.get(info.getOutput_dir(), "gtselect").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtselect").toString();
        GtProcess gtselect = GtProcess.newInstance("gtselect", args,
                output_directory, err_directory);
        gtselect.setLog_file_path(info.getOutput_dir());

        if (!this.isExist(outfile))
            gtselect.run();
    }

    @Override
    public void run() {
    }

    private boolean isExist(String testFilePath) {
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();
    }
}

















