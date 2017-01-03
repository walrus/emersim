package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.Polygon;
import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Simulation.ClientRegion;
import jmt.jmarkov.SpatialQueue.Utils.Location;

import java.util.HashMap;
import java.util.LinkedList;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.*;

public class ClientGraphic implements Graphic {

    /*
    * Represents the client region graphic on the map and provides methods to
    * manage these regions
    */

    private Polygon polygon;
    private InfoWindow infoWindow;
    private Marker marker;
    private LinkedList<LatLng> path = new LinkedList<>();
    private Polyline areaPeri;
    private GuiComponents guiComponents;
    private ClientRegion client;
    private HashMap<Location, RequestMarker> requestMarkers = new HashMap<>();

    // Create graphic manually using clicks on map
    ClientGraphic(LatLng latLng, GuiComponents guiComponents) {
        this.guiComponents = guiComponents;
        // Create perimeter line
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
        // Add vertex to path
        addVertexToArea(latLng);
        // Place close area marker
        marker = new Marker(map);
        marker.setPosition(latLng);
        marker.addEventListener("dblclick", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                finaliseArea();
            }
        });
    }

    // Create graphic using predefined path, used in loading from files
    ClientGraphic(LinkedList<LatLng> path, GuiComponents guiComponents) {
        this.guiComponents = guiComponents;
        this.path = path;
        generatePolygon();

        // Creating an information window
        generateInfoWindow();
        addEventListeners();
        // Store area for use in simulation
        this.client = new ClientRegion(getPathAsArray(), this);
        clientGraphics.add(this);
    }

    // Adds a vertex to this client region, used in the manual drawing process
    void addVertexToArea(LatLng latLng) {
        path.add(latLng);
        LatLng[] pathArray = getPathAsArray();
        // Set current perimeter
        areaPeri.setPath(pathArray);
    }

    // Returns the current path vertices in an array
    private LatLng[] getPathAsArray() {
        final LatLng[] pathArray = new LatLng[path.size()];
        path.toArray(pathArray);
        return pathArray;
    }

    // Sets the lambda arrival rate for this client region
    void setLambda(double lambda) {
        client.setLambda(lambda);
    }

    // Completes the manual process of drawing this client region and displays
    // the area as a polygon on the map
    private void finaliseArea() {
        if (path.size() < 3) {
            // Error, area must have >= 3 vertices
            System.out.println("Client must have >= 3 vertices!");
            new ClientErrorDialog();
        } else {
            areaPeri.setVisible(false);
            generatePolygon();
            generateInfoWindow();
            addEventListeners();

            // Remove first vertex marker
            marker.setVisible(false);

            this.client = new ClientRegion(getPathAsArray(), this);
            // Store area for use in simulation
            clientGraphics.add(this);
            areaBeingDrawn = null;
            buttonState = BUTTON_STATE.NONE;
            guiComponents.finishClientCreation();
        }
    }

    // Add event listeners to client graphic to allow modification on names
    // and lambda variables
    private void addEventListeners() {
        polygon.addEventListener("click", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                infoWindow.open(map, polygon.getPaths()[0][0]);
            }
        });
        final Graphic graphic = this;
        polygon.addEventListener("rightclick", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                new RegionSettingsFrame(graphic, client);
            }
        });

    }

    // Creates the name label for this client graphic
    private void generateInfoWindow() {
        // Creating an information window
        infoWindow = new InfoWindow(map);
        // Putting the address and location to the content of the information window
        infoWindow.setContent("<b>Client region #" + clientGraphics.size() + "</b>");
        // Moving the information window to the result location
        infoWindow.setPosition(polygon.getPaths()[0][0]);
        // Showing of the information window
        infoWindow.open(map, polygon.getPaths()[0][0]);
    }

    // Creates the polygon graphic for this client
    private void generatePolygon() {
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
    }

    // Removes this client from the simulation
    @Override
    public void remove() {
        polygon.setVisible(false);
        areaPeri.setVisible(false);
        infoWindow.close();
        clientGraphics.remove(this);
        for (RequestMarker requestMarker : requestMarkers.values())
            requestMarker.remove();
    }

    // Adds a marker to the map indicating that a request has arrived from that
    // location
    public void addRequestMarker(Location location) {
        requestMarkers.put(location, new RequestMarker(location.getLocationAsLatLng()));
    }

    // Changes the colour of the request marker at location to indicate it is being served
    public void setRequestMarkerServing(Location location) {
        requestMarkers.get(location).setServingColour();
    }

    // Changes the colour of the request marker at location to indicate it has been served
    public void setRequestMarkerServed(Location location) {
        requestMarkers.get(location).setServedColour();
    }

    // Returns the path of vertices surrounding the client region
    LinkedList<LatLng> getPath() {
        return path;
    }

    // Renames the current client region
    @Override
    public void rename(String newName) {
        infoWindow.setContent("<b>" + newName + "</b>");
    }

    // Returns the name of the current region without HTML tags
    @Override
    public String getName() {
        String html = infoWindow.getContent();
        // Splits the html String to get the inner text without formatting
        String[] t = html.split("<b>");
        String[] s = t[1].split("</b>");
        return s[0];
    }

    // Returns the polygon graphic corresponding to this client
    public Polygon getPolygon() {
        return polygon;
    }

    // Returns the ClientRegion object corresponding to this client
    ClientRegion getClientRegion() {
        return client;
    }
}
