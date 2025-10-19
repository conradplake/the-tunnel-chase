import java.awt.Color;

import gframe.engine.Model3D;
import gframe.engine.Point3D;
import gframe.engine.Toolbox;

public class FlyoverCity extends Model3D {

	long timePassedInMillis;
	long lastTimeInMillis;
	long timestepInMillis = 30;

	private int blockSize = 120;

	int cityWidth;
	int cityDepth;
	float citySpeed = 30;

	public FlyoverCity(int meshWidth, int meshDepth) {
		super();

		this.cityWidth = meshWidth * blockSize;
		this.cityDepth = meshDepth * blockSize;

		for (int y = 0; y < meshDepth - 1; y += 2) {
			for (int x = 0; x < meshWidth - 1; x += 2) {
			
				double r = Math.random();
				
				if(r < 0.5) {
					//continue;
				}
				
				int red = 200;
				int green = 200 + (int)(r * 20);
				int blue = 200 + (int)(r * 55);
				
			
				float blockSizeFactorX = r < 0.66? 0.8f : 1.5f;
				int myBlocksizeX = (int)(blockSizeFactorX * blockSize);
				
				r = Math.random();
				float blockSizeFactorZ = r < 0.33? 1 : r < 0.66? 2 : 3;
				int myBlocksizeZ = (int)(blockSizeFactorZ * blockSize);
				
				Model3D skyScraper = buildBlock(myBlocksizeX, 1, myBlocksizeZ, new Color(red, green, blue));
				skyScraper.move(new Point3D(x * myBlocksizeX, 0, y * myBlocksizeZ));

				float scaleFactor = (float)Toolbox.map(Math.random(), 0, 1, 0.2, 1);
				skyScraper.scale(1, 500 * scaleFactor, 1);

				this.addSubModel(skyScraper);
			}
		}
		
		lastTimeInMillis = System.currentTimeMillis();
	}

	public static Model3D buildBlock(float width, float height, float depth, java.awt.Color col) {
		Model3D result = new Model3D();

		result.addVertex(-width / 2, 0, -depth / 2); // 0
		result.addVertex(-width / 2, 0, depth / 2); // 1
		result.addVertex(width / 2, 0, depth / 2); // 2
		result.addVertex(width / 2, 0, -depth / 2); // 3

		result.addVertex(-width / 2, height, -depth / 2); // 4
		result.addVertex(-width / 2, height, depth / 2); // 5
		result.addVertex(width / 2, height, depth / 2); // 6
		result.addVertex(width / 2, height, -depth / 2); // 7

		result.stretchFace(4, 5, 6, 7, col); // dach
		result.stretchFace(0, 4, 7, 3, col); // rueckwand
		result.stretchFace(1, 5, 4, 0, col); // linke wand
		result.stretchFace(2, 6, 5, 1, col); // vorderwand
		result.stretchFace(3, 7, 6, 2, col); // rechte wand
		result.stretchFace(3, 2, 1, 0, col); // boden
		return result;
	}

	private void move() {
		for (Model3D subModel : this.getSubModels()) {
			subModel.move(0, 0, -citySpeed);
			if (subModel.getOrigin().z < 0) {
				subModel.move(0, 0, cityDepth);
			}
		}
	}

	@Override
	public void preDraw() {
		long currentTimeInMillis = System.currentTimeMillis();
		timePassedInMillis += (currentTimeInMillis - lastTimeInMillis);
		while (timePassedInMillis > timestepInMillis) {
			timePassedInMillis -= timestepInMillis;
			move();
		}
		lastTimeInMillis = currentTimeInMillis;
	}
}