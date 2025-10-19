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
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.PhongShader;
import gframe.engine.Toolbox;
import gframe.engine.generator.NoiseGenerator;
import gframe.parser.WavefrontObjParser;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Main extends DoubleBufferedFrame {

	private int screenX;
	private int screenY;

	private Engine3D engine;
	private Camera camera;
	private Lightsource lightsource;

	private ImageRaster frame;

	private MediaPlayer mediaPlayer;

	static float[] filterKernel = new float[7];
	static {
		for (int i = 0; i < filterKernel.length; i++) {
			filterKernel[i] = 1f / (float) filterKernel.length;
		}
	}

	public static void main(String[] args) {
		int screenX = 640;
		int screenY = 400;
		if(args.length == 2) {
			screenX = Integer.parseInt(args[0]);
			screenY = Integer.parseInt(args[1]);
		}
		new Main(screenX, screenY).start();
	}

	public Main(int screenX, int screenY) {
		super();
		this.screenX = screenX;
		this.screenY = screenY;
		setBackground(Color.BLACK);
		setResizable(false);
		frame = new ImageRaster(screenX, screenY);

		new JFXPanel(); // init java fx for audio playback
	}

	public void start() {
		initEngine();
		
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			DisplayMode newMode = new DisplayMode(screenX, screenY, 32, 60);
			this.setUndecorated(true);
			this.setResizable(false);
			device.setFullScreenWindow(this);
			device.setDisplayMode(newMode);
		} else {
			setSize(screenX, screenY);
			setLocation(20, 0);
			setLayout(null);
			setVisible(true);
		}

		enableEvents(AWTEvent.KEY_EVENT_MASK);

		startFreeFlow();
	}

	private void startFreeFlow() {

		mediaPlayer = new MediaPlayer(new Media(new File("audio/star-wars-style-chase-music.mp3").toURI().toString()));
		mediaPlayer.setVolume(0.66);

		Tunnel tunnel = new Tunnel(500, 6000);
		tunnel.move(0, 0, 0);
		engine.register(tunnel, new PhongShader(lightsource));

		Model3D falcon = WavefrontObjParser.parse(new File("models/Millennium_Falcon.obj"), Color.white);
		falcon.scale(0.1f, 0.1f, 0.1f);
		falcon.move(0, 0, 50);
		engine.register(falcon, new PhongShader(lightsource));
		
		Model3D tieFighter = WavefrontObjParser.parse(new File("models/Tie_Fighter.obj"), Color.darkGray);
		tieFighter.scale(2f, 2f, 2f);
		tieFighter.move(0, 0, 500);
		engine.register(tieFighter, new PhongShader(lightsource));

		lightsource.x = 0;
		lightsource.y = 0;
		lightsource.z = -1000;
		lightsource.setAddAttenuation(true);
		lightsource.setLightAttenuationFalloffFactor(0.0000001f);

		long startTimeInMillis = System.currentTimeMillis();
		long totalTimePassedInMillis = 0;

		float noiseYOffset = 0;
		float noiseXOffset = 0;
		
		mediaPlayer.play();
		while (true) {
			repaint();

			float noiseX = (float) Toolbox.map(NoiseGenerator.improvedPerlinNoise(noiseXOffset, 0), -1, 1, -100, 100);
			float noiseY = (float) Toolbox.map(NoiseGenerator.improvedPerlinNoise(0, noiseYOffset), -1, 1, -100, 100);
			camera.getOrigin().x = noiseX;
			camera.getOrigin().y = noiseY;
			noiseXOffset += 0.001;
			noiseYOffset += 0.001;

			lightsource.setLightAttenuationFalloffFactor((float) Toolbox.map(noiseX, -100, 100, 0.00000001f, 0.0000003f));

			falcon.rotate(0, 0, (float)Toolbox.map(noiseY, -100, 100, -1.5f, 1.5f));
			falcon.move(0, 0, (float)Toolbox.map(noiseX, -100, 100, -1, 1));
			
			tieFighter.rotate(0, 0, (float)Toolbox.map(noiseX, -100, 100, -1.5f, 1.5f));
			
			if(totalTimePassedInMillis > 54000) {
				tunnel.moveSpeed = 10;
				falcon.move(3);
				tieFighter.move(3);
				lightsource.setIntensity(lightsource.getIntensity() * 0.999f);
				if(lightsource.getIntensity() < 0.25f) {
					exit();
				}
			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}
			totalTimePassedInMillis = (System.currentTimeMillis() - startTimeInMillis);
		}
	}

	private void initEngine() {
		engine = new Engine3D(screenX, screenY);

		Lightsource.AMBIENT_LIGHT_INTENSITY = 0;
		lightsource = new Lightsource(0, 0, 0, Color.WHITE, Lightsource.MAX_INTENSITY);
		engine.setLightsource(lightsource);
		
		camera = new Camera();
		engine.setCamera(camera);
		
		engine.setDefaultShader(new PhongShader(lightsource));
	}

	protected void processKeyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_PRESSED) {
			int keycode = event.getKeyCode();
			if (keycode == KeyEvent.VK_ESCAPE) {
				exit();
			} else if (keycode == KeyEvent.VK_F3) {
				engine.shadingEnabled = !engine.shadingEnabled;				
			}
		}
		super.processKeyEvent(event);
	}

	@Override
	public void paint(Graphics g) {
		long updateTime = System.currentTimeMillis();

		engine.drawScene(frame);
		frame = addFilterEffect(frame);
		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);

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
				int newRgb = ((255 & 0xFF) << 24) | (((int) newRed & 0xFF) << 16) | (((int) newGreen & 0xFF) << 8) | (((int) newBlue & 0xFF) << 0);
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
