package jmt.jmarkov.SpatialQueue.Utils;

import com.teamdev.jxmaps.LatLng;

public class LatLngUtils {

    public static double calculateDistance(LatLng latLng1, LatLng latLng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(latLng2.getLat()-latLng1.getLat());
        double dLng = Math.toRadians(latLng2.getLng()-latLng1.getLng());
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(latLng1.getLat())) * Math.cos(Math.toRadians(latLng2.getLat())) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }
}
