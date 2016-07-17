package sounds;

import javafx.animation.Transition;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MediaPlayerSupport {

	public static void play(final MediaPlayer mediaPlayer, final long fadeInMillis) {
		if (fadeInMillis > 0) {
			mediaPlayer.setVolume(0);
		}
		mediaPlayer.setOnReady(new Runnable() {

			@Override
			public void run() {
				mediaPlayer.play();
				new Transition() {
					{
						setCycleDuration(Duration.millis(fadeInMillis));
					}
					@Override
					protected void interpolate(double frac) {
						mediaPlayer.setVolume(frac);
					}
				}.play();
			}
		});
	}
	
	public static void stop(final MediaPlayer mediaPlayer, final long fadeInMillis) {
		double step = mediaPlayer.getVolume() / (fadeInMillis/10);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				while(mediaPlayer.getVolume() > 0.1){
					mediaPlayer.setVolume(mediaPlayer.getVolume() - step);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
				mediaPlayer.stop();
			}	
		});
		t.start();
	}

	public static void changeMusic(final MediaPlayer mediaPlayer1, final MediaPlayer mediaPlayer2, final long fadeInMillis) {
		long fade = fadeInMillis/2;
		double volume = mediaPlayer1.getVolume();
		if (fade > 0) {
			mediaPlayer2.setVolume(0);
		}
		double step = mediaPlayer1.getVolume() / (fade/10);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				while(mediaPlayer1.getVolume() > 0.1){
					mediaPlayer1.setVolume(mediaPlayer1.getVolume() - step);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
				mediaPlayer1.stop();
				mediaPlayer2.play();
				while(mediaPlayer2.getVolume() < volume){
					mediaPlayer2.setVolume(mediaPlayer2.getVolume() + step);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}	
		});
		t.start();
	}

}