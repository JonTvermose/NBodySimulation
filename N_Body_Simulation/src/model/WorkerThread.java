package model;

import java.util.ArrayList;

public class WorkerThread implements Runnable{
	
	private int frame;
	private double deltaTime;
	private ArrayList<Body> bodies, gravityBodies;
	private ArrayList<Collision> collisions;
	
	public WorkerThread(ArrayList<Body> bodies, ArrayList<Body> gravityBodies, double dt, int frame, int i, BodySystem sys){
		this.bodies = bodies;
		this.gravityBodies = gravityBodies;
		this.deltaTime = dt;
		this.frame = frame;
		collisions = sys.getCollisionsList(i);
	}

	@Override
	public void run() {
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
					if(i < 0){
						i=0;
					}
				}
			}
		}
		
		//Then, loop again and update the bodies using timestep dt
		for (Body b : bodies) { 
			b.update(deltaTime);
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

}
