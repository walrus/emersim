package jmt.jmarkov.SpatialQueue;

import jmt.jmarkov.SpatialQueue.Simulation.Server;

import static org.junit.Assert.*;
import org.junit.Test;

public class ServerTest {

    Server server = new Server(null, createRandomLocation());

    private static Location createRandomLocation() {
        double x = Math.random() * 90;
        double y = Math.random() * 180;

        return new Location(x, y);
    }

    @Test
    public void testReceiverExists() {
        assertNotNull(this.server);
    }
}
