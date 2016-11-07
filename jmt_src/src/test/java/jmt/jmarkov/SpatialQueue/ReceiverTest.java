package jmt.jmarkov.SpatialQueue;

import jmt.jmarkov.SpatialQueue.Simulation.Receiver;

import static org.junit.Assert.*;
import org.junit.Test;

public class ReceiverTest {

    Receiver receiver = new Receiver(createRandomLocation());

    private static Location createRandomLocation() {
        double x = Math.random() * 90;
        double y = Math.random() * 180;

        return new Location(x, y);
    }

    @Test
    public void testReceiverExists() {
        assertNotNull(this.receiver);
    }
}
