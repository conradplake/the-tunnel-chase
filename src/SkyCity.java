import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.File;

import gframe.app.DoubleBufferedFrame;
import gframe.engine.Camera;
import gframe.engine.Engine3D;
import gframe.engine.FlatShader;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.PhongShader;
import gframe.engine.Point3D;
import gframe.engine.Toolbox;
import gframe.engine.generator.NoiseGenerator;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SkyCity extends DoubleBufferedFrame {

	public static int SCREENX = 800;
	public static int SCREENY = 600;

	private Engine3D engine;
	private Camera camera;
	private Lightsource lightsource;

	private ImageRaster frame;
	
	private MediaPlayer mediaPlayer;

	private FlyoverCity city;

	static float[] filterKernel = new float[11];
	static {
		for (int i = 0; i < filterKernel.length; i++) {
			filterKernel[i] = 1f / (float) filterKernel.length;
		}
	}

	public static void main(String[] args) {
		new SkyCity().start();
	}

	public SkyCity() {
		super();
		setBackground(Color.darkGray);
		setResizable(false);
		frame = new ImageRaster(SCREENX, SCREENY);
	
		new JFXPanel(); // init java fx for audio playback
	}

	public void start() {
		initEngine();

		// -- DISPLAY MODE SETTINGS
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		// switch off fullscreen
		if (false && device.isFullScreenSupported()) {
			DisplayMode newMode = new DisplayMode(SCREENX, SCREENY, 32, 60);
			this.setUndecorated(true);
			this.setResizable(false);
			// this.setIgnoreRepaint(true);
			device.setFullScreenWindow(this);
			device.setDisplayMode(newMode);
		} else {
			setSize(SCREENX, SCREENY);
			setLocation(20, 0);
			setLayout(null);
			setVisible(true);
		}

		enableEvents(AWTEvent.KEY_EVENT_MASK);

		startFreeFlow();
	}

	private void startFreeFlow() {

		// 133 bpm
		mediaPlayer = new MediaPlayer(new Media(new File("audio/synthwave-overdrive.mp3").toURI().toString()));
		mediaPlayer.setVolume(0.66);

		city = new FlyoverCity(150, 150);
		city.move(-city.cityWidth/2, 0, 0);
		engine.register(city, new FlatShader(lightsource));

		LightBeams lightBeams = new LightBeams(12500);
		lightBeams.move(0, 0, city.cityDepth);
		engine.register(lightBeams, new PhongShader(lightsource));

		lightsource.x = 0;
		lightsource.y = 20000;
		lightsource.z = -1000;

		camera.reset();
		camera.move(new Point3D(0, 700, -400));

		long startTimeInMillis = System.currentTimeMillis();
		long totalTimePassedInMillis = 0;
		mediaPlayer.play();
		
		float noiseOffset = 0;
		while (true) {
			repaint();
			
			float noise = (float)Toolbox.map(NoiseGenerator.improvedPerlinNoise(noiseOffset, 0), -1, 1, 400, 1000);
			camera.getOrigin().y = noise;
			noiseOffset += 0.0002;
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}
			totalTimePassedInMillis = (System.currentTimeMillis() - startTimeInMillis);
			if (totalTimePassedInMillis > 234000) {
				break;
			}
		}
		exit();
	}

	private void initEngine() {
		engine = new Engine3D(SCREENX, SCREENY);

		Lightsource.AMBIENT_LIGHT_INTENSITY = 0;
		lightsource = new Lightsource(0, 0, 0, Color.WHITE, Lightsource.MAX_INTENSITY);
		engine.setLightsource(lightsource);

		camera = new Camera();
		camera.move(0, 0, -1000);
		engine.setCamera(camera);

		engine.setDefaultShader(new PhongShader(lightsource));
	}

	protected void processKeyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_PRESSED) {
			int keycode = event.getKeyCode();
			if (keycode == KeyEvent.VK_ESCAPE) {
				exit();
			} else {
				
				if (keycode == KeyEvent.VK_F3) {
					engine.shadingEnabled = !engine.shadingEnabled;
				}

				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-10, 0, 0);
				} else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(10, 0, 0);
				} else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 10, 0);
				} else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, -10, 0);
				} else if (keycode == KeyEvent.VK_PAGE_UP) {
					lightsource.move(0, 0, 10);
				} else if (keycode == KeyEvent.VK_PAGE_DOWN) {
					lightsource.move(0, 0, -10);
				} 
				
				else if (keycode == KeyEvent.VK_Y) {
					camera.rotate(0, 0, 1);
				} else if (keycode == KeyEvent.VK_X) {
					camera.rotate(0, 0, -1);
				}
			}
		}
		super.processKeyEvent(event);
	}

	@Override
	public void paint(Graphics g) {
		long updateTime = System.currentTimeMillis();

		engine.drawScene(frame);
		ImageRaster finalFrame = addFilterEffect(frame);
		g.drawImage(finalFrame.createImage(), 0, 0, finalFrame.getWidth(), finalFrame.getHeight(), null);

		updateTime = System.currentTimeMillis() - updateTime;
		if (updateTime < 33) { // cap at 33ms ~ 30 FPS
			try {
				Thread.sleep(33 - updateTime);
				updateTime = 33;
			} catch (InterruptedException ie) {
			}
		}
	}

	private ImageRaster addFilterEffect(ImageRaster inputFrame) {
		ImageRaster result = inputFrame.copy();

		int halfFilterLength = filterKernel.length / 2;

		for (int y = 0; y < inputFrame.getHeight(); y++) {
			for (int x = halfFilterLength; x < inputFrame.getWidth() - halfFilterLength; x++) {
				
				float newRed = 0;
				float newGreen = 0;
				float newBlue = 0;

				for (int i = 0; i < filterKernel.length; i++) {
					int rgb = inputFrame.getPixel(x + i - halfFilterLength, y);
					newRed += filterKernel[i] * ((rgb >> 16) & 0xff);
					newGreen += filterKernel[i] * ((rgb >> 8) & 0xff);
					newBlue += filterKernel[i] * ((rgb) & 0xff);
				}
				
				int newRgb = ((255 & 0xFF) << 24) | (((int) newRed & 0xFF) << 16) | (((int) newGreen & 0xFF) << 8)
						| (((int) newBlue & 0xFF) << 0);
				
				result.setPixel(x, y, newRgb);
			}
		}
		return result;
	}

	private void exit() {
		mediaPlayer.stop();
		System.exit(0);
	}

}
