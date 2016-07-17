package model;

import javafx.scene.paint.Color;

public class Asteroid extends Body{

	public Asteroid(double rx, double ry, double vx, double vy, double mass, Color color) {
		super(rx, ry, vx, vy, mass, color);
		// TODO Auto-generated constructor stub
	}
	
	public Asteroid(double rx, double ry, double mass, Color color){
		super(rx, ry, mass, color);
	}
	
	public Asteroid(double dist, double mass, Color color){
		super(dist, mass, color);
		
		//Orient a random 2D circular orbit (1% chance)
		if (Math.random() <=.01) {
			vx=-vx;
			vy=-vy;
		} 
	}

}
