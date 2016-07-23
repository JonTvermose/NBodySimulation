package model;

import java.util.ArrayList;

import files.FileLoader;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class BodySystem {

	public static final double solarmass=1.98892e30;
	public static final double earthmass = solarmass/333000.0;
	public static final double earthDistance = 1.496e17;

	public static final double G = 6.673e-11;   // gravitational constant
	public static final double maxDeltaTime = 5e14; // Maximum delta time between calculations
	public static final double minDeltaTime = 1e11; // Minimum delta time between calculations
	public static final int MAX_OBJECTS = 710000; // Maximum number of objects in the system

	private FileLoader fileLoader;
	private double deltaTime = 1e13;
	private ArrayList<Body> bodies, gravityBodies;
	private ArrayList<Collision> collisions; // Bodies that has been removed in the last update due to collision with another body
	private int frame;

	private final int CORES = Runtime.getRuntime().availableProcessors(); // Estimated number of cores (may include virtual cores)
	private final int OPTIMALTHREADCOUNT = CORES*2; // Benchmarking shows better performance at more threads pr. core

	private ArrayList<ArrayList<Body>> bodiesList; // List containing the 4 lists of non gravity bodies
	private ArrayList<ArrayList<Collision>> collisionsList;
	private ArrayList<Body> comets;
	private boolean showComets, enableCollisions;

	private int updates; // For performance-testing
	private long avgTime; // For performance-testing

	public BodySystem(){
		frame = 0;
		collisionsList = new ArrayList<ArrayList<Collision>>();
		resetBodies();
	}

	public void resetBodies(){	
		updates = 0;
		avgTime = 0;
		bodiesList = new ArrayList<ArrayList<Body>>();
		gravityBodies = new ArrayList<Body>();

		// Create the solar system (sun + planets)
		Star sun = new Star(0.0, 0.0, 0.0, 0.0, solarmass, Color.DARKORANGE, null);
		Planet mercury = new Planet(earthDistance*.38709, 0.20563, 48.3, 29.1, earthmass*.0553, Color.SILVER, sun); // getBody(px*.38709, py*.38709, earthmass*0.0553, Color.SILVER);
		Planet venus = new Planet(earthDistance*.7233, 0.006772, 76.68, 54.884, earthmass*.815, Color.CYAN, sun); // getBody(px*.7233, py*.7233, earthmass*0.815, Color.CYAN);
		Planet earth = new Planet(earthDistance, 0.0167086, 174.9, 288.1, earthmass, Color.BLUE, sun);
		Planet moon = new Planet(earthDistance*0.00384748, 0.0549, 0, 0, earthmass*0.0012, Color.SILVER, earth);
		Planet mars = new Planet(earthDistance*1.524, 0.0934, 49.558, 286.502, earthmass*.107, Color.RED, sun); //getBody(px*1.524, py*1.524, earthmass*0.107, Color.RED);
		Planet jupiter = new Planet(earthDistance*5.203, 0.048498, 100.464, 273.867, earthmass*317.83, Color.YELLOW, sun); //getBody(px*5.203, py*5.203, earthmass*317.83, Color.YELLOW);
		Planet saturn = new Planet(earthDistance*9.537, 0.05555, 113.665, 339.392, earthmass*95.162, Color.BURLYWOOD, sun); // getBody(px*9.537, py*9.537, earthmass*95.162, Color.BURLYWOOD);
		Planet uranus = new Planet(earthDistance*19.2, 0.046381, 74.006, 96.998857, earthmass*14, Color.CYAN, sun); 
		Planet neptune = new Planet(earthDistance*30.1, 0.009456, 131.784, 276.336, earthmass*17, Color.BLUE, sun);	
		gravityBodies.add(sun);
		gravityBodies.add(mercury);
		gravityBodies.add(venus);
		gravityBodies.add(earth);
		gravityBodies.add(moon);
		gravityBodies.add(mars);
		gravityBodies.add(jupiter);
		gravityBodies.add(saturn);
		gravityBodies.add(uranus);
		gravityBodies.add(neptune);

		fileLoader = new FileLoader(sun); // Load know comets and asteroids
		comets = fileLoader.getCometList();
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

	public ArrayList<ArrayList<Body>> getBodies(){
		return bodiesList;
	}

	public ArrayList<Body> getGravityBodies(){
		return gravityBodies;
	}

	public void addRandomBodies(int n){
		if(n > MAX_OBJECTS){
			n = MAX_OBJECTS;
		} else if (n < 0){
			n = 0;
		}	
		for(int r = 0; r < OPTIMALTHREADCOUNT; r++){
			bodies = new ArrayList<Body>();
			collisions = new ArrayList<Collision>();
			for (int i = 0; i < n/OPTIMALTHREADCOUNT; i++) {
//				double px = Math.abs(1e18*exp(-1.8)*(.5-Math.random())); // Exponential objects. More at the center
				double dist = Math.abs(Math.random()*earthDistance*40); //1.5e18); // Linear objects
				double mass = Math.abs(Math.random()*9.393e20*exp(1.8)); // Up to the mass of Ceres
				Color color = Color.ANTIQUEWHITE;
				bodies.add(new Asteroid(dist, mass, color));
			}
			this.bodiesList.add(bodies);
			this.collisionsList.add(collisions);
		}
		if(this.showComets){
			bodiesList.add(comets);			
		}
	}


	public void addKnownBodies(int n){
		ArrayList<Asteroid> asteroids = fileLoader.getAsteroidList();
		if(n > MAX_OBJECTS){
			n = MAX_OBJECTS;
		} else if (n < 0){
			n = 0;
		}	
		for(int r = 0; r < OPTIMALTHREADCOUNT; r++){
			bodies = new ArrayList<Body>();
			collisions = new ArrayList<Collision>();
			for (int i = 0; i < n/OPTIMALTHREADCOUNT; i++) {
				bodies.add(asteroids.get((n/OPTIMALTHREADCOUNT)*r + i));
			}
			this.bodiesList.add(bodies);
			this.collisionsList.add(collisions);
		}
		if(this.showComets){
			bodiesList.add(comets);			
		}
	}

	private void addComets(int n){
		for(int i=0; i<n; i++){
			double dist = Math.abs(Math.random()*earthDistance*9.6); //1.5e18); // Linear objects
			double mass = Math.abs(Math.random()*9.393e20*exp(1.8)); // Up to the mass of Ceres
			Color color = Color.ANTIQUEWHITE;
			bodies.add(new Comet(dist, mass, color));
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

	public synchronized void updatePositions(){
		long start = System.currentTimeMillis();
		if(frame == Integer.MAX_VALUE){
			collisions = new ArrayList<Collision>();
			frame = 0;
		}
		frame++;

		// Assign data and work to workerthreads. They will update all non-gravity bodies in the system
		Thread workers[] = new Thread[OPTIMALTHREADCOUNT];		
		for(int i=0; i< OPTIMALTHREADCOUNT; i++){
			workers[i]= new Thread(new WorkerThread(bodiesList.get(i), gravityBodies, this.deltaTime, this.frame, i, this, enableCollisions));
			workers[i].start();
		}

		// Update the gravitybodies
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

		for(Body b : gravityBodies){
			b.update(deltaTime);
		}

		if(showComets){
			// Update the comets
			for(int i = 0; i < comets.size(); i++){
				comets.get(i).resetForce();
				for (int j = 0; j < gravityBodies.size(); j++){
					if(i!=j){
						comets.get(i).addForce(gravityBodies.get(j));
						// If two bodies collide then we merge them and keep the combined mass in a single item
						if(collided(comets.get(i), gravityBodies.get(j))){
							System.err.println("Comet collision detected!");
							if(comets.get(i).mass < gravityBodies.get(j).mass){
								gravityBodies.get(j).addBodyMass(comets.get(i));
								collisions.add(new Collision(comets.get(i), frame));
								comets.remove(i);
								i--;
							} else {
								comets.get(i).addBodyMass(gravityBodies.get(j));
								collisions.add(new Collision(gravityBodies.get(j), frame));
								gravityBodies.remove(j);
								j--;
							}
						}
					}
				}
			}

			for(Body b : comets){
				b.update(deltaTime);
			}
		}

		// Wait for each workerthread to finish
		for(int i=0; i<OPTIMALTHREADCOUNT; i++){
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.updates++;
		this.avgTime += System.currentTimeMillis() - start;
		System.out.println("Update time: " + (avgTime/updates) + " - OPTIMALTHREADCOUNT: " + OPTIMALTHREADCOUNT);
	}	

	public synchronized void setDeltaTime(double dt){
		if(dt <= BodySystem.maxDeltaTime && dt >= BodySystem.minDeltaTime){
			this.deltaTime = dt;			
		}
	}

	// Check if two bodies are visually collided
	private boolean collided(Body a, Body b){
		if(!enableCollisions){
			return false;
		}
		double dist = Math.abs(Math.sqrt(Math.pow((b.rx-a.rx), 2)+Math.pow(b.ry-a.ry, 2)));
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
		//		final int framesToShow = 5;
		//		for(ArrayList<Collision> collisions : collisionsList){
		//			for(Collision c : collisions){
		//				if(c.frame > frame - framesToShow){
		//					out.add(c);
		//				}
		//			}
		//			collisions = out;
		//		}
		return out;
	}

	public double getDeltaTime() {
		return this.deltaTime;
	}

	public Point2D getGravityCenter(Canvas canvas) {
		double x = 0, y = 0;
		double totalMass = 0;
		for(Body b : this.gravityBodies){
			x += (b.rx *(canvas.getWidth()/2)/1e18) * b.mass;
			y += (b.ry *(canvas.getWidth()/2)/1e18) * b.mass;
			totalMass += b.mass;
		}
		return new Point2D(x/totalMass, y/totalMass);
	}

	public ArrayList<Collision> getCollisionsList(int i) {
		return collisionsList.get(i);
	}
	
	public void setShowComets(boolean c){
		this.showComets = c;
	}

	public void setCollisions(boolean c) {
		this.enableCollisions = c;
	}

}
