package jmt.jmarkov.SpatialQueue.Utils;
import com.teamdev.jxmaps.LatLng;

import java.util.LinkedList;

/**
 * Created by Dyl on 08/01/2017.
 */
public class ClientGraphicLabelled {

    private LinkedList<LatLng> path;
    private String name;
    private double lambda;

    public ClientGraphicLabelled(LinkedList<LatLng> path, String name, double lambda) {
        this.path = path;
        this.name = name;
        this.lambda = lambda;
    }

    public LinkedList<LatLng> getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public double getLambda() { return lambda; }
}