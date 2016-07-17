package model;

import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class BodySystem {

	public static final double solarmass=1.98892e30;
	public static final double earthmass = solarmass/333000.0;
	public static final double earthDistance = 1.47e17;

	protected static final double G = 6.673e-11;   // gravitational constant

	private double factor;
	private Canvas canvas;
	private ArrayList<Body> bodies, gravityBodies;
	private ArrayList<Collision> collisions; // Body's that has been removed in the last update due to collision with another body
	private int frame;

	public BodySystem(Canvas c){
		this.canvas = c;
		factor = (canvas.getWidth()/2)/1e18;
		frame = 0;
		collisions = new ArrayList<Collision>();
		resetBodies();
	}

	public void resetBodies(){		
		gravityBodies = new ArrayList<Body>();
		bodies = new ArrayList<Body>();
		
		double px = earthDistance; // Earth orbit distance from sun

		// Create the solar system (sun + planets)
		Star sun = new Star(0, 0, 0, 0, solarmass, Color.DARKORANGE);
		Planet mercury = new Planet(px*.38709, earthmass*.0553, Color.SILVER); // getBody(px*.38709, py*.38709, earthmass*0.0553, Color.SILVER);
		Planet venus = new Planet(px*.7233, earthmass*.815, Color.CYAN); // getBody(px*.7233, py*.7233, earthmass*0.815, Color.CYAN);
		Planet earth = new Planet(px, earthmass, Color.BLUE); // getBody(px, py, earthmass, Color.BLUE);
		Planet mars = new Planet(px*1.524, earthmass*.107, Color.RED); //getBody(px*1.524, py*1.524, earthmass*0.107, Color.RED);
		Planet jupiter = new Planet(px*5.203, earthmass*317.83, Color.YELLOW); //getBody(px*5.203, py*5.203, earthmass*317.83, Color.YELLOW);
		Planet saturn = new Planet(px*9.537, earthmass*95.162, Color.BURLYWOOD); // getBody(px*9.537, py*9.537, earthmass*95.162, Color.BURLYWOOD);
		Planet uranus = new Planet(px*19.2, earthmass*14, Color.CYAN); 
		Planet neptune = new Planet(px*30.1, earthmass*17, Color.BLUE);	
		gravityBodies.add(sun);
		gravityBodies.add(mercury);
		gravityBodies.add(venus);
		gravityBodies.add(earth);
		gravityBodies.add(mars);
		gravityBodies.add(jupiter);
		gravityBodies.add(saturn);
//		gravityBodies.add(uranus);
//		gravityBodies.add(neptune);
	}

	public Body getBody(double dist, double mass, Color c, String type){
		switch(type.toUpperCase()){
		case "PLANET":
			return new Planet(dist, mass, c);
		case "STAR":
			return new Star(dist, mass, c);
		case "ASTEROID":
			return new Asteroid(dist, mass, c);
		case "COMET":
			return new Comet(dist, mass, c);
		case "BLACK HOLE":
			return new BlackHole(dist, mass, c);
		default:
			return new Asteroid(dist, mass, c);
		}
	}

	public ArrayList<Body> getBodies(){
		return bodies;
	}

	public ArrayList<Body> getGravityBodies(){
		return gravityBodies;
	}

	public void addRandomBodies(int n){
		if(n > 125000){
			n = 125000;
		} else if (n < 0){
			n = 0;
		}
		for (int i = 0; i < n; i++) {
			double px = Math.abs(1e18*exp(-1.8)*(.5-Math.random())); // Exponential objects. More at the center
			double dist = Math.abs(Math.random()*earthDistance*9.6); //1.5e18); // Linear objects
			double mass = Math.abs(Math.random()*9.393e20*exp(1.8)); // Up to the mass of Ceres
			Color color = Color.ANTIQUEWHITE;
			bodies.add(new Asteroid(dist, mass, color));
		}
	}

	public synchronized void addBody(Body b){
		if(b instanceof Star || b instanceof Planet || b instanceof BlackHole){
			gravityBodies.add(b);
		} else {
			bodies.add(b);			
		}
	}

	private double exp(double lambda) {
		return -Math.exp(1 - Math.random()) / lambda;
	}

	public synchronized void updatePositions(double dt){
		if(frame == Integer.MAX_VALUE){
			collisions = new ArrayList<Collision>();
			frame = 0;
		}
		frame++;

		for (int i = 0; i < bodies.size(); i++) {
			bodies.get(i).resetForce();
			for (int j = 0; j < gravityBodies.size(); j++) {
				bodies.get(i).addForce(gravityBodies.get(j));
				// If two bodies collide then we merge them and keep the combined mass in a single item
				if(collided(bodies.get(i), gravityBodies.get(j))){ 
					gravityBodies.get(j).addBodyMass(bodies.get(i));
					collisions.add(new Collision(bodies.get(i), frame));
					bodies.remove(i);
					i--;						
				}
			}
		}

		for(int i = 0; i < gravityBodies.size(); i++){
			gravityBodies.get(i).resetForce();
			for (int j = 0; j < gravityBodies.size(); j++){
				if(i!=j){
					gravityBodies.get(i).addForce(gravityBodies.get(j));
					// If two bodies collide then we merge them and keep the combined mass in a single item
					if(collided(gravityBodies.get(i), gravityBodies.get(j))){
						System.err.println("Planet/Sun collision detected!");
						if(gravityBodies.get(i).mass < gravityBodies.get(j).mass){
							gravityBodies.get(j).addBodyMass(gravityBodies.get(i));
							collisions.add(new Collision(gravityBodies.get(i), frame));
							gravityBodies.remove(i);
							i--;
						} else {
							gravityBodies.get(i).addBodyMass(gravityBodies.get(j));
							collisions.add(new Collision(gravityBodies.get(j), frame));
							gravityBodies.remove(j);
							j--;
						}
					}
				}
			}
		}

		//Then, loop again and update the bodies using timestep dt
		for (Body b : bodies) { 
			b.update(dt);
		}
		for(Body b : gravityBodies){
			b.update(dt);
		}
	}	

	// Check if two bodies are visually collided
	private boolean collided(Body a, Body b){
		double x1 = a.rx;
		double y1 = a.ry;
		double x2 = b.rx;
		double y2 = b.ry;
		double dist = Math.abs(Math.sqrt(Math.pow((x2-x1), 2)+Math.pow(y2-y1, 2)));
//		System.out.println("Dist: " + dist);
		if(dist < 7e15){
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
		final int framesToShow = 5;
		for(Collision c : collisions){
			if(c.frame > frame - framesToShow){
				out.add(c);
			}
		}
		collisions = out;
		return out;
	}

}
