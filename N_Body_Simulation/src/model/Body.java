package model;

import java.awt.geom.Ellipse2D;

import javafx.scene.paint.Color;

public abstract class Body {

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

	public Body(double dist, double mass, Color color){
		double angle = Math.random()*Math.PI*2;
		rx = Math.cos(angle)*dist;
		ry = Math.sin(angle)*dist;

		double magv = circlev(rx,ry);

		double absangle = Math.atan(Math.abs(ry/rx));
		double thetav= Math.PI/2-absangle;
		vx = -1*Math.signum(ry)*Math.cos(thetav)*magv;
		vy = Math.signum(rx)*Math.sin(thetav)*magv;

		this.mass = mass;
		this.color = color;
		//		System.out.println("Pos: " + rx + "," + ry + " - Velocity: " + magv);
		addBodyMass(null);
	}

	public Body(double rx, double ry, double mass, Color color){
		this.rx = rx;
		this.ry = ry;
		if(mass!=0.0){
			this.mass = mass;	
		} else {
			this.mass = Math.random()*1e7;
		}
		this.color = color;
		double magv = circlev(rx,ry);

		double absangle = Math.atan(Math.abs(ry/rx));
		double thetav= Math.PI/2-absangle;
		vx = -1*Math.signum(ry)*Math.cos(thetav)*magv;
		vy = Math.signum(rx)*Math.sin(thetav)*magv;
		addBodyMass(null);
	}

	//the bodies are initialized in circular orbits around the central mass.
	//This is just some physics to do that
	public double circlev(double rx, double ry) {
		double r2=Math.sqrt(rx*rx+ry*ry);
		double numerator=BodySystem.G*BodySystem.solarmass;
		return Math.sqrt(numerator/r2);
	}	

	public void addBodyMass(Body b){
		// Adjust the size & color according to the bodys mass
		if(b!=null){
			mass += b.mass;			
		}
		diameter = (int) (Math.pow((((mass)/BodySystem.solarmass)/3.14)*(3.0/4.0), 1.0/3.0)*1500);
		if(diameter < 1){
			diameter = 1;	    	
		}
		if (mass >= BodySystem.solarmass/10){
			color = Color.DARKORANGE;
			diameter = diameter/30;
		} else if (mass > BodySystem.earthmass*5){
			diameter = diameter/5;
		}
		if(this instanceof BlackHole){
			diameter = diameter;
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
//		if(mass < BodySystem.earthmass){
//			Body c = new Asteroid(0, 0, Color.WHITE);
//			System.out.println("Distance: " + this.distanceTo(c)/BodySystem.earthDistance);
//		}
	}

	public Body(double semiMajor, double eccentricity, double longAscendNode, double argPeri, double mass, Color color, Body sun){
		this.mass = mass;
		this.color = color;
		if(semiMajor == 0.0 || sun == null){
			this.rx = 0.0;
			this.ry = 0.0;
			this.vx = 0.0;
			this.vy = 0.0;
		} else {			
			double semiMinor = semiMajor * Math.sqrt(1-eccentricity*eccentricity);
			double focalDistance = Math.sqrt((semiMajor*semiMajor - semiMinor*semiMinor));
			double degrees = Math.toRadians(longAscendNode + argPeri); // TODO
			double placement = 0; // Math.random()*Math.PI*2; // random placement in orbit // TODO - does not work!
			double rxTemp = semiMajor * Math.cos(placement) + focalDistance + sun.rx; // TODO - Substitute 0 with placement
			double ryTemp = semiMinor * Math.sin(placement) + sun.ry; // TODO - Substitute 0 with placement
			rx = rxTemp * Math.cos(degrees) - ryTemp * Math.sin(degrees); // Rotate
			ry = rxTemp * Math.sin(degrees) + ryTemp * Math.cos(degrees); // Rotate
			double radius = this.distanceTo(sun);
			double velocity = Math.sqrt((sun.mass*BodySystem.G)*((2.0/radius)-(1.0/semiMajor)));
//			double vAngle = Math.toDegrees(Math.PI/2-Math.atan(Math.abs(ry/rx)));
//			double vAngleTemp = -((semiMinor*Math.cos(placement))/(semiMajor*Math.sin(placement)));
//			System.out.println("vAngleTemp: " + vAngleTemp);
//			vx = -1*Math.signum(ry)*Math.cos(Math.toRadians(vAngle))*velocity;
//			vy = Math.signum(rx)*Math.sin(Math.toRadians(vAngle))*velocity;
//			System.out.println("Velocity: " + velocity);
//			System.out.println("Pos: " + rx + "," + ry + " - "+ vx + "," + vy);
//			tangent(semiMajor, semiMinor, rx, ry, velocity);
			double[] vRot = rotate(0,velocity,degrees);
			vx = vRot[0] + sun.vx;
			vy = vRot[1] + sun.vy;
		}
		this.addBodyMass(null);
	}
	
	public double[] rotate(double x, double y, double radians){
		double out[] = new double[2];
		out[0] = x * Math.cos(radians) - y * Math.sin(radians); 
		out[1] = x * Math.sin(radians) + y * Math.cos(radians); 
//		System.out.println("Rotated: " + out[0] + "," + out[1]);
		return out;
	}
	
	public double tangent(double semiMajor, double semiMinor, double x1, double y1, double v){
		double degrees = 0.0;
		double fx = -1 * (semiMajor*semiMajor) * ry;
		double fy = (semiMinor * semiMinor) * rx;
		double f = Math.sqrt(fx*fx + fy*fy);
		double tx = fx/f;
		double ty = fy/f;
//		System.out.println("Vinkel: " + Math.toDegrees(Math.acos(tx)));
//		System.out.println("Velocity Vector: (" + vx + "," + vy + ")");
//		return Math.atan2(ty, tx);
		double y;
		double yFinal, xFinal;
		if(y1 < 0.000000000001){
			y = y1;
			yFinal = v;
			xFinal = 0;
		} else {
			y = -(semiMinor*semiMinor*x1)/(semiMajor*semiMajor*y1);			
			double x = 1;
			double len = Math.sqrt(x*x + y*y);
			yFinal = (y/len) * v;
			xFinal = (x/len) * v;
		}
		vx = -1*Math.signum(ry)*xFinal;
		vy = Math.signum(rx)*yFinal;
//		System.out.println("Pos2: " + rx + "," + ry + " - "+ vx + "," + vy);
//		System.out.println("Slope: " + y);
//		System.out.println("VLength: " + Math.sqrt(vx*vx+vy*vy));
		return degrees;
	}

}
