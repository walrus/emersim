package jmt.jmarkov.SpatialQueue;

import jmt.jmarkov.SpatialQueue.Sender;
import jmt.jmarkov.SpatialQueue.Location;
import jmt.jmarkov.SpatialQueue.Simulator;

import org.junit.Assert;
import org.junit.Test;

import static jmt.jmarkov.SpatialQueue.Simulator.isLocationWithinArea;

public class AreaTest {

    Sender sender = new Sender(new Location(0,0));

    Location[] polygon = {new Location(2,0),
                            new Location(1.5,-1.5),
                            new Location(0, -2),
                            new Location(-1.5,-1.5),
                            new Location(-2, 0),
                            new Location(-1.5, 1.5),
                            new Location(0, 2),
                            new Location(1.5, 1.5)};

    @Test
    public void canTellWhetherSenderWithinArea() {
        Assert.assertTrue(isLocationWithinArea(new Location(0,0), polygon));
    }
}
