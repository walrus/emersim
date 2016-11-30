package jmt.jmarkov.SpatialQueue.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.MouseEvent;
import com.teamdev.jxmaps.swing.MapView;
import jmt.jmarkov.SpatialQueue.ClientRegion;
import jmt.jmarkov.SpatialQueue.Gui.GuiComponents;
import jmt.jmarkov.SpatialQueue.Location;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.LinkedList;

public class MapConfig extends MapView {

    private static final String INITIAL_LOCATION = "Imperial College London, SW7 2AZ";
    private OptionsWindow optionsWindow;
    static ClientEntity areaBeingDrawn;
    static Map map;
    static LinkedList<ClientEntity> clientRegions = new LinkedList<>();
    static LinkedList<Marker> serverMarkers = new LinkedList<>();
    final private GuiComponents guiComponents;

    public enum BUTTON_STATE {ADD_CLIENT, DRAWING_CLIENT, ADD_RECEIVER, NONE};
    static BUTTON_STATE buttonState;

    public MapConfig(MapViewOptions options, final GuiComponents guiComponents) {
        super(options);
        this.guiComponents = guiComponents;
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
                performGeocode(INITIAL_LOCATION);

                // Event handler for adding receivers
                map.addEventListener("click", new MapMouseEvent() {
                    @Override
                    public void onEvent(MouseEvent mouseEvent) {
                        if (buttonState == BUTTON_STATE.ADD_RECEIVER) {
                            buttonState = BUTTON_STATE.NONE;
                            new ServerEntity(mouseEvent);
                        }
                    }
                });

                // Event handler for drawing areas
                map.addEventListener("click", new MapMouseEvent() {
                    @Override
                    public void onEvent(MouseEvent mouseEvent) {
                        if (buttonState == BUTTON_STATE.ADD_CLIENT) {
                            buttonState = BUTTON_STATE.DRAWING_CLIENT;
                            areaBeingDrawn = new ClientEntity(mouseEvent, guiComponents);
                        }
                        else if (buttonState == BUTTON_STATE.DRAWING_CLIENT) {
                            areaBeingDrawn.addPointToArea(mouseEvent);
                        }
                    }
                });
            }
        });
    }

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

    public void displayRoute(DirectionsResult directionsResult) {
        map.getDirectionsRenderer().setDirections(directionsResult);
    }

    public void setButtonState(BUTTON_STATE buttonState) {
        this.buttonState = buttonState;
    }

    public ClientRegion[] getClientRegions() {
        ClientRegion[] regions = new ClientRegion[clientRegions.size()];
        for (int i=0; i<clientRegions.size(); i++) {
            regions[i] = new ClientRegion(clientRegions.get(i).getPolygon().getPath(), clientRegions.get(i));
            regions[i].setLambda(clientRegions.get(i).getLambda());
        }
        return regions;
    }

    public Location getReceiverLocation() {
        Marker marker = serverMarkers.get(0);
        return new Location(marker.getPosition().getLng(), marker.getPosition().getLat());
    }

    public String saveServers() {
        LinkedList<LatLng> serverLocations = new LinkedList<>();
        for (Marker serverMarker : serverMarkers) {
            serverLocations.add(serverMarker.getPosition());
        }
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in String
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(serverLocations);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonInString;
    }

    public void loadServers(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        LinkedList<LatLng> serverLocations = null;
        try {
            serverLocations = mapper.readValue(jsonString, LinkedList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (LatLng latLng : serverLocations) {
            new ServerEntity(latLng);
        }
    }

    public String saveClients() {
        LinkedList<LinkedList> clientPaths = new LinkedList<>();
        for (ClientEntity clientEntity : clientRegions) {
            clientPaths.add(clientEntity.getPath());
        }
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in String
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(clientPaths);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonInString;
    }

    public void loadClients(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        LinkedList<LinkedList> clientPaths = null;
        try {
            clientPaths = mapper.readValue(jsonString, LinkedList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (LinkedList<LatLng> path : clientPaths) {
            new ClientEntity(path, guiComponents);
        }
    }
}