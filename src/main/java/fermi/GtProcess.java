/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fermi;


import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * This class is the basic of all fermi analysis.
 * It can run "gtselect, gtmktime, gtbin, gtmodel, gtltcube, gtexpcube2, gtsrcmaps, gtlike" and so on.
 * It has 3 constructors in it.
 *
 * @author grewwc
 */
public class GtProcess {

    private static Logger logger = Logger.getLogger(GtProcess.class.getName());
    private String log_file_path = ".";
    private String output_directory;
    private String err_directory;
    private String output_file;
    private String err_file;
    private String command = "";
    private List<String> Args = null;
    private OutputStream outputstream;
    private OutputStream errstream;
    private boolean out_dir_is_set = false;
    private boolean err_dir_is_set = false;

    public GtProcess(String command, List<String> Args, String output_directory,
                     String err_directory, String out_file, String err_file) {
        this(command, Args, output_directory, err_directory);
        setErr_file(err_file);
        setOutput_file(out_file);

    }

    //overloaded constructor.
    public GtProcess(String command, List<String> Args, String output_directory,
                     String err_directory) {
        this(command, Args);
        this.setErr_directory(err_directory);
        this.setOutput_directory(output_directory);
        setErr_file();
        setOutput_file();
    }

    //constructor
    public GtProcess(String command, List<String> Args) {
        this.command = command;
        this.Args = Args;
    }

    public void setErr_file() {

        if (this.err_dir_is_set) {
            this.err_file = Paths.get(err_directory, this.command + ".err").toString();
        } else {
            System.out.println("set err dir fiirst!!");
            return;
        }

    }

    public void setOutput_file() {
        if (out_dir_is_set) {
            this.output_file = Paths.get(this.getOutput_directory(), this.command + ".out").toString();
        } else {
            System.out.println("set output dir first!!");
            return;
        }
    }

    public String getOutput_directory() {
        return output_directory;
    }

    public void setOutput_directory(String output_directory) {
        File temp = new File(output_directory);
        if (!temp.isAbsolute()) {
            String working_dir = System.getProperty("user.dir");
            this.output_directory = Paths.get(working_dir, output_directory).toString();
            out_dir_is_set = helper.create_directory(output_directory);
            return;
        }
        out_dir_is_set = true;
        this.output_directory = output_directory;
    }

    /**
     * @param command Command line program, e.g.: "gtlike".
     * @param Args    Arguments of the command line program.
     * @return Singleton, returns an instance of "GtProcess"
     */
    public static GtProcess newInstance(String command, List<String> Args) {
        return new GtProcess(command, Args);
    }

    /**
     * @param command          Command line program, e.g.: "gtlike".
     * @param Args             Arguments of the command line program.
     * @param output_directory Redirect the "stdout" to a file in this directory.
     * @param err_directory    Redirect the "stderr" to a file in this directory.
     * @param out_file         File name of redirected stdout. e.g.: for "gtselect", the default value is
     *                         "gtselect.out". Can be ignored.
     * @param err_file         File name of redirected stderr. e.g.: for "gtselect", the default value is
     *                         "gtselect.err". Can be ignored.
     * @return Singleton, returns an instance of "GtProcess"
     */
    public static GtProcess newInstance(String command, List<String> Args, String output_directory,
                                        String err_directory, String out_file, String err_file) {
        return new GtProcess(command, Args, output_directory, err_directory,
                out_file, err_file);
    }

    public static void main(String[] args) {
        GtProcess p = GtProcess.newInstance("ls", Arrays.asList("*"), ".", ".");
        p.setLog_file_path("/Users/grewwc");
        p.run();
        System.out.println(p.getOutput_directory());
    }

    /**
     * @param command          Command line program, e.g.: "gtlike".
     * @param Args             Arguments of the command line program.
     * @param output_directory Redirect the "stdout" to a file in this directory.
     * @param err_directory    Redirect the "stderr" to a file in this directory.
     * @return Singleton, returns an instance of "GtProcess"
     */
    public static GtProcess newInstance(String command, List<String> Args, String output_directory,
                                        String err_directory) {
        return new GtProcess(command, Args, output_directory, err_directory);
    }

    public void setLog_file_path(String log_file_path) {
        this.log_file_path = log_file_path;
    }

    public void run() {
        logger.entering(getClass().getName(), "run");
        List<String> parameters = new ArrayList<>();
        if (Args == null) {
            parameters.add(this.getCommand());
        } else {
            parameters.add(command);
            parameters.addAll(Args);
        }
        ProcessBuilder pb = new ProcessBuilder(parameters);
        Thread t = new Thread(new Redirect(getErr_file(), getOutput_file(), pb));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            logger.severe(e.getMessage() + " in file: 'GtProcess', line: '163'. ");
            System.exit(-1);
        }

        for (Handler h : logger.getHandlers()) {
            h.close();
        }
        logger.exiting(getClass().getName(), "run");

    }

    public String getCommand() {
        return this.command;
    }

    public String getErr_file() {
        return err_file;
    }

    //setter && getter
    public String getOutput_file() {
        return output_file;
    }

    public void setOutput_file(String output_file) {
        if (out_dir_is_set) {
            this.output_file = Paths.get(this.getOutput_directory(), output_file).toString();

        } else {
            System.out.println("set output dir first!!");
        }
    }

    public void setErr_file(String err_file) {
        if (this.err_dir_is_set) {
            this.err_file = Paths.get(err_directory, err_file).toString();
        } else {
            System.out.println("set err dir fiirst!!");
        }

    }

    public String getErr_directory() {
        return err_directory;
    }

    public void setErr_directory(String err_directory) {
        File temp = new File(err_directory);
        if (!temp.isAbsolute()) {
            String working_dir = System.getProperty("user.dir");
            this.err_directory = Paths.get(working_dir, err_directory).toString();
            err_dir_is_set = GtProcess.helper.create_directory(err_directory);
            return;
        }
        this.err_directory = err_directory;
        err_dir_is_set = GtProcess.helper.create_directory(err_directory);
    }

    private static class helper {
        private static boolean create_directory(String path) {
            File temp = new File(path);
            if (!temp.exists()) {
                return temp.mkdirs();
            }
            System.out.println("directory: " + path + " exists.");
            return true;
        }
    }
}


class Redirect implements Runnable {

    private String err_path;
    private String out_path;
    private ProcessBuilder pb;

    public Redirect(String err_path, String out_path, ProcessBuilder pb) {
        this.err_path = err_path;
        this.out_path = out_path;
        this.pb = pb;
    }

    @Override
    public void run() {
        if (pb == null) {
            System.exit(250);
        }

        File err = new File(err_path);
        File out = new File(out_path);
        pb.redirectOutput(out);
        pb.redirectError(err);

        try {
            Process p = pb.start();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (BufferedReader read_out = new BufferedReader(new InputStreamReader(p.getInputStream()));
                         BufferedReader read_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                         BufferedWriter write_out = new BufferedWriter(new FileWriter(out));
                         BufferedWriter write_err = new BufferedWriter(new FileWriter(err))) {

                        StringBuilder line = new StringBuilder();
                        int ch;
                        while ((ch = read_out.read()) != -1) {
                            line.append((char) ch);
                            if ((char) ch == '\n') {
                                line.deleteCharAt(line.length() - 1);
                                write_out.write(line.toString() + "\n");
                                line.setLength(0);
                            }
                        }

                        while ((ch = read_err.read()) != -1) {
                            line.append((char) ch);
                            if ((char) ch == '\n') {
                                line.deleteCharAt(line.length() - 1);
                                write_err.write(line.toString() + "\n");
                                line.setLength(0);
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            p.waitFor();
            t.join();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}


























