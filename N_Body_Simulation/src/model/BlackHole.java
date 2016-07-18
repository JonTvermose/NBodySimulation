package model;

import javafx.scene.paint.Color;

public class BlackHole extends Body{

	public BlackHole(double dist, double mass, Color color) {
		super(dist, mass, color);	
	}
	
	public BlackHole(double rx, double ry, double mass, Color color){
		super(rx,ry,mass,color);
	}

}
