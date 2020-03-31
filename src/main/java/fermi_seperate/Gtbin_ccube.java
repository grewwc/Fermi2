package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtbin_ccube implements File_Information {
    @Override
    public void run() {
    }

    @Override
    public void run(Information info) {
        double ra = info.getRa();
        double dec = info.getDec();
        double tmin = info.getTmin();
        double tmax = info.getTmax();
        double rad = info.getRad();
        double emin = info.getEmin();
        double emax = info.getEmax();
        String scfile = info.getScfile();

        double binsz = 0.2;
        int nxpix = (int) (rad * Math.sqrt(2) / binsz);
        int nypix = (int) (rad * Math.sqrt(2) / binsz);

        String evfile = Paths.get(info.getOutput_dir(), "gti.fits").toString();
        String outfile = Paths.get(info.getOutput_dir(), "ccube.fits").toString();
        List<String> args = Arrays.asList("evfile=" + evfile,
                "scfile=" + scfile, "outfile=" + outfile,
                "algorithm=CCUBE", "nxpix=" + nxpix, "nypix=" + nypix, "binsz=" + binsz, "coordsys=CEL",
                "ebinalg=LOG", "emin=" + emin, "emax=" + emax,
                "xref=" + ra, "yref=" + dec, "axisrot=0", "proj=AIT", "enumbins=30");

        String output_directory = Paths.get(info.getOutput_dir(), "gtbin_ccube").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtbin_ccube").toString();
        GtProcess gtbin_ccube = GtProcess.newInstance("gtbin", args,
                output_directory, err_directory);
        gtbin_ccube.setLog_file_path(info.getOutput_dir());

        if (!this.isExist(outfile))
            gtbin_ccube.run();
    }

    private boolean isExist(String testFilePath) {
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();
    }
}














