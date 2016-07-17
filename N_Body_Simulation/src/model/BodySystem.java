package model;

import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class BodySystem {

	public static final double solarmass=1.98892e30;
	public static final double earthmass = solarmass/333000.0;

	protected static final double G = 6.673e-11;   // gravitational constant
	
	private double factor;
	private Canvas canvas;
	private ArrayList<Body> bodies;
	private ArrayList<Collision> collisions; // Body's that has been removed in the last update due to collision with another body
	private int frame;

	public BodySystem(Canvas c){
		this.canvas = c;
		factor = (canvas.getWidth()/2)/1e18;
		resetBodies();
		frame = 0;
		collisions = new ArrayList<Collision>();
	}

	public void resetBodies(){
		bodies = new ArrayList<Body>();
		Body sun = new Body(0, 0, 0, 0, solarmass, Color.DARKORANGE);
		
		double px = 1.47e17*3; // Earth orbit
		double py = -1.47e17*3;

		Body earth = getBody(px, py, earthmass, Color.BLUE);
		Body mars = getBody(px*1.524, py*1.524, earthmass*0.107, Color.RED);
		Body jupiter = getBody(px*5.203, py*5.203, earthmass*317.83, Color.YELLOW);
		Body venus = getBody(px*.7233, py*.7233, earthmass*0.815, Color.CYAN);
		Body saturn = getBody(px*9.537, py*9.537, earthmass*95.162, Color.BURLYWOOD);
		Body mercury = getBody(px*.38709, py*.38709, earthmass*0.0553, Color.SILVER);
		
		bodies.add(sun);
		bodies.add(earth);
		bodies.add(mars);
		bodies.add(jupiter);
		bodies.add(venus);
		bodies.add(saturn);
		bodies.add(mercury);
	}
	
	public Body getBody(double px, double py, double mass, Color c){
		double magv = circlev(px,py);

		double absangle = Math.atan(Math.abs(py/px));
		double thetav= Math.PI/2-absangle;
		double phiv = Math.random()*Math.PI;
		double vx   = -1*Math.signum(py)*Math.cos(thetav)*magv;
		double vy   = Math.signum(px)*Math.sin(thetav)*magv;
		return new Body(px, py, vx, vy, mass, c);
	}

	public ArrayList<Body> getBodies(){
		return bodies;
	}

	public void addRandomBodies(int n){
		for (int i = 0; i < n; i++) {
			double px = 1e18*exp(-1.8)*(.5-Math.random());
			double py = 1e18*exp(-1.8)*(.5-Math.random());
						
			double magv = circlev(px,py);

			double absangle = Math.atan(Math.abs(py/px));
			double thetav= Math.PI/2-absangle;
			double phiv = Math.random()*Math.PI;
			double vx   = -1*Math.signum(py)*Math.cos(thetav)*magv;
			double vy   = Math.signum(px)*Math.sin(thetav)*magv;

			//Orient a random 2D circular orbit
			// Should there be counterclockwise orbits?
			if (Math.random() <=.05) {
				vx=-vx;
				vy=-vy;
			} 

			double mass = Math.random()*earthmass*0.0000001; //*10+1e20; 
			Color color = Color.ANTIQUEWHITE;
			bodies.add(new Body(px, py, vx, vy, mass, color));
		}
	}

	public void addBody(Body b){
		bodies.add(b);
	}

	private double exp(double lambda) {
		return -Math.exp(1 - Math.random()) / lambda;
	}

	//the bodies are initialized in circular orbits around the central mass.
	//This is just some physics to do that
	public double circlev(double rx, double ry) {
		double r2=Math.sqrt(rx*rx+ry*ry);
		double numerator=G*solarmass;
		return Math.sqrt(numerator/r2);
	}	

	public void updatePositions(double dt){
		if(frame == Integer.MAX_VALUE){
			collisions = new ArrayList<Collision>();
			frame = 0;
		}
		frame++;
		
		for (int i = 0; i < bodies.size(); i++) {
			bodies.get(i).resetForce();
			//Notice-2 loops-->N^2 complexity
			for (int j = 0; j < bodies.size(); j++) {
				if (i != j){
					bodies.get(i).addForce(bodies.get(j));
					// If two bodies collide then we merge them and keep the combined mass in a single item
					if(collided(bodies.get(i), bodies.get(j))){ 
						if(bodies.get(i).mass > bodies.get(j).mass){
							bodies.get(i).addBodyMass(bodies.get(j));
							collisions.add(new Collision(bodies.get(j), frame));
							bodies.remove(j);
							j--;
						} else {
							bodies.get(j).addBodyMass(bodies.get(i));
							collisions.add(new Collision(bodies.get(i), frame));
							bodies.remove(i);
							i--;
						}
					}
				}
			}
		}
		
		//Then, loop again and update the bodies using timestep dt
		bodies.get(0).resetForce(); //dont move the sun
		for (int i = 0; i < bodies.size(); i++) { 
			bodies.get(i).update(dt);
		}
	}	
	
	// Check if two bodies are visually collided
	private boolean collided(Body a, Body b){
		double x1 = a.rx*factor;
		double y1 = a.ry*factor;
		double x2 = b.rx*factor;
		double y2 = b.ry*factor;
		double dist = Math.abs(Math.sqrt(Math.pow((x2-x1), 2)+Math.pow(y2-y1, 2)));
		if(dist < (a.diameter + b.diameter)/2){
			return true;
		}
		return false;
	}
	
	/**
	 * Get the current collisions in the model. Collisions are removed from the list as time moves forward
	 * @return ArrayList of current collisions to be drawn on the GUI
	 */
	public ArrayList<Collision> getCollisions(){
		ArrayList<Collision> out = new ArrayList<Collision>();
		final int framesToShow = 50;
		for(Collision c : collisions){
			if(c.frame > frame - framesToShow){
				out.add(c);
			}
		}
		collisions = out;
		return out;
	}

}
