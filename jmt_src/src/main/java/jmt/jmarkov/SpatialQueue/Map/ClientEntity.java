package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.InfoWindow;
import com.teamdev.jxmaps.Marker;
import com.teamdev.jxmaps.Polygon;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.clientRegions;
import static jmt.jmarkov.SpatialQueue.Map.MapConfig.receiverMarkers;

class ClientEntity implements Entity {
    private Polygon polygon;
    private InfoWindow infoWindow;

    ClientEntity(Polygon polygon, InfoWindow infoWindow) {
        this.polygon = polygon;
        this.infoWindow = infoWindow;
    }

    @Override
    public void remove() {
        polygon.setVisible(false);
        infoWindow.close();
        clientRegions.remove(polygon);
    }

    @Override
    public void rename(String newName) {
        infoWindow.setContent("<b>" + newName + "</b>");
    }
}
