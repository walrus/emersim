package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.LatLng;

import jmt.jmarkov.SpatialQueue.Map.ClientEntity;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;

public class ClientRegionTest {

    LatLng[] vertices = {new LatLng(2,0),
            new LatLng(1.5,-1.5),
            new LatLng(0, -2),
            new LatLng(-1.5,-1.5),
            new LatLng(-2, 0),
            new LatLng(-1.5, 1.5),
            new LatLng(0, 2),
            new LatLng(1.5, 1.5)};
    ClientRegion polygon = new ClientRegion(vertices, null);

    @Test
    public void canTellWhetherPointIsInRegion() {
        Assert.assertTrue(polygon.contains(new Point(0, 0)));
    }

    @Test
    public void getCanTellWhetherPointIsOutsideRegion() {
        Assert.assertFalse(polygon.contains(new Point(10, 0)));
    }
}
