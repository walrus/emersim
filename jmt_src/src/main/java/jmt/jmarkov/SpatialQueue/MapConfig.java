package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.swing.MapView;

public class MapConfig extends MapView {
    public MapConfig(MapViewOptions options) {
        super(options);
        setOnMapReadyHandler(new MapReadyHandler() {
            @Override
            public void onMapReady(MapStatus status) {
                if (status == MapStatus.MAP_STATUS_OK) {
                    final Map map = getMap();
                    map.setZoom(13.0);
                    GeocoderRequest request = new GeocoderRequest(map);
                    request.setAddress("Imperial College London, SW7 2AZ");

                    getServices().getGeocoder().geocode(request, new GeocoderCallback(map) {
                        @Override
                        public void onComplete(GeocoderResult[] result, GeocoderStatus status) {
                            if (status == GeocoderStatus.OK) {
                                map.setCenter(result[0].getGeometry().getLocation());
                                Marker marker = new Marker(map);
                                marker.setPosition(result[0].getGeometry().getLocation());

                                final InfoWindow window = new InfoWindow(map);
                                window.setContent("Imperial College London");
                                window.open(map, marker);
                            }
                        }
                    });
                }
            }
        });
    }
}
