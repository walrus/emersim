package jmt.engine.simEngine;

import jmt.jmarkov.SpatialQueue.SpatialQueueData;
import java.util.ArrayList;

public class SpatialQueueEngine extends ArrayList<SimEvent> implements EventQueue {

    private SpatialQueueData data = null;
    private ArrayList<SimEvent> internal;

    public SpatialQueueEngine() {
        internal = new ArrayList<SimEvent>();
    }

    public void define_area(double lat, double lon, double radius) {
        data = new SpatialQueueData(lat, lon, radius);
    }

    /* FIXME: values need to be generated/requested */
    public boolean create_client() {
        if (data == null)
            return false;

        SimEvent ev = new SimEvent(SimEvent.CREATE,
            /* time */     0,
            /* src */      0,
            /* dst */      0,
            /* tag */      0,
            /* edata */ null);

        internal.add(ev);

        return true;
    }

    public void run()
    {
        while (true) {
            /* TODO: find a way to exit or use Simulation */

            /* FIXME: need a way to generate a random chance */
            if (0 % 1 == 0)
                create_client();
        }
    }

    @Override
    public SimEvent pop() {
        return internal.remove(0);
    }

    @Override
    public SimEvent peek() {
        return internal.get(0);
    }

    @Override
    public boolean remove(SimEvent ev) {
        return internal.remove(ev);
    }
}