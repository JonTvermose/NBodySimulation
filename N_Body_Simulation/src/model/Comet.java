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
	
	public Comet(double a, double e, double om, double w, double mass, Color white, Body center) {
		super(a,e,om,w,mass,white,center);
	}

	@Override
	public double circlev(double rx, double ry) {
		double r2=Math.sqrt(rx*rx+ry*ry);
		double numerator=BodySystem.G*BodySystem.solarmass*0.1;
		return Math.sqrt(numerator/r2);
	}	

}
