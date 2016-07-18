package sounds;

import java.net.URL;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundLoader {
	
	public MediaPlayer getGameSoundPlayer(){
		URL soundFile = getClass().getResource("Space_Trip.mp3");
		Media hit = new Media(soundFile.toString());
		MediaPlayer gameSoundPlayer = new MediaPlayer(hit);
		gameSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Always loop

		return gameSoundPlayer;
	}
	
	public MediaPlayer getSettingsSoundPlayer(){
		URL soundFile = getClass().getResource("Cosmic_Messages.wav");
		Media hit = new Media(soundFile.toString());
		MediaPlayer settingsSoundPlayer = new MediaPlayer(hit);
		settingsSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Always loop
		return settingsSoundPlayer;
	}
	


}
