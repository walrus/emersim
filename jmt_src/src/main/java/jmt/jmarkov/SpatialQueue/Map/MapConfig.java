package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.MouseEvent;
import com.teamdev.jxmaps.Polygon;
import com.teamdev.jxmaps.swing.MapView;
import jmt.jmarkov.SpatialQueue.Location;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class MapConfig extends MapView {

    private static final String INITIAL_LOCATION = "Imperial College London, SW7 2AZ";
    private OptionsWindow optionsWindow;
    private boolean placeMarker;
    private boolean placeAreaVertex;
    private Map map;
    private LinkedList<Polygon> selectedAreas = new LinkedList<>();

    public MapConfig(MapViewOptions options) {
        super(options);
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
                        if (placeMarker) {
                            // Closing initially created info window
                            infoWindow.close();
                            // Creating a new marker
                            final Marker marker = new Marker(map);
                            // Move marker to the position where user clicked
                            marker.setPosition(mouseEvent.latLng());
                            placeMarker = false;
                        }
                    }
                });

                // Event handler for drawing areas
                map.addEventListener("click", new MapMouseEvent() {
                    @Override
                    public void onEvent(MouseEvent mouseEvent) {
                        if (placeAreaVertex)
                            addPointToArea(mouseEvent);
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
                searchField.setText(INITIAL_LOCATION);
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

    Marker currentLocation;
    InfoWindow infoWindow;

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
                    // Creating a marker object
                    if (currentLocation == null)
                        currentLocation = new Marker(map);

                    // Setting position of the marker to the result location
                    currentLocation.setPosition(location);
                    // Creating an information window
                    if (infoWindow == null)
                        infoWindow = new InfoWindow(map);
                    // Putting the address and location to the content of the information window
                    infoWindow.setContent("<b>" + result.getFormattedAddress() + "</b><br>" + location.toString());
                    // Moving the information window to the result location
                    infoWindow.setPosition(location);
                    // Showing of the information window
                    infoWindow.open(map, currentLocation);
                }
            }
        });
    }

    private Polyline pathLine;
    private LinkedList<LatLng> path = new LinkedList<>();
    private void addPointToArea(MouseEvent mouseEvent) {
        path.add(mouseEvent.latLng());
        LatLng[] pathArray = new LatLng[path.size()];
        pathLine.setPath(path.toArray(pathArray));
        if (path.size() == 1) {
            final Marker marker = new Marker(map);
            marker.setPosition(mouseEvent.latLng());
            marker.addEventListener("click", new MapMouseEvent() {
                @Override
                public void onEvent(MouseEvent mouseEvent) {
                    placeAreaVertex = false;
                    pathLine.setVisible(false);
                    Polygon polygon = new Polygon(map);
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
                    // Delete current path
                    path = new LinkedList<>();
                    // Remove first vertex marker
                    marker.setVisible(false);
                    // Store area for use in simulation
                    selectedAreas.add(polygon);
                }
            });
        }
    }

    public void toggleMarkerPlacement() {
        placeMarker = true;
    }

    public void toggleAreaPlacement() {
        placeAreaVertex = true;
        pathLine = new Polyline(map);
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
        pathLine.setOptions(options);
    }

    public Location translateCoordinate(LatLng location) {
        Location loc = new Location(location.getLat(), location.getLng());
        return loc;
    }
}