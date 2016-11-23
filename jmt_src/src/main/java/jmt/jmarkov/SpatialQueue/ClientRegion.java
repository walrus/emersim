package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.LatLng;
import jmt.jmarkov.SpatialQueue.Map.ClientEntity;
import jmt.jmarkov.SpatialQueue.Simulation.Request;
import jmt.jmarkov.SpatialQueue.Simulation.RequestGenerator;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ClientRegion implements Shape {

    Point2D.Double[] vertices;
    ClientEntity mapEntity;
    RequestGenerator requestGenerator;

    public ClientRegion(LatLng[] areaVertices, ClientEntity clientEntity) {
        this.mapEntity = clientEntity;
        vertices = new Point2D.Double[areaVertices.length];
        for (int i=0; i<areaVertices.length; i++) {
            vertices[i] = new Point2D.Double(areaVertices[i].getLng(), areaVertices[i].getLat());
        }
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public Rectangle2D getBounds2D() {
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

    @Override
    public boolean contains(double x, double y) {
        return false;
    }

    @Override
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

    public Location generatePoint(){
        Rectangle2D r = getBounds2D();
        double x, y;
        do {
            x = r.getX() + r.getWidth() * Math.random();
            y = r.getY() + r.getHeight() * Math.random();
        } while(!contains(new Point2D.Double(x, y)));

        if (mapEntity != null)
            mapEntity.displayRequestLocationOnMap(new Location(x, y));
        return new Location(x, y);
    }

    public Location[] generatePointsWithinRegions(ClientRegion[] regions) {
        Location[] locations = new Location[regions.length];
        regions = rankRegions(regions);
        for(int i = 0; i < regions.length; i++) {
            Location location = generatePoint();
            locations[i] = location;
        }
        return locations;
    }

    public ClientRegion[] rankRegions(ClientRegion[] regions) {
        double max_probability = regions[0].getRegionProbability();
        ClientRegion[] rankedRegions = regions;
        for(int i = 0; i < regions.length; i++) {
            if (max_probability < regions[i].getRegionProbability()) {
                max_probability = regions[i].getRegionProbability();
                rankedRegions[0] = regions[i];
                rankedRegions[i] = regions[0];
            }
        }
        return rankedRegions;
    }

    /* Set the request generator for this client region with rate parameter */
    public void setRequestGenerator(RequestGenerator generator){
        this.requestGenerator = generator;
    }

    private double getRegionProbability() {
        //to be implemented
        return 0;
    }


    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return null;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return null;
    }
}
