package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.*;
import jmt.jmarkov.SpatialQueue.Simulation.Server;
import jmt.jmarkov.SpatialQueue.Utils.Location;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.map;
import static jmt.jmarkov.SpatialQueue.Map.MapConfig.serverGraphics;

class ServerGraphic implements Entity {
    private Marker marker;
    private InfoWindow infoWindow;
    private Server server;

    ServerGraphic(MapConfig mapConfig, LatLng latLng) {
        // Creating a new marker
        marker = new Marker(map);
        // Move marker to the position where user clicked
        marker.setPosition(latLng);
        // Creating an information window
        infoWindow = new InfoWindow(map);
        // Putting the address and location to the content of the information window
        infoWindow.setContent("<b>Server #" + serverGraphics.size() + "</b>");
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
                new RenameEntityFrame(entity);
            }
        });
        this.server = new Server(mapConfig, new Location(latLng));
        serverGraphics.add(this);
    }

    LatLng getPosition() {
        return marker.getPosition();
    }

    @Override
    public void remove() {
        marker.setVisible(false);
        infoWindow.close();
        serverGraphics.remove(this);
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
        return s[0];
    }

    public Server getServer() {
        return server;
    }
}
