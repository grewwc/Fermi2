package fermi_seperate;

import fermi.File_Information;
import fermi.GtProcess;
import pulsar_information.Information;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Gtlike implements File_Information {
    @Override
    public void run() {
    }

    @Override
    public void run(Information info) {
        run_1(info);
        try {
            Thread.sleep(5000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        run_2(info);
    }

    private void run_1(Information info) {
        String scfile = Paths.get(info.getOutput_dir(), "output_tmp.xml").toString();
        String expcube = Paths.get(info.getOutput_dir(), "ltcube.fits").toString();
        String srcmdl = Paths.get(info.getOutput_dir(), "input.xml").toString();
        String cmap = Paths.get(info.getOutput_dir(), "srcmap.fits").toString();
        String bexpmap = Paths.get(info.getOutput_dir(), "expcube.fits").toString();
        String results = Paths.get(info.getOutput_dir(), "gtlike_output_results_tmp.dat").toString();
        String specfile = Paths.get(info.getOutput_dir(), "gtlike_output_counts_spectra_tmp.fits")
                .toString();

        List<String> args = Arrays.asList("sfile=" + scfile,
                "expcube=" + expcube, "srcmdl=" + srcmdl,
                "statistic=BINNED", "optimizer=DRMNFB", "cmap=" + cmap,
                "bexpmap=" + bexpmap, "irfs=CALDB",
                "results=" + results, "specfile=" + specfile);

        String output_directory = Paths.get(info.getOutput_dir(), "gtlike").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtlike").toString();
        GtProcess gtlike1 = GtProcess.newInstance("gtlike", args,
                output_directory, err_directory,
                "gtlike_tmp.out", "gtlike_tmp.err");
        gtlike1.setLog_file_path(info.getOutput_dir());
        gtlike1.run();
    }

    private void run_2(Information info) {
        String scfile = Paths.get(info.getOutput_dir(), "output.xml").toString();
        String expcube = Paths.get(info.getOutput_dir(), "ltcube.fits").toString();
        String srcmdl = Paths.get(info.getOutput_dir(), "output_tmp.xml").toString();
        String cmap = Paths.get(info.getOutput_dir(), "srcmap.fits").toString();
        String bexpmap = Paths.get(info.getOutput_dir(), "expcube.fits").toString();
        String results = Paths.get(info.getOutput_dir(), "gtlike_output_results.dat").toString();
        String specfile = Paths.get(info.getOutput_dir(), "gtlike_output_counts_spectra.fits")
                .toString();
        List<String> args = Arrays.asList("sfile=" + scfile,
                "expcube=" + expcube, "srcmdl=" + srcmdl,
                "statistic=BINNED", "optimizer=NEWMINUIT", "cmap=" + cmap,
                "bexpmap=" + bexpmap, "irfs=CALDB",
                "results=" + results, "specfile=" + specfile);

        String output_directory = Paths.get(info.getOutput_dir(), "gtlike").toString();
        String err_directory = Paths.get(info.getOutput_dir(), "gtlike").toString();
        GtProcess gtlike2 = GtProcess.newInstance("gtlike", args,
                output_directory, err_directory,
                "gtlike.out", "gtlike.err");
        gtlike2.setLog_file_path(info.getOutput_dir());
        gtlike2.run();
    }


}












