package jmt.jmarkov.SpatialQueue;

import java.lang.Math;

public class SpatialQueueData {

	private double latitude;
	private double longitude;
	private double radius;
	private double area = radius * radius * Math.PI;

	public SpatialQueueData(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public SpatialQueueData(double latitude, double longitude, double radius) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}

	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}

	public void setRadius(double radius){
		this.radius = radius;
	}

	public double getRadius(){
		return radius;
	}

	public boolean isValid(){
		return radius != 0;
	}

	public void render() {
	}


}



