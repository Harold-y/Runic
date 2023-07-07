package org.hye.runic;

import java.io.*;

public class FileUtil {
    public static String readFile(String fileName){
        String result = "";
        File file = new File(fileName);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null)
            {
                result += line +"\n";
            }
            return result.substring(0, result.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] readFileGetName(String fileName){
        String result = "";
        File file = new File(fileName);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null)
            {
                result += line +"\n";
            }
            return new String[]{result.substring(0, result.length()-1), file.getName()};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile2(String fileName){
        File file = new File(fileName);
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] bs = new byte[(int) file.length()];
            is.read(bs);
            is.close();
            return new String(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
