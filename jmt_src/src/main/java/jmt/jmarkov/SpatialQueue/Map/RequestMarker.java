package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.LatLng;
import com.teamdev.jxmaps.Polyline;
import com.teamdev.jxmaps.PolylineOptions;

class RequestMarker {
    /*
     * Represents a request location marker on the map
     */

    private final Polyline line1;
    private final Polyline line2;
    private final PolylineOptions options;

    RequestMarker(LatLng latLng) {
        double strokeWeight = 5;
        LatLng[] line1Path = new LatLng[2];
        LatLng[] line2Path = new LatLng[2];
        Double x = latLng.getLng();
        Double y = latLng.getLat();
        line1Path[0] = new LatLng(y, x - 0.00005);
        line1Path[1] = new LatLng(y, x + 0.00005);
        line2Path[0] = new LatLng(y + 0.00003, x);
        line2Path[1] = new LatLng(y - 0.00003, x);
        line1 = new Polyline(MapConfig.map);
        line2 = new Polyline(MapConfig.map);
        // Creating a polyline options object
        options = new PolylineOptions();
        // Setting geodesic property value
        options.setGeodesic(true);
        // Setting stroke color value
        options.setStrokeColor("#FF0000");
        // Setting stroke opacity value
        options.setStrokeOpacity(1.0);
        // Setting stroke weight value
        options.setStrokeWeight(strokeWeight);
        // Applying options to the polyline
        line1.setOptions(options);
        line2.setOptions(options);
        // Set current perimeter
        line1.setPath(line1Path);
        line2.setPath(line2Path);
    }

    public void setServingColour() {
        options.setStrokeColor("#0000FF");
        line1.setOptions(options);
        line2.setOptions(options);
    }

    public void setServedColour() {
        options.setStrokeColor("#00FF00");
        line1.setOptions(options);
        line2.setOptions(options);
    }

    public void remove() {
        line1.setVisible(false);
        line2.setVisible(false);
    }
}