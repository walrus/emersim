package jmt.jmarkov.SpatialQueue.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.swing.MapView;
import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Simulation.ClientRegion;
import jmt.jmarkov.SpatialQueue.Simulation.Server;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.concurrent.*;

public class MapConfig extends MapView {

    private static final LatLng INITIAL_LOCATION = new LatLng(51.498813, -0.174877);
    private OptionsWindow optionsWindow;
    static ClientGraphic areaBeingDrawn;
    static Map map;
    static LinkedList<ClientGraphic> clientGraphics = new LinkedList<>();
    static LinkedList<ServerGraphic> serverGraphics = new LinkedList<>();
    private GuiComponents guiComponents;
    private TRAVEL_METHOD travelMethod = TRAVEL_METHOD.DRIVING;
    // speed used for crow-flies travel
    private int straightLineSpeed;
    // Holds current line drawn for visualisation of crow-flies simulations
    private Polyline route;

    public enum BUTTON_STATE {ADD_CLIENT, DRAWING_CLIENT, ADD_RECEIVER, NONE}
    public enum TRAVEL_METHOD {DRIVING, BICYCLING, WALKING, PUBLIC_TRANSPORT, AS_CROW_FLIES}

    static BUTTON_STATE buttonState;

    public MapConfig(MapViewOptions options, final GuiComponents guiComponents) {
        super(options);
        this.guiComponents = guiComponents;
        final MapConfig mapConfig = this;
        setOnMapReadyHandler(new MapReadyHandler() {
            @Override
            public void onMapReady(MapStatus status) {
                // Getting the associated map object
                map = getMap();
                // Setting initial zoom value
                map.setZoom(7.0);
                // Creating a map options object
                MapOptions options = new MapOptions(map);
                // Creating a map type control options object
                MapTypeControlOptions controlOptions = new MapTypeControlOptions(map);
                // Changing position of the map type control
                controlOptions.setPosition(ControlPosition.TOP_RIGHT);
                // Setting map type control options
                options.setMapTypeControlOptions(controlOptions);
                // Setting map options
                map.setOptions(options);
                map.setCenter(INITIAL_LOCATION);

                // Event handler for adding receivers
                map.addEventListener("click", new MapMouseEvent() {
                    @Override
                    public void onEvent(MouseEvent mouseEvent) {
                        if (buttonState == BUTTON_STATE.ADD_RECEIVER) {
                            buttonState = BUTTON_STATE.NONE;
                            new ServerGraphic(mapConfig, mouseEvent.latLng());
                        }
                    }
                });

                // Event handler for drawing areas
                map.addEventListener("click", new MapMouseEvent() {
                    @Override
                    public void onEvent(MouseEvent mouseEvent) {
                        if (buttonState == BUTTON_STATE.ADD_CLIENT) {
                            buttonState = BUTTON_STATE.DRAWING_CLIENT;
                            areaBeingDrawn = new ClientGraphic(mouseEvent.latLng(), guiComponents);
                        } else if (buttonState == BUTTON_STATE.DRAWING_CLIENT) {
                            areaBeingDrawn.addVertexToArea(mouseEvent.latLng());
                        }
                    }
                });
            }
        });
    }

    // API call handler
    // Rate limit for JxMaps API, defined as the minimum time between API calls
    private static final int RATE_LIMIT = 1000;

    // Executor service for API calls to ensure that the rate limit is not exceeded
    private static final ScheduledExecutorService handler = Executors.newScheduledThreadPool(1);

    // Creates a new API call to be scheduled in the queue
    public DirectionsResult handleDirectionCall(TravelMode travelMode, double x1, double y1, double x2, double y2) throws DirectionsNotFoundException {
        Future<DirectionsResult> directions = handler.schedule(new DirectionsJob(this, travelMode, x1, y1, x2, y2), RATE_LIMIT, TimeUnit.MILLISECONDS);
        DirectionsResult directionsResult = null;
        try {
            directionsResult = directions.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Critical error so exit
            System.exit(-1);
        } catch (ExecutionException e) {
            Throwable rootException = e.getCause();
            if (rootException instanceof DirectionsNotFoundException) {
                throw (DirectionsNotFoundException) rootException;
            }
            if (rootException instanceof CallRateExceededException) {
                // Retry API call
                return handleDirectionCall(travelMode, x1, y1, x2, y2);
            }
        }
        return directionsResult;
    }

    // Displays the given directions on the map, to visualise the current job
    public void displayRoute(DirectionsResult directionsResult) {
        // Hide current current crow-flies route (if applicable)
        if (route != null)
            route.setVisible(false);
        map.getDirectionsRenderer().setDirections(directionsResult);
    }

    public void displayCrowFliesRoute(LatLng start, LatLng end) {
        // Hide current current crow-flies route (if applicable)
        if (route != null)
            route.setVisible(false);
        // Create perimeter line
        route = new Polyline(map);
        // Creating a polyline options object
        PolylineOptions options = new PolylineOptions();
        // Setting geodesic property value
        options.setGeodesic(true);
        // Setting stroke color value
        options.setStrokeColor("#6EABDA");
        // Setting stroke opacity value
        options.setStrokeOpacity(1.0);
        // Setting stroke weight value
        options.setStrokeWeight(3.0);
        // Applying options to the polyline
        route.setOptions(options);
        LatLng[] routePath = {start, end};
        // Set line path as route
        route.setPath(routePath);

        // Centre map on route path
        double lowLat = (start.getLat() > end.getLat()) ? end.getLat() : start.getLat();
        double highLat = (start.getLat() > end.getLat()) ? start.getLat() : end.getLat();
        double lowLng = (start.getLng() > end.getLng()) ? end.getLng() : start.getLng();
        double highLng = (start.getLng() > end.getLng()) ? start.getLng() : end.getLng();
        LatLng sw = new LatLng(lowLat, lowLng);
        LatLng ne = new LatLng(highLat, highLng);
        LatLngBounds bounds = new LatLngBounds(sw, ne);
        map.fitBounds(bounds);
    }

    // Notifies the map of changes to the buttons in the GUI
    public void setButtonState(BUTTON_STATE buttonState) {
        this.buttonState = buttonState;
    }

    // Returns the ClientRegion objects that correspond to clients placed on the map
    public LinkedList<ClientRegion> getClientRegions() {
        LinkedList<ClientRegion> clientRegions = new LinkedList<>();
        for (ClientGraphic c : clientGraphics) {
            clientRegions.add(c.getClientRegion());
        }
        return clientRegions;
    }

    // Returns the Server objects that correspond to servers placed on the map
    public LinkedList<Server> getServers() {
        LinkedList<Server> servers = new LinkedList<>();
        for (ServerGraphic s : serverGraphics)
            servers.add(s.getServer());
        return servers;
    }

    public void setTravelMethod(TRAVEL_METHOD travelMethod) {
        this.travelMethod = travelMethod;
    }

    public TRAVEL_METHOD getTravelMethod() {
        return travelMethod;
    }

    public int getStraightLineSpeed() {
        return straightLineSpeed;
    }

    public void setStraightLineSpeed(int straightLineSpeed) {
        this.straightLineSpeed = straightLineSpeed;
    }

    public void removeAllRequestMarkers() {
        for (ClientGraphic c : clientGraphics) {
            c.removeRequestMarkers();
        }
    }

    // Saves the server objects currently placed into a string to be loaded again later
    public String saveServers() {
        LinkedList<LatLng> serverLocations = new LinkedList<>();
        for (ServerGraphic serverGraphic : serverGraphics) {
            serverLocations.add(serverGraphic.getPosition());
        }
        Gson gson = new Gson();
        return gson.toJson(serverLocations);
    }

    // Loads the servers as defined by the given string
    public void loadServers(String jsonString) {
        removeServers();
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<LatLng>>(){}.getType();
        LinkedList<LatLng> locations = gson.fromJson(jsonString, type);
        for (LatLng latLng : locations) {
            serverGraphics.add(new ServerGraphic(this, latLng));
        }
    }

    public void removeServers() {
        LinkedList<ServerGraphic> toRemove = new LinkedList<>();
        for (ServerGraphic sg : serverGraphics) {
            toRemove.add(sg);
        }
        serverGraphics.removeAll(toRemove);
        for (ServerGraphic sg : toRemove) {
            sg.remove();
        }
    }

    // Saves the client objects currently placed into a string to be loaded again later
    public String saveClients() {
        LinkedList<LinkedList<LatLng>> clientPaths = new LinkedList<>();
        for (ClientGraphic clientGraphic : clientGraphics) {
            clientPaths.add(clientGraphic.getPath());
            System.out.println(clientGraphic.getName());
        }
        Gson gson = new Gson();
        return gson.toJson(clientPaths);
    }

    // Loads the clients as defined by the given string
    public void loadClients(String jsonString) {
        removeClients();
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<LinkedList<LatLng>>>(){}.getType();
        LinkedList<LinkedList<LatLng>> paths = gson.fromJson(jsonString, type);

        for (LinkedList<LatLng> path : paths) {
            clientGraphics.add(new ClientGraphic(path, guiComponents));
        }
    }

    public void removeClients() {
        LinkedList<ClientGraphic> toRemove = new LinkedList<>();
        for (ClientGraphic cg : clientGraphics) {
            toRemove.add(cg);
        }
        clientGraphics.removeAll(toRemove);
        for (ClientGraphic cg : toRemove) {
            cg.remove();
        }
    }

    // Below this is code provided by JxMaps for location search

    @Override
    public void addNotify() {
        super.addNotify();
        optionsWindow = new OptionsWindow(this, new Dimension(350, 40)) {
            @Override
            public void initContent(JWindow contentWindow) {
                JPanel content = new JPanel(new GridBagLayout());
                content.setBackground(Color.white);

                Font robotoPlain13 = new Font("Roboto", 0, 13);
                final JTextField searchField = new JTextField();
                searchField.setToolTipText("Enter address or coordinates...");
                searchField.setBorder(BorderFactory.createEmptyBorder());
                searchField.setFont(robotoPlain13);
                searchField.setForeground(new Color(0x21, 0x21, 0x21));
                searchField.setUI(new SearchFieldUI(searchField));

                final JButton searchButton = new JButton();
                searchButton.setIcon(new ImageIcon(this.getClass().getResource("Icons/search.png")));
                searchButton.setRolloverIcon(new ImageIcon(this.getClass().getResource("Icons/search_hover.png")));
                searchButton.setBorder(BorderFactory.createEmptyBorder());
                searchButton.setUI(new BasicButtonUI());
                searchButton.setOpaque(false);
                ActionListener searchActionListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        performGeocode(searchField.getText());
                    }
                };
                searchButton.addActionListener(searchActionListener);
                searchField.addActionListener(searchActionListener);

                content.add(searchField, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(11, 11, 11, 0), 0, 0));
                content.add(searchButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(11, 0, 11, 11), 0, 0));

                contentWindow.getContentPane().add(content);
            }

            @Override
            protected void updatePosition() {
                if (parentFrame.isVisible()) {
                    java.awt.Point newLocation = parentFrame.getContentPane().getLocationOnScreen();
                    newLocation.translate(250, 11);
                    contentWindow.setLocation(newLocation);
                    contentWindow.setSize(340, 40);
                }
            }
        };
    }

    class SearchFieldUI extends BasicTextFieldUI {
        private final JTextField textField;

        public SearchFieldUI(JTextField textField) {
            this.textField = textField;
        }

        @Override
        protected void paintBackground(Graphics g) {
            super.paintBackground(g);
            String toolTipText = textField.getToolTipText();
            String text = textField.getText();
            if (toolTipText != null && text.isEmpty()) {
                paintPlaceholderText(g, textField);
            }
        }

        protected void paintPlaceholderText(Graphics g, JComponent c) {
            g.setColor(new Color(0x75, 0x75, 0x75));
            g.setFont(c.getFont());
            String text = textField.getToolTipText();
            if (g instanceof Graphics2D) {
                Graphics2D graphics2D = (Graphics2D) g;
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.drawString(text, 0, 14);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        optionsWindow.dispose();
    }

    private void performGeocode(String text) {
        // Getting the associated map object
        final Map map = getMap();
        // Creating a geocode request
        GeocoderRequest request = new GeocoderRequest(map);
        // Setting address to the geocode request
        request.setAddress(text);

        // Geocoding position by the entered address
        getServices().getGeocoder().geocode(request, new GeocoderCallback(map) {
            @Override
            public void onComplete(GeocoderResult[] results, GeocoderStatus status) {
                // Checking operation status
                if ((status == GeocoderStatus.OK) && (results.length > 0)) {
                    // Getting the first result
                    GeocoderResult result = results[0];
                    // Getting a location of the result
                    LatLng location = result.getGeometry().getLocation();
                    // Setting the map center to result location
                    map.setCenter(location);
                }
            }
        });
    }
}