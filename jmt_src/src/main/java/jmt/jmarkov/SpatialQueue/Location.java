package jmt.jmarkov.SpatialQueue;

/**
 * Class that represents the location of a Client or Receiver.
 * Given a pair of locations, the Receiver should be able to
 * work out the distance between them
 */
public class Location {
    /**
     * This implementation represents location as x, y and z coordinates
     * This works for real world coordinates as follows:
     * x maps to latitude
     * y maps to longitude
     * z maps to height above sea level (but can be ignored)
     */

    private double x;
    private double y;
    private double z;

    // Create new Location specifying all three axes
    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Create new Location specifying only x and y axes.
    public Location(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
