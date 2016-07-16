package model;

import javafx.scene.paint.Color;

public class Body {

	public double rx, ry;       // holds the cartesian positions
	public double vx, vy;       // velocity components 
	public double fx, fy;       // force components
	public double mass;         // mass
	public Color color;         // color 
	public int diameter;		// diameter in pixels when drawing
	public int impacts;			// Number of impacts between this body and other bodies

	// create and initialize a new Body
	public Body(double rx, double ry, double vx, double vy, double mass, Color color) {
		this.rx    = rx;
		this.ry    = ry;
		this.vx    = vx;
		this.vy    = vy;
		this.mass  = mass;
		this.color = color;
		impacts = 0;
		addBodyMass(null);
	}

	public void addBodyMass(Body b){
		// Adjust the size & color according to the bodys mass
		if(b!=null){
			mass += b.mass;			
		}
		diameter = (int) (Math.pow(((mass/BodySystem.solarmass)/3.14)*(3.0/4.0), 1.0/3.0)*50);
		if(diameter < 1){
			diameter = 1;	    	
		}
		if (mass >= BodySystem.solarmass/10){
			color = Color.DARKORANGE;
		}
		impacts++;
	}

	// update the velocity and position using a timestep dt
	public void update(double dt) {
		vx += dt * fx / mass;
		vy += dt * fy / mass;
		rx += dt * vx;
		ry += dt * vy;
	}

	// returns the distance between two bodies
	public double distanceTo(Body b) {
		double dx = rx - b.rx;
		double dy = ry - b.ry;
		return Math.sqrt(dx*dx + dy*dy);
	}

	public boolean collided(Body b){
		if(distanceTo(b) < diameter/2 + b.diameter/2){
			return true;
		}
		return false;
	}

	// set the force to 0 for the next iteration
	public void resetForce() {
		fx = 0.0;
		fy = 0.0;
	}

	// compute the net force acting between the body a and b, and
	// add to the net force acting on a
	public void addForce(Body b) {
		Body a = this;
		double EPS = 3E4;      // softening parameter (just to avoid infinities)
		double dx = b.rx - a.rx;
		double dy = b.ry - a.ry;
		double dist = Math.sqrt(dx*dx + dy*dy);
		double F = (BodySystem.G * a.mass * b.mass) / (dist*dist + EPS*EPS);
		a.fx += F * dx / dist;
		a.fy += F * dy / dist;
	}

}
