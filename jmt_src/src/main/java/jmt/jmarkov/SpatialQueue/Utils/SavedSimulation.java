package jmt.jmarkov.SpatialQueue.Utils;

import jmt.jmarkov.SpatialQueue.Map.MapConfig;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by Dyl on 01/12/2016.
 */
public class SavedSimulation {

    private static Integer i = 0;


    public static void toFile(String client, String server) {
        try{
            PrintWriter writer = new PrintWriter(i.toString() + ".sqs", "UTF-8");
            writer.println(client);
            writer.println(server);
            writer.close();
            i++;
        } catch (IOException e) {
            // do something
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
            return  clientServer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  clientServer;

    }
}
