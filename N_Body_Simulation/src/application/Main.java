package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Simulation.fxml"));
			AnchorPane root = (AnchorPane) loader.load();
			Controller controller = loader.getController();
			Scene scene = new Scene(root,1200,1000);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			scene.setOnKeyPressed(new EventHandler<KeyEvent>(){

				@Override
				public void handle(KeyEvent event) {
					double translateX = 0.0, translateY = 0.0;
					double step = 20.0;

					switch(event.getCode()){
					case UP: translateY = -step; break;
					case DOWN: translateY = step; break;
					case LEFT: translateX = -step; break;
					case RIGHT: translateX = step; break;
					}
					controller.setTranslate(translateX, translateY);

				}
			});
			primaryStage.setScene(scene);
			primaryStage.show();


		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
