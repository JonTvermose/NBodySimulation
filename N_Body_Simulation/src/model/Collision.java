package model;

public class Collision {
	
	public double rx, ry;       // holds the cartesian positions
	public int diameter;		// diameter in pixels when drawing
	public int frame;			// At what frame did the collision happen

	public Collision(Body body, int frame) {
		this.frame = frame;
		this.rx = body.rx;
		this.ry = body.ry;
		this.diameter = body.diameter;
	}
	
}
