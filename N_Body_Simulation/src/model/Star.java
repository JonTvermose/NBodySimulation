package model;

import javafx.scene.paint.Color;

public class Star extends Body{

	public Star(double rx, double ry, double vx, double vy, double mass, Color color) {
		super(rx, ry, vx, vy, mass, color);
		// TODO Auto-generated constructor stub
	}
	
	public Star(double rx, double ry, double mass, Color color){
		super(rx, ry, mass, color);
	}
	
	public Star(double dist, double mass, Color color){
		super(dist, mass, color);
	}

	public Star(double d, double f, double g, double h, double solarmass, Color c, Body s) {
		super(d,f,g,h,solarmass,c,s);
	}

}
