package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.InfoWindow;
import com.teamdev.jxmaps.MapMouseEvent;
import com.teamdev.jxmaps.Marker;
import com.teamdev.jxmaps.MouseEvent;
import jmt.jmarkov.Queues.MM1Logic;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.map;
import static jmt.jmarkov.SpatialQueue.Map.MapConfig.receiverMarkers;

class ServerEntity implements Entity {
    private Marker marker;
    private InfoWindow infoWindow;
    private MM1Logic ql = new MM1Logic(0.0, 0.0);

    ServerEntity(MouseEvent mouseEvent) {
        // Creating a new marker
        marker = new Marker(map);
        // Move marker to the position where user clicked
        marker.setPosition(mouseEvent.latLng());
        // Creating an information window
        infoWindow = new InfoWindow(map);
        // Putting the address and location to the content of the information window
        infoWindow.setContent("<b>Server #" + receiverMarkers.size() + "</b>");
        // Moving the information window to the result location
        infoWindow.setPosition(marker.getPosition());
        // Showing of the information window
        infoWindow.open(map, marker);
        marker.addEventListener("click", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                infoWindow.open(map, marker);
            }
        });
        final Entity entity = this;
        marker.addEventListener("rightclick", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                new MapEntityOptionsDialog(entity, ql);
            }
        });
        receiverMarkers.add(marker);
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

    @Override
    public String getName() {
        String html = infoWindow.getContent();

        // Splits the html String to get the inner text without formatting
        String[] t = html.split("<b>");
        String[] s = t[1].split("</b>");
        String name = s[0];

        return name;

    }
}
