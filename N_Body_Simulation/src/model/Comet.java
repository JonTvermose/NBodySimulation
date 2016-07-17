package model;

import javafx.scene.paint.Color;

public class Comet extends Body{

	public Comet(double rx, double ry, double vx, double vy, double mass, Color color) {
		super(rx, ry, vx, vy, mass, color);
		// TODO Auto-generated constructor stub
	}
	
	public Comet(double rx, double ry, double mass, Color color){
		super(rx, ry, mass, color);
	}
	
	public Comet(double dist, double mass, Color color){
		super(dist, mass, color);
	}

}
