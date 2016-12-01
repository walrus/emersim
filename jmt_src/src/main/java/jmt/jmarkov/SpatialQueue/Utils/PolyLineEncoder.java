package jmt.jmarkov.SpatialQueue.Utils;

import com.teamdev.jxmaps.LatLng;

import java.util.Arrays;
import java.util.List;

public class PolyLineEncoder {

    /**
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    static String encode(final List<LatLng> path) {
        long lastLat = 0;
        long lastLng = 0;

        final StringBuffer result = new StringBuffer();

        for (final LatLng point : path) {
            long lat = Math.round(point.getLat() * 1e5);
            long lng = Math.round(point.getLng() * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    private static void encode(long v, StringBuffer result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }

    /**
     * Encodes an array of LatLngs into an encoded path string.
     */
    public static String encode(LatLng[] path) {
        return encode(Arrays.asList(path));
    }
}
