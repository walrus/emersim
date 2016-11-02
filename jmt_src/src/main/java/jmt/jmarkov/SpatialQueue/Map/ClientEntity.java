package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.*;

import javax.swing.*;
import java.util.LinkedList;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.*;

class ClientEntity implements Entity {
    private Polygon polygon;
    private InfoWindow infoWindow;
    private Marker marker;
    private LinkedList<LatLng> path = new LinkedList<>();
    private Polyline areaPeri;

    ClientEntity(MouseEvent mouseEvent) {
        // Add first point to path
        path.add(mouseEvent.latLng());
        LatLng[] pathArray = new LatLng[path.size()];

        areaPeri = new Polyline(map);
        // Creating a polyline options object
        PolylineOptions options = new PolylineOptions();
        // Setting geodesic property value
        options.setGeodesic(true);
        // Setting stroke color value
        options.setStrokeColor("#FF0000");
        // Setting stroke opacity value
        options.setStrokeOpacity(1.0);
        // Setting stroke weight value
        options.setStrokeWeight(2.0);
        // Applying options to the polyline
        areaPeri.setOptions(options);

        // Set current perimeter
        areaPeri.setPath(path.toArray(pathArray));

        marker = new Marker(map);
        marker.setPosition(mouseEvent.latLng());
        marker.addEventListener("click", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                addPointToArea(mouseEvent);
                finaliseArea();
            }
        });
    }

    void addPointToArea(MouseEvent mouseEvent) {
        path.add(mouseEvent.latLng());
        LatLng[] pathArray = new LatLng[path.size()];
        areaPeri.setPath(path.toArray(pathArray));
    }

    private void finaliseArea() {
        if (path.size() < 3) {
            // Error, area must have >= 3 vertices
            // TODO: Display error dialog
        } else {
            areaPeri.setVisible(false);
            polygon = new Polygon(map);
            // Initializing the polygon with the created path
            LatLng[] pathArray = new LatLng[path.size()];
            polygon.setPath(path.toArray(pathArray));
            // Creating a polyline options object
            PolygonOptions options = new PolygonOptions();
            // Setting fill color value
            options.setFillColor("#FF0000");
            // Setting fill opacity value
            options.setFillOpacity(0.35);
            // Setting stroke color value
            options.setStrokeColor("#FF0000");
            // Setting stroke opacity value
            options.setStrokeOpacity(0.8);
            // Setting stroke weight value
            options.setStrokeWeight(2.0);
            // Applying options to the polygon
            polygon.setOptions(options);

            // Creating an information window
            infoWindow = new InfoWindow(map);
            // Putting the address and location to the content of the information window
            infoWindow.setContent("<b>Client region #" + clientRegions.size() + "</b>");

            // Moving the information window to the result location
            infoWindow.setPosition(polygon.getPaths()[0][0]);
            // Showing of the information window
            infoWindow.open(map, polygon.getPaths()[0][0]);
            polygon.addEventListener("click", new MapMouseEvent() {
                @Override
                public void onEvent(MouseEvent mouseEvent) {
                    infoWindow.open(map, polygon.getPaths()[0][0]);
                }
            });
            final Entity entity = this;
            polygon.addEventListener("rightclick", new MapMouseEvent() {
                @Override
                public void onEvent(MouseEvent mouseEvent) {
                    new MapEntityOptionsDialog(entity);
                }
            });

            // Remove first vertex marker
            marker.setVisible(false);
            // Store area for use in simulation
            clientRegions.add(polygon);
            areaBeingDrawn = null;
            drawingInProgess = false;
        }
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
