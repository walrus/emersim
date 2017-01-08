package jmt.jmarkov.SpatialQueue.Utils;
import com.teamdev.jxmaps.LatLng;

import java.util.LinkedList;

/**
 * Created by Dyl on 08/01/2017.
 */
public class ClientGraphicLabelled {

    private LinkedList<LatLng> path;
    private String name;

    public ClientGraphicLabelled(LinkedList<LatLng> path, String name) {
        this.path = path;
        this.name = name;
    }

    public LinkedList<LatLng> getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}