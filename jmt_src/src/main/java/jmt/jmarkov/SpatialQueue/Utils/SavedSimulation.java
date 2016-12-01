package jmt.jmarkov.SpatialQueue.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.Charset;

public class SavedSimulation {


    public static String toNewFile(String client, String server) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Spatial Queue Simulator Files (*.sqs)", ".sqs"));

        String fileName;

        int approved = chooser.showSaveDialog(null);
        if (approved == JFileChooser.APPROVE_OPTION) {

            File input = chooser.getSelectedFile();
            String inputString = input.toString();

            // Adds the .sqs extension if not typed in by the user
            if (!inputString.substring(inputString.length() - 4).equals(".sqs")) {
                fileName = (inputString + ".sqs");
            } else {
                fileName = inputString;
            }

            // Writes to the file given by the user
            try {
                FileWriter fw = new FileWriter(fileName);
                fw.write(client + "\n");
                fw.write(server);
                fw.close();
                return fileName;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static void toExistingFile(String fileName, String client, String server) {
        try {
            FileWriter fw = new FileWriter(fileName, false);
            fw.write(client + "\n");
            fw.write(server);
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] fromFile() {
        String[] clientServer = null;
        String client;
        String server;
        String fileName = "";

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Spatial Queue Simulator Files (*.sqs)", ".sqs"));

        int approved = chooser.showOpenDialog(null);
        if (approved == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().toString();
        }

        try (
                InputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
        ) {
            client = br.readLine();
            server = br.readLine();
            clientServer = new String[]{client, server};
            return  clientServer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  clientServer;

    }
}
