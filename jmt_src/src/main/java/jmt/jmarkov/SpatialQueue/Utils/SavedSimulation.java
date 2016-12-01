package jmt.jmarkov.SpatialQueue.Utils;

import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import org.fest.assertions.Fail;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by Dyl on 01/12/2016.
 */
public class SavedSimulation {


    public static void toNewFile(String client, String server) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Spatial Queue Simulator Files (*.sqs)", ".sqs"));

        String fileName;
        boolean success;

        int approved = chooser.showSaveDialog(null);
        if (approved == JFileChooser.APPROVE_OPTION) {

            File input = chooser.getSelectedFile();
            String inputString = input.toString();

//            // Adds the .sqs extension if not typed in by the user
//            if (inputString.substring(inputString.length() - 4).equals(".sqs")) {
//                fileName = inputString.substring(0, inputString.length() - 4);
//            } else {
//                fileName = inputString;
//            }

            // Writes to the file given by the user
            try {
                FileWriter fw = new FileWriter(inputString);
                fw.write(client + "\n");
                fw.write(server);
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static String[] fromFile(String fileName) {
        String[] clientServer = null;
        String client;
        String server;
        try (
                InputStream fis = new FileInputStream("0.sqs");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
        ) {
            client = br.readLine();
            server = br.readLine();
            clientServer = new String[]{client, server};

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  clientServer;

    }
}
