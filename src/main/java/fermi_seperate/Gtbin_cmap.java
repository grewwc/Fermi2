package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtbin_cmap implements File_Information {
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

        double binsz = 0.2f;
        int nxpix = (int) (rad * Math.sqrt(2) / binsz);
        int nypix = (int) (rad * Math.sqrt(2) / binsz);

        String evfile = Paths.get(info.getOutput_dir(), "gti.fits").toString();
        String outfile = Paths.get(info.getOutput_dir(), "count_map.fits").toString();
        List<String> args = Arrays.asList("evfile=" + evfile,
                "scfile=" + scfile, "outfile=" + outfile,
                "algorithm=CMAP", "nxpix=" + nxpix, "nypix=" + nypix, "binsz=" + binsz, "coordsys=CEL",
                "xref=" + ra, "yref=" + dec, "axisrot=0", "proj=AIT");

        String output_directory = Paths.get(info.getOutput_dir(), "gtbin_cmap").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtbin_cmap").toString();
        GtProcess gtbin_cmap = GtProcess.newInstance("gtbin", args,
                output_directory, err_directory);
        gtbin_cmap.setLog_file_path(info.getOutput_dir());

        if(!this.isExist(outfile))
            gtbin_cmap.run();
    }


    private boolean isExist(String testFilePath){
        File f = new File(testFilePath);
        return f.exists() && !f.isDirectory();

    }
}
