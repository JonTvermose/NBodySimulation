package application;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import files.FileLoader;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import model.BlackHole;
import model.Body;
import model.BodySystem;
import model.Collision;
import model.Comet;
import model.Star;
import sounds.MediaPlayerSupport;
import sounds.SoundLoader;

public class Controller {

	@FXML
	private Canvas canvas; // Draw on the canvas

	@FXML
	private Button stop; // Stop the simulation

	@FXML
	private TextField objects; // Enter the number of objects

	@FXML
	private Button start; // Start the simulation

	@FXML
	private Button reset; // Reset the simulation

	@FXML
	private Slider speed; // Set the delta time of the simulation

	@FXML
	private Slider zoom; // Set the zoom level of the Canvas
	
    @FXML
    private CheckBox showComets; // Show real Comets

    @FXML
    private CheckBox useRealAsteroids; // Use real Asteroids

	private GraphicsContext gc;
	private BodySystem sys;
	private SoundLoader soundLoader;
	private AnimationTimer at;

	private Image sun, black_hole; // GIF of rotating sun/black hole
	private MediaPlayer gameSoundPlayer, settingsSoundPlayer;

	private double translateX, translateY; 
	private double scale; // "Zoom" level of the canvas
	private boolean realAsteroids, realComets;

	@FXML
	void initialize(){	
		this.gc = canvas.getGraphicsContext2D();
		this.sys = new BodySystem();
		scale = 1e18;

		// Translate to center of canvas
		translateX = canvas.getWidth()/2.0;
		translateY = canvas.getHeight()/2.0;
		gc.translate(translateX, translateY);

		// Load the GIFs of the sun and Black Hole
		sun = new Image(Main.class.getResourceAsStream("/resources/sun_gif3.gif"));
		black_hole = new Image(Main.class.getResourceAsStream("/resources/giphy.gif"));

		// Load the sound files
		soundLoader = new SoundLoader();
		gameSoundPlayer = soundLoader.getGameSoundPlayer();
		settingsSoundPlayer = soundLoader.getSettingsSoundPlayer();
		MediaPlayerSupport.play(settingsSoundPlayer, 2000); // Start playing the intro music

		// Setup the speed/delta time slider
		speed.setMax(BodySystem.maxDeltaTime);
		speed.setMin(BodySystem.minDeltaTime);
		speed.setValue(sys.getDeltaTime());
		speed.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				sys.setDeltaTime((double) newValue);
			}
		});

		// Setup the zoom level slider
		zoom.setMax(5e18);
		zoom.setMin(1e11);
		zoom.setValue(scale);
		zoom.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				scale = (double) newValue;
			}
		});

		// Handle mouseclicks on the canvas
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {				
				// Transform mouseclick values to universe position
				double x1 = event.getX() - translateX;
				double y1 = event.getY() - translateY;
				Point2D p = transformToUniverse(new Point2D(x1, y1));
				// Add body at mouseclick
				Body b = new BlackHole(p.getX(), p.getY(), BodySystem.solarmass, Color.BLACK);
				sys.addBody(b);
				gc.drawImage(black_hole, x1-b.diameter/4, y1-b.diameter/4, b.diameter/2, b.diameter/2);	
				System.out.println("Mouseclick at: " + event.getX() + "," + event.getY());
			}
		});

		// Refresh the buttons
		this.stopSimulation(null); 
	}

	@FXML
	void startSimulation(ActionEvent event) {	
		// Deactivate button: start
		start.setDisable(true);

		// Deactivate button: Reset
		reset.setDisable(true);

		// Lock the textfield
		objects.setDisable(true);

		if(at==null){ // If no simulation has been started then:		
			// Read input_field and create bodies
			int n;
			try{
				n = Integer.parseInt(this.objects.getText());    		
			} catch (NumberFormatException e){
				n = 0;
			}
			sys.resetBodies();
			if(realAsteroids){
				sys.addKnownBodies(n);				
			} else {
				sys.addRandomBodies(n);				
			}

			// Change the music
			MediaPlayerSupport.changeMusic(settingsSoundPlayer, gameSoundPlayer, 1000);
		}

		// Activate button: stop
		stop.setDisable(false);

		// Activate slider: speed
		speed.setDisable(false);

		// Begin the simulation
		drawSimulation();
		System.err.println("Simulation started.");
	}


	/**
	 * Draw the model on the canvas in a JavaFX Application Thread (AnimationTimer)
	 */
	private void drawSimulation() {
		if(at==null){
			at = new AnimationTimer(){
				@Override
				public void handle(long now) {
					// Update the model
					Controller.this.sys.updatePositions(); 	

					// Clear the canvas
					gc.translate(-translateX, -translateY);
					gc.clearRect(-canvas.getWidth(), -canvas.getHeight(), canvas.getWidth()*2, canvas.getHeight()*2); 
					int totalBodies = 0;
					for(ArrayList<Body> bodies : sys.getBodies()){
						totalBodies += bodies.size();
					}
					String txt = "# of bodies: " + totalBodies;
					gc.setFill(Color.YELLOW);
					gc.fillText(txt, 20, 30);

					// Update the translate vector to match center of gravity
					//					Point2D gravityCenter = sys.getGravityCenter(canvas);
					//					translateX = canvas.getWidth()/2.0 - gravityCenter.getX();
					//					translateY = canvas.getHeight()/2.0 - gravityCenter.getY();
					gc.translate(translateX, translateY);

					// Draw the bodies (Comets & Asteroids)
					Point2D p;
					for(ArrayList<Body> bodies : sys.getBodies()){
						for(Body b : bodies){
							p = transformToPixels(b.rx, b.ry);
							if(b instanceof Comet){ // Comets are larger, and have a tail
								gc.setFill(b.color); 
								gc.fillOval(p.getX(), p.getY(), 8, 8); // Draw the body
								//							gc.setStroke(b.color);
								//							gc.setLineWidth(2);
								//							double dist = Math.sqrt(p.getX()*p.getX() + p.getY()*p.getY());
								//							double angle = Math.atan2(p.getY(), p.getX());
								//							double tailX = p.getX() + (Math.abs(canvas.getWidth()-dist)/50)*Math.cos(angle); // TODO - calculate based on distance to sun (center) and angle
								//							double tailY = p.getY() + (Math.abs(canvas.getHeight()-dist)/50)*Math.sin(angle); // TODO
								//							gc.strokeLine(p.getX()+4, p.getY()+4, tailX, tailY); // Draw the tail
							} else { // Asteroids							
								gc.setFill(b.color);
								gc.fillOval(p.getX()-b.diameter/2, p.getY()-b.diameter/2, b.diameter, b.diameter);
							}
						}
					}
					// Draw the gravity bodies
					for(Body b : sys.getGravityBodies()){
						p = transformToPixels(b.rx, b.ry);
						if(b instanceof Star){	
							gc.drawImage(sun, p.getX()-b.diameter/2, p.getY()-b.diameter/2, b.diameter, b.diameter);
//							System.out.println("Transformed coord: " + p.getX() + "," + p.getY());
//							System.out.println("Sun coord: " + b.rx + "," + b.ry);
						} else if (b instanceof BlackHole) {
							gc.drawImage(black_hole, p.getX()-b.diameter/4, p.getY()-b.diameter/4, b.diameter/2, b.diameter/2);				
						} else {
							gc.setFill(b.color);    		
							gc.fillOval(p.getX()-b.diameter/2, p.getY()-b.diameter/2, b.diameter, b.diameter);	
//							gc.fillOval(p.getX(), p.getY(), 1, 1);
						}
					}
					// Draw the collisions
					for(Collision b : sys.getCollisions()){
						p = transformToPixels(b.rx, b.ry);
						gc.setFill(Color.YELLOW);
						gc.fillOval(p.getX()-b.diameter/2, p.getY()-b.diameter/2, b.diameter*5, b.diameter*5);
					}		
				}
			};
		}
		at.start();
	}

	@FXML
	void stopSimulation(ActionEvent event) {
		// Stop the simulation
		if(at!=null){
			at.stop();			
		}
		// Deactivate button: Stop
		stop.setDisable(true);
		// Activate button: Start
		start.setDisable(false);
		// Deactivate slider: speed
		speed.setDisable(true);
		// Activate button: Reset
		reset.setDisable(false);
	}

	@FXML
	void resetSimulation(ActionEvent event) {	
		at = null;
		gc.translate(-translateX, -translateY);
		translateX = canvas.getWidth()/2.0;
		translateY = canvas.getHeight()/2.0;
		gc.translate(translateX, translateY);

		MediaPlayerSupport.changeMusic(gameSoundPlayer, settingsSoundPlayer, 1000);
		//		settingsSoundPlayer.play();
		//		MediaPlayerSupport.stop(gameSoundPlayer, 1000);

		// Unlock textfield: objects
		objects.setDisable(false);

		// Lock the reset field
		reset.setDisable(true);
	}
	

    @FXML
    void toggleRealAsteroids(ActionEvent event) {
    	realAsteroids = !realAsteroids;
    }

    @FXML
    void toggleComets(ActionEvent event) {
    	this.realComets = !realComets;
    	sys.setShowComets(realComets);
    }

	public void setTranslate(double translateX, double translateY) {
		//		this.translateX += translateX;
		//		this.translateY += translateY;
	}

	private Point2D transformToPixels(Point2D p){
		double x = p.getX()*(canvas.getWidth()/2)/scale;
		double y = p.getY()*(canvas.getHeight()/2)/scale;
		return new Point2D(x,y);
	}

	private Point2D transformToPixels(double x, double y){
		double x1 = x*(canvas.getWidth()/2)/scale;
		double y1 = y*(canvas.getHeight()/2)/scale;
		return new Point2D(x1,y1);
	}

	private Point2D transformToUniverse(Point2D p){
		double x = p.getX()/((canvas.getWidth()/2)/scale);
		double y = p.getY()/((canvas.getHeight()/2)/scale);
		return new Point2D(x,y);
	}

	private Point2D transformToUniverse(double x, double y){
		double x1 = x/((canvas.getWidth()/2)/scale);
		double y1 = y/((canvas.getHeight()/2)/scale);
		return new Point2D(x1,y1);
	}


}
