package jmt.jmarkov.SpatialQueue.Simulation;

import com.teamdev.jxmaps.LatLng;
import jmt.jmarkov.SpatialQueue.Map.MapConfig;
import jmt.jmarkov.SpatialQueue.Utils.Location;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ClientRegion {

    private Point2D.Double[] vertices;
    private RequestGenerator requestGenerator;
    // Default number of arrivals per second
    private double lambda = 0.1;

    public ClientRegion(LatLng[] areaVertices) {
        vertices = new Point2D.Double[areaVertices.length];
        for (int i = 0; i < areaVertices.length; i++) {
            vertices[i] = new Point2D.Double(areaVertices[i].getLng(), areaVertices[i].getLat());
        }
    }

    private Rectangle2D getBounds2D() {
        Double maxX = Double.MIN_VALUE;
        Double minX = Double.MAX_VALUE;
        Double maxY = Double.MIN_VALUE;
        Double minY = Double.MAX_VALUE;
        for (Point2D.Double p : vertices) {
            maxX = Math.max(maxX, p.getX());
            minX = Math.min(minX, p.getX());
            maxY = Math.max(maxY, p.getY());
            minY = Math.min(minY, p.getY());
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean contains(Point2D p) {
        if (!getBounds2D().contains(p))
            return false;

        boolean inside = false;

        for (int i = 0, j = vertices.length - 1; i < vertices.length; j = i++) {
            if ((vertices[i].getY() > p.getY()) != (vertices[j].getY() > p.getY()) &&
                    p.getX() < (vertices[j].getX() - vertices[i].getX())
                            * (p.getY() - vertices[i].getY())
                            / (vertices[j].getY() - vertices[i].getY()) + vertices[i].getX()) {
                inside = !inside;
            }
        }
        return inside;
    }

    public Location generatePoint() {
        Rectangle2D r = getBounds2D();
        double x, y;
        do {
            x = r.getX() + r.getWidth() * Math.random();
            y = r.getY() + r.getHeight() * Math.random();
        } while (!contains(new Point2D.Double(x, y)));

        MapConfig.displayRequestLocationOnMap(new LatLng(y, x));
        return new Location(x, y);
    }

    /* Set the request generator for this client region with rate parameter */
    public void setRequestGenerator(RequestGenerator generator) {
        this.requestGenerator = generator;
    }

    public RequestGenerator getGenerator() {
        return this.requestGenerator;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
        if (this.requestGenerator != null)
            this.requestGenerator.setLambda(lambda);
    }
}
