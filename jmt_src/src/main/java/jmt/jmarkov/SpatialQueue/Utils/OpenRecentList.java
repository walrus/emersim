package jmt.jmarkov.SpatialQueue.Utils;

import com.google.gson.Gson;
import com.teamdev.jxmaps.LatLng;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Created by Dyl on 02/12/2016.
 */
public class OpenRecentList {

    private static LinkedList<String> projects = new LinkedList<String>();
    private static Gson gson = new Gson();

    public static void linkedListFromFile() {
        String listJson;
        try (
                InputStream fis = new FileInputStream("openRecentList.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
        ) {
            listJson = br.readLine();
            projects = gson.fromJson(listJson, LinkedList.class);
        } catch (FileNotFoundException e) {
            File f = new File("openRecentList.txt");
            try {
                f.createNewFile();
                updateFile("[]");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void updateLinkedList(String fileName) {
        linkedListFromFile();
        if (projects.contains(fileName)) {
            projects.remove(fileName);
        }
        projects.push(fileName);
        String listJson = gson.toJson(projects);
        updateFile(listJson);
        if (projects.size() >= 10) {
            projects.remove(10);
        }

    }

    public static void resetLinkedList() {
        projects = null;
        String listJson = gson.toJson(projects);
        updateFile(listJson);

    }

    private static void updateFile(String listJson) {
        try {
            FileWriter fw = new FileWriter("openRecentList.txt", false);
            fw.write(listJson);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LinkedList<String> getProjects() {
        return projects;
    }
}
