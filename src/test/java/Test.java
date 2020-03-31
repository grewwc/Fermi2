import pulsar_information.Information;

import java.io.*;

public class Test {
    public static void main(String[] args) throws Exception{
        FileWriter writer = new FileWriter("test.xml");
        writer.write("good");
        writer.write("next");
        writer.close();
    }

    private static String parseInteger(Information info) {
        String fileName = info.getFile_name();
        int len = fileName.length();
        for (int i = len - 1; i > 0; i--) {
            if (Character.isDigit(fileName.charAt(i))) {
                return String.valueOf(fileName.charAt(i));
            }
        }
        return null;
    }
}



















