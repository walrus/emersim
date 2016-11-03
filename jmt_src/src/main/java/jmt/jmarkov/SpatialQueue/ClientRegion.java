package jmt.jmarkov.SpatialQueue;

import com.teamdev.jxmaps.LatLng;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ClientRegion implements Shape {

    Point2D.Double[] vertices;

    public ClientRegion(LatLng[] areaVertices) {
        vertices = new Point2D.Double[areaVertices.length];
        for (int i=0; i<areaVertices.length; i++) {
            vertices[i] = new Point2D.Double(areaVertices[i].getLat(), areaVertices[i].getLng());
        }
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public Rectangle2D getBounds2D() {
        Double maxX = vertices[0].getX();
        Double minX = vertices[0].getX();
        Double maxY = vertices[0].getY();
        Double minY = vertices[0].getY();
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
        } while(!contains(x,y));

        return new Location(x, y);
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
