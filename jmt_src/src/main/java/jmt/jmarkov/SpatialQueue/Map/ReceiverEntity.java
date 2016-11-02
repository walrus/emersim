package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.InfoWindow;
import com.teamdev.jxmaps.Marker;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.receiverMarkers;

class ReceiverEntity implements Entity {
    private Marker marker;
    private InfoWindow infoWindow;

    ReceiverEntity(Marker marker, InfoWindow infoWindow) {
        this.marker = marker;
        this.infoWindow = infoWindow;
    }

    @Override
    public void remove() {
        marker.setVisible(false);
        infoWindow.close();
        receiverMarkers.remove(marker);
    }

    @Override
    public void rename(String newName) {
        infoWindow.setContent("<b>" + newName + "</b>");
    }
}
