package application;

import java.io.File;
import java.net.URL;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import model.Asteroid;
import model.BlackHole;
import model.Body;
import model.BodySystem;
import model.Collision;
import model.Comet;
import model.Star;
import sounds.MediaPlayerSupport;

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

	private GraphicsContext gc;
	private BodySystem sys;
	private double dt; // delta time between physics calculation
	private AnimationTimer at;

	private Image sun, black_hole; // Gif of rotating sun
	private MediaPlayer gameSoundPlayer, settingsSoundPlayer;

	@FXML
	void initialize(){
		this.gc = canvas.getGraphicsContext2D();
		this.sys = new BodySystem(canvas);

		// Translate to center of canvas
		gc.translate(canvas.getWidth()/2, canvas.getHeight()/2);

		// Load the GIF of the sun
		File file = new File("sun_gif3.gif");
		sun = new Image(file.toURI().toString());
		File file2 = new File("darkhole_gif.gif");
		black_hole = new Image(file2.toURI().toString());

		// Load the sound files
		URL soundFile = getClass().getResource("Space_Trip.mp3");
		Media hit = new Media(soundFile.toString());
		gameSoundPlayer = new MediaPlayer(hit);
		gameSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Always loop
		URL soundFile2 = getClass().getResource("Cosmic_Messages.wav");
		Media hit2 = new Media(soundFile2.toString());
		settingsSoundPlayer = new MediaPlayer(hit2);
		settingsSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Always loop
		MediaPlayerSupport.play(settingsSoundPlayer, 2000); // Start playing the intro music

		// Setup the speed/delta time slider
		dt = 1e13;
		speed.setMax(5e14);
		speed.setMin(1e12);
		speed.setValue(dt);
		speed.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				dt = (double) newValue;
			}
		});

		// Handle mouseclicks on the canvas
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {				
				// Calculate distance from mouseclick to the sun (center of canvas)
				double x1 = event.getX();
				double y1 = event.getY();
				double x2 = canvas.getWidth()/2;
				double y2 = canvas.getHeight()/2;
				double dist = Math.abs(Math.sqrt(Math.pow((x2-x1), 2)+Math.pow(y2-y1, 2)));
				System.out.println("Mouseclick at: " + x1 + ", " + y1 + " - Distance to sun: " + dist);

				// add planet with calculated distance
				sys.addBody(sys.getBody(dist*0.0135e17, BodySystem.solarmass, Color.BLACK, "BLACK HOLE"));
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

		if(at==null){ // If no simulation has been started		
			// Read input_field and create bodies
			int n;
			try{
				n = Integer.parseInt(this.objects.getText());    		
			} catch (NumberFormatException e){
				n = 0;
			}
			sys.resetBodies();
			sys.addRandomBodies(n);

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
	protected void drawSimulation() {
		if(at==null){
			at = new AnimationTimer(){
				@Override
				public void handle(long now) {
					Controller.this.updateSimulation();
					gc.translate(-canvas.getWidth()/2.0, -canvas.getHeight()/2.0);
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear the canvas
					String txt = "# of bodies: " + sys.getBodies().size();
					gc.fillText(txt, 20, 30);
					//				Body center = sys.getGravityBodies().get(0);
					//				for(Body b : sys.getGravityBodies()){
					//					if(b.mass > center.mass){
					//						center = b;
					//					}
					//				}
					//				x = Math.round(center.rx*(canvas.getWidth()/2.0)/1e18);
					//				y = Math.round(center.ry*(canvas.getWidth()/2.0)/1e18);
					//				System.out.println("Translated: " + x + ", " + y);
					gc.translate(canvas.getWidth()/2.0, canvas.getHeight()/2.0);

					// Draw the bodies
					for(Body b : sys.getBodies()){
						if(b instanceof Comet){
							gc.setFill(b.color); 
							gc.setStroke(b.color);
							gc.setLineWidth(2);
							double x = b.rx*(canvas.getWidth()/2)/1e18-4;
							double y = b.ry*(canvas.getHeight()/2)/1e18-4;
							gc.fillOval(x, y, 8, 8);
							double dist = Math.sqrt(x*x+y*y);
							double angle = Math.atan2(y,x);
							double tailX = dist*Math.cos(Math.toRadians(angle)); // TODO - calculate based on distance to sun (center) and angle
							double tailY = dist*Math.sin(Math.toRadians(angle)); // TODO
							gc.strokeLine(x+4, y+4, tailX, tailY);

						} else {							
							gc.setFill(b.color);    		
							gc.fillOval(b.rx*(canvas.getWidth()/2)/1e18-b.diameter/2, b.ry*(canvas.getHeight()/2)/1e18-b.diameter/2, b.diameter, b.diameter);
						}
					}

					// Draw the gravity bodies
					for(Body b : sys.getGravityBodies()){
						if(b instanceof Star){	
							// Draw the sun(s)
							gc.drawImage(sun, b.rx*(canvas.getWidth()/2)/1e18-b.diameter/2, b.ry*(canvas.getHeight()/2)/1e18-b.diameter/2, b.diameter, b.diameter);
						} else if (b instanceof BlackHole) {
							gc.drawImage(black_hole, b.rx*(canvas.getWidth()/2)/1e18-b.diameter/4, b.ry*(canvas.getHeight()/2)/1e18-b.diameter/4, b.diameter/2, b.diameter/2);
							gc.setFill(Color.BLACK);
							gc.fillOval(b.rx*(canvas.getWidth()/2)/1e18-b.diameter/2, b.ry*(canvas.getHeight()/2)/1e18-b.diameter/2, b.diameter, b.diameter);					

						} else {
							gc.setFill(b.color);    		
							gc.fillOval((int) Math.round(b.rx*(canvas.getWidth()/2)/1e18)-b.diameter/2, (int) Math.round(b.ry*(canvas.getHeight()/2)/1e18)-b.diameter/2, b.diameter, b.diameter);					
						}
					}

					// Draw the collisions
					for(Collision b : sys.getCollisions()){
						gc.setFill(Color.YELLOW);
						gc.fillOval(b.rx*(canvas.getWidth()/2)/1e18-b.diameter/2, b.ry*(canvas.getHeight()/2)/1e18-b.diameter/2, b.diameter*5, b.diameter*5);
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
	void setSimulationSpeed(ActionEvent event) {	}

	@FXML
	void resetSimulation(ActionEvent event) {
		at = null;

		MediaPlayerSupport.changeMusic(gameSoundPlayer, settingsSoundPlayer, 1000);
		//		settingsSoundPlayer.play();
		//		MediaPlayerSupport.stop(gameSoundPlayer, 1000);

		// Unlock textfield: objects
		objects.setDisable(false);
	}

	/**
	 * Update the model
	 */
	private void updateSimulation(){
		sys.updatePositions(dt); // Update the model
	}

}
