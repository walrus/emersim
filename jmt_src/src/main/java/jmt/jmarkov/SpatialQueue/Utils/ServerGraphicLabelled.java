package jmt.jmarkov.SpatialQueue.Utils;
import com.teamdev.jxmaps.LatLng;


/**
 * Created by Dyl on 08/01/2017.
 */
public class ServerGraphicLabelled {

    private LatLng location;
    private String name;

    public ServerGraphicLabelled(LatLng location, String name) {
        this.location = location;
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
