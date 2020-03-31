package pulsar_information;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Information implements Cloneable {
    private final String default_scfile = "/data/wwc129/b1937/fermi2/S2/p1937sc00.fits";
    private String file_name; //default is "information.json"
    private double ra;
    private double dec;
    private double tmin;
    private double tmax;
    private double rad;
    private double emin;
    private double emax;
    private String scfile;
    private String events_file;
    private String path_of_addMysource = "./add_mysource.txt";

    public static void main(String[] args) {
        Information i1 = new Information();
        i1.setEmax(100);
        i1.setEmin(50);
        i1.setR(1.232);
        Information i2 = i1.clone();
        System.out.println(i1.emin + " " + i1.emax);
        System.out.println(i2.emin + " " + i2.emax);
        System.out.println("here " + i2.getR());
    }

    public String getDefault_scfile() {
        return default_scfile;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setEvents_file(String events_file) {
        this.events_file = events_file;
    }

    public Boolean getDelete_source_for_tsmap() {
        return delete_source_for_tsmap;
    }

    public void setDelete_source_for_tsmap(Boolean delete_source_for_tsmap) {
        this.delete_source_for_tsmap = delete_source_for_tsmap;
    }

    private Boolean delete_source_for_tsmap = false;
    private double r = 8;
    private double s = 8;
    private String output_dir = "more_fermi";        //default is "more_fermi"

    public Information(String filename) {
        file_name = filename;
        ObjectMapper objectMapper = new ObjectMapper();
        String working_dir = System.getProperty("user.dir");
        String abs_path = Paths.get(working_dir, file_name).toString();
        File temp = new File(abs_path);
        if (!temp.exists()) {
            generate_information_file();
        } else {
            try {
                helper info = objectMapper.readValue(temp, helper.class);
                ra = info.ra;
                dec = info.dec;
                tmin = info.tmin;
                tmax = info.tmax;
                emin = info.emin;
                emax = info.emax;
                rad = info.rad;
                scfile = info.scfile;
                events_file = info.events_file;
                path_of_addMysource = info.getPath_of_addMysource();
                output_dir = info.getOutput_dir();
                delete_source_for_tsmap = info.delete_source_for_tsmap;

                if (delete_source_for_tsmap == null) {
                    delete_source_for_tsmap = false;
                }

                if (path_of_addMysource == null) {
                    path_of_addMysource = "add_mysource.txt";
                }
                r = info.getR();
                s = info.getS();
                if (scfile.equals("")) {
                    scfile = default_scfile;
                }
                if (path_of_addMysource.equals("")) {
                    path_of_addMysource = "./add_mysource.txt";
                }

//                String buf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
//                try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file_name))) {
//                    writer.write(buf + "\n");
//                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(250);
            }
        }
    }

    public Information() {
    }

    public void generate_information_file() {
        ObjectMapper objectMapper = new ObjectMapper();
        String working_dir = System.getProperty("user.dir");
        String abs_path = Paths.get(working_dir, this.file_name).toString();
        File temp = new File(abs_path);

        try {
            System.out.println("you have no \"" + this.file_name + "\" file, so you need to create one...");
            Scanner in = new Scanner(System.in);
            System.out.println("ra: ");
            ra = in.nextDouble();

            System.out.print("dec: ");
            dec = in.nextDouble();

            System.out.print("tmin: ");
            tmin = in.nextDouble();

            System.out.print("tmax: ");
            tmax = in.nextDouble();

            System.out.print("emin: ");
            emin = in.nextDouble();

            System.out.print("emax: ");
            emax = in.nextDouble();

            System.out.print("rad: ");
            rad = in.nextDouble();

            System.out.print("scfile path: (press \"n\" to skip)");
            String tmp = in.nextLine();
            switch (tmp) {
                case "n":
                    scfile = default_scfile;
                    break;
                default:
                    scfile = tmp;
                    break;
            }

            System.out.println("\"add_mysource\" path: (press \"n\" to skip)");
            tmp = in.nextLine();
            switch (tmp) {
                case "n":
                    path_of_addMysource = "add_mysource.txt";
                    break;
                default:
                    path_of_addMysource = tmp;
                    break;
            }

            System.out.println("\"events_file\" path: (press \"n\" to skip)");
            tmp = in.nextLine();
            switch (tmp) {
                case "n":
                    events_file = "add_mysource.txt";
                    break;
                default:
                    events_file = tmp;
                    break;
            }

            System.out.println("\"output directroy\" path: (press \"n\" to skip)");
            tmp = in.nextLine();
            switch (tmp) {
                case "n":
                    output_dir = "add_mysource.txt";
                    break;
                default:
                    output_dir = tmp;
                    break;
            }

            String buf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
                writer.write(buf + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(250);
        }
    }

    public String getFile_name() {
        return file_name;
    }

    public String getOutput_dir() {
        return output_dir;
    }

    public void setOutput_dir(String output_dir) {
        this.output_dir = output_dir;
    }

    public String getPath_of_addMysource() {
        return path_of_addMysource;
    }

    public void setPath_of_addMysource(String path_of_addMysource) {
        this.path_of_addMysource = path_of_addMysource;
    }

    public void generate_default_file(String filename) {
        ObjectMapper objectMapper = new ObjectMapper();
        Information info = Information.getInformation(filename);
        String buf;
        try {
            buf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(info);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file_name))) {
                writer.write(buf + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(250);
        }
    }

    public static Information getInformation(String filename) {
        return new Information(filename);
    }

    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public double getDec() {
        return dec;
    }

    public void setDec(double dec) {
        this.dec = dec;
    }

    public double getTmin() {
        return tmin;
    }

    public void setTmin(double tmin) {
        this.tmin = tmin;
    }

    public double getTmax() {
        return tmax;
    }

    public void setTmax(double tmax) {
        this.tmax = tmax;
    }

    public double getRad() {
        return rad;
    }

    public void setRad(double rad) {
        this.rad = rad;
    }

    public double getEmin() {
        return emin;
    }

    public void setEmin(double emin) {
        this.emin = emin;
    }

    public double getEmax() {
        return emax;
    }

    public void setEmax(double emax) {
        this.emax = emax;
    }

    public String getScfile() {
        return scfile;
    }

    public void setScfile(String scfile) {
        this.scfile = scfile;
    }

    public String getEvents_file() {
        return events_file;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getS() {
        return s;
    }

    public void setS(double s) {
        this.s = s;
    }

    public Information clone() {
        Information another = new Information();
        try {
            another = (Information) super.clone();
            another.setEmin(0);
            another.setEmax(10);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return another;
    }

}


class helper {
    static Information instance;
    final String default_scfile = "/data/wwc129/b1937/fermi2/S2/p1937sc00.fits";
    double ra;
    double dec;
    double tmin;
    double tmax;
    double rad;
    double emin;
    double emax;
    String events_file;
    String scfile;
    String path_of_addMysource;

    Boolean delete_source_for_tsmap = false;
    double r = 10;
    double s = 10;
    String output_dir = "more_fermi";

    public static Information getInstance() {
        return instance;
    }

    public static void setInstance(Information instance) {
        helper.instance = instance;
    }

    public Boolean isDelete_source_for_tsmap() {
        return delete_source_for_tsmap;
    }

    public void setDelete_source_for_tsmap(Boolean delete_source_for_tsmap) {
        this.delete_source_for_tsmap = delete_source_for_tsmap;
    }

    public String getEvents_file() {
        return events_file;
    }

    public void setEvents_file(String events_file) {
        this.events_file = events_file;
    }

    public String getOutput_dir() {
        return output_dir;
    }

    public void setOutput_dir(String output_dir) {
        this.output_dir = output_dir;
    }

    public String getDefault_scfile() {
        return default_scfile;
    }

    public String getPath_of_addMysource() {
        return path_of_addMysource;
    }

    public void setPath_of_addMysource(String path_of_addMysource) {
        this.path_of_addMysource = path_of_addMysource;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getS() {
        return s;
    }

    public void setS(double s) {
        this.s = s;
    }

    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public double getDec() {
        return dec;
    }

    public void setDec(double dec) {
        this.dec = dec;
    }

    public double getTmin() {
        return tmin;
    }

    public void setTmin(double tmin) {
        this.tmin = tmin;
    }

    public double getTmax() {
        return tmax;
    }

    public void setTmax(double tmax) {
        this.tmax = tmax;
    }

    public double getRad() {
        return rad;
    }

    public void setRad(double rad) {
        this.rad = rad;
    }

    public double getEmin() {
        return emin;
    }

    public void setEmin(double emin) {
        this.emin = emin;
    }

    public double getEmax() {
        return emax;
    }

    public void setEmax(double emax) {
        this.emax = emax;
    }

    public String getScfile() {
        return scfile;
    }

    public void setScfile(String scfile) {
        this.scfile = scfile;
    }

}
















