package model;

import javafx.scene.paint.Color;

public class Planet extends Body {

	public Planet(double rx, double ry, double vx, double vy, double mass, Color color) {
		super(rx, ry, vx, vy, mass, color);
		// TODO Auto-generated constructor stub
	}
	
	public Planet(double rx, double ry, double mass, Color color){
		super(rx, ry, mass, color);
	}
	
	public Planet(double dist, double mass, Color color){
		super(dist, mass, color);
	}

}
