package files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javafx.scene.paint.Color;
import model.Asteroid;
import model.Body;
import model.BodySystem;
import model.Comet;

public class FileLoader {
	
	private boolean cometLoadDone, asteroidLoadDone;
	private ArrayList<Body> cometList;
	private ArrayList<Asteroid> asteroidsList;
	private InputStream cometStream, asteroidStream;
	
	public FileLoader(Body center){
		cometStream = this.getClass().getClassLoader().getResourceAsStream("resources/comets.txt");
		asteroidStream = this.getClass().getClassLoader().getResourceAsStream("resources/asteroids.txt");
		cometList = new ArrayList<Body>();
		asteroidsList = new ArrayList<Asteroid>();
		this.loadComets(center);
		this.loadAsteroids(center);
	}
	
	private void loadComets(Body center){
		cometLoadDone = false;
		new Thread(new Runnable(){

			@Override
			public void run() {
				long start = System.currentTimeMillis();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(cometStream))) {
				    String line;
				    
				    while ((line = br.readLine()) != null) {
				       String[] s = line.split(",");
				       if(s.length==6){
				    	   double a = Double.parseDouble(s[2])*BodySystem.earthDistance;
				    	   double e = Double.parseDouble(s[1]);
				    	   double om = Double.parseDouble(s[3]);
				    	   double w = Double.parseDouble(s[4]);
				    	   double radius = Double.parseDouble(s[5])/2;
				    	   double volume = (4/3)*Math.PI*Math.pow(radius*1000, 3);
				    	   double mass = (0.6/1000) * volume;
				    	   cometList.add(new Comet(a, e, om, w, mass, Color.WHITE, center));
				       } else if (s.length == 5){
				    	   double a = Double.parseDouble(s[2])*BodySystem.earthDistance;
				    	   double e = Double.parseDouble(s[1]);
				    	   double om = Double.parseDouble(s[3]);
				    	   double w = Double.parseDouble(s[4]);
				    	   double radius = Math.random()*50; // No data on the comet size
				    	   double volume = (4/3)*Math.PI*Math.pow(radius*1000, 3);
				    	   double mass = (0.6/1000) * volume;
				    	   cometList.add(new Comet(a, e, om, w, mass, Color.WHITE, center));
				       } else {
				    	   System.err.println("Comets.txt format exception.");
				       }
				    }
				    cometLoadDone = true;
				    System.out.println(cometList.size() + " Comets loaded in " + (System.currentTimeMillis()-start) +"ms...");
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}).start();;	
	}
	
	private void loadAsteroids(Body center){
		asteroidLoadDone = false;
		new Thread(new Runnable(){
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(asteroidStream))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				       String[] s = line.split(",");
				       if(s.length==6){
				    	   double a = Double.parseDouble(s[2])*BodySystem.earthDistance;
				    	   double e = Double.parseDouble(s[1]);
				    	   double om = Double.parseDouble(s[3]);
				    	   double w = Double.parseDouble(s[4]);
				    	   double radius = Double.parseDouble(s[5])/2;
				    	   double volume = (4/3)*Math.PI*Math.pow(radius*1000, 3);
				    	   double mass = (0.6/1000) * volume;
				    	   asteroidsList.add(new Asteroid(a, e, om, w, mass, Color.WHITE, center));
				       } else if (s.length == 5){
				    	   double a = Double.parseDouble(s[2])*BodySystem.earthDistance;
				    	   double e = Double.parseDouble(s[1]);
				    	   double om = Double.parseDouble(s[3]);
				    	   double w = Double.parseDouble(s[4]);
				    	   double radius = Math.random()*50; // No data on the comet size
				    	   double volume = (4/3)*Math.PI*Math.pow(radius*1000, 3);
				    	   double mass = (0.6/1000) * volume;
				    	   asteroidsList.add(new Asteroid(a, e, om, w, mass, Color.WHITE, center));
				       } else {
				    	   System.err.println("Asteroids.txt format exception.");
				       }
				    }
				    asteroidLoadDone = true;
				    System.out.println(asteroidsList.size() + " Asteroids loaded in " + (System.currentTimeMillis()-start) +"ms...");
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}).start();;	
	}
	
	public ArrayList<Body> getCometList(){
		while(!cometLoadDone){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			return cometList;
	}
	
	public ArrayList<Asteroid> getAsteroidList(){
		while(!asteroidLoadDone){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return asteroidsList;
	}

}
