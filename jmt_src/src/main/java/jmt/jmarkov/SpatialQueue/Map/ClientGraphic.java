package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.*;
import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Simulation.ClientRegion;

import java.util.LinkedList;

import static jmt.jmarkov.SpatialQueue.Map.MapConfig.*;

class ClientGraphic implements Entity {
    private Polygon polygon;
    private InfoWindow infoWindow;
    private Marker marker;
    private LinkedList<LatLng> path = new LinkedList<>();
    private Polyline areaPeri;
    private GuiComponents guiComponents;
    private ClientRegion client;

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

    ClientGraphic(LinkedList<LatLng> path, GuiComponents guiComponents) {
        this.guiComponents = guiComponents;
        this.path = path;
        generatePolygon();

        // Creating an information window
        generateInfoWindow();
        addEventListeners();
        // Store area for use in simulation
        this.client = new ClientRegion(getPathAsArray());
        clientGraphics.add(this);
    }

    void addVertexToArea(LatLng latLng) {
        path.add(latLng);
        LatLng[] pathArray = getPathAsArray();
        // Set current perimeter
        areaPeri.setPath(pathArray);
    }

    private LatLng[] getPathAsArray() {
        final LatLng[] pathArray = new LatLng[path.size()];
        path.toArray(pathArray);
        return pathArray;
    }

    void setLambda(double lambda) {
        client.setLambda(lambda);
    }

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

            this.client = new ClientRegion(getPathAsArray());
            // Store area for use in simulation
            clientGraphics.add(this);
            areaBeingDrawn = null;
            buttonState = BUTTON_STATE.NONE;
            guiComponents.finishClientCreation();
        }
    }

    private void addEventListeners() {
        polygon.addEventListener("click", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                infoWindow.open(map, polygon.getPaths()[0][0]);
            }
        });
        final Entity entity = this;
        final ClientGraphic clientGraphic = this;
        polygon.addEventListener("rightclick", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                new RenameEntityFrame(entity);
            }
        });
        polygon.addEventListener("dblclick", new MapMouseEvent() {
            @Override
            public void onEvent(MouseEvent mouseEvent) {
                new LambdaSliderFrame(clientGraphic);
            }
        });
    }

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

    @Override
    public void remove() {
        polygon.setVisible(false);
        areaPeri.setVisible(false);
        infoWindow.close();
        clientGraphics.remove(this);
    }

    LinkedList<LatLng> getPath() {
        return path;
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

    public Polygon getPolygon() {
        return polygon;
    }

    ClientRegion getClientRegion() {
        return client;
    }
}
