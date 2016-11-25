package jmt.jmarkov.SpatialQueue.Map;

import com.teamdev.jxmaps.DirectionsResult;

import java.util.concurrent.*;

public class JxMapsAPICaller {

    // Minimum time between calls in milliseconds
    private static final int RATE_LIMIT = 1000;

    private static final ScheduledExecutorService handler = Executors.newScheduledThreadPool(1);

    public static DirectionsResult handleDirectionCall(MapConfig map, double x1, double y1, double x2, double y2) {
        Future<DirectionsResult> directions = handler.schedule(new DirectionsJob(map, x1, y1, x2, y2), RATE_LIMIT, TimeUnit.MILLISECONDS);
        DirectionsResult directionsResult = null;
        try {
            directionsResult = directions.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (directionsResult == null) {
            // If API fails for any reason, retry
            return handleDirectionCall(map, x1, y1, x2, y2);
        }
        return directionsResult;
    }
}
