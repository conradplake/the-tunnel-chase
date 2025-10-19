import java.awt.Color;

import gframe.engine.Model3D;
import gframe.engine.Toolbox;
import gframe.engine.generator.NoiseGenerator;

public class Tunnel extends Model3D {

	long timePassedInMillis;
	long lastTimeInMillis;
	long timestepInMillis = 30;

	long moveSpeed = 30;

	int radius;
	int depth;
	
	float noiseOffset = 0;

	public Tunnel(int radius, int depth) {
		super();

		this.radius = radius;
		this.depth = depth;

		int segmentSize = 200;

		for (int z = 0; z < depth; z += segmentSize) {

			for (int i = 0; i < 10; i++) {
				Color structureColor = Color.darkGray;

				int degree = 0;
				int spanningDegree = (int) Toolbox.map(Math.random(), 0, 1, 20, 60);

				Model3D structure = new Model3D();

				float minRadius = (float) Toolbox.map(Math.random(), 0, 1, radius / 5f, radius / 2f);
				float structureDepth = minRadius + (float)Toolbox.map(Math.random() * 5, 0, 5, -5, 5);
				
				float x0 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * minRadius;
				float y0 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * minRadius;
				structure.addVertex(x0, y0, 0);

				float x1 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * radius;
				float y1 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * radius;
				structure.addVertex(x1, y1, 0);

				float x2 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * radius;
				float y2 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * radius;
				structure.addVertex(x2, y2, structureDepth);

				float x3 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * minRadius;
				float y3 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * minRadius;
				structure.addVertex(x3, y3, structureDepth);

				degree += spanningDegree;

				float x4 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * minRadius;
				float y4 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * minRadius;
				structure.addVertex(x4, y4, 0);

				float x5 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * radius;
				float y5 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * radius;
				structure.addVertex(x5, y5, 0);

				float x6 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * radius;
				float y6 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * radius;
				structure.addVertex(x6, y6, structureDepth);

				float x7 = (float) Math.cos(Toolbox.degreeToRadiant(degree)) * minRadius;
				float y7 = (float) Math.sin(Toolbox.degreeToRadiant(degree)) * minRadius;
				structure.addVertex(x7, y7, structureDepth);

				structure.stretchFace(4, 5, 1, 0, structureColor);
				structure.stretchFace(7, 4, 0, 3, structureColor);
				structure.stretchFace(6, 5, 4, 7, structureColor);
				structure.stretchFace(0, 1, 2, 3, structureColor);

				structure.move(0, 0, z + (float)Toolbox.map(Math.random() * 5, 0, 5, -5, 5));
				structure.rotate(0, 0, (float) Toolbox.map(Math.random(), 0, 1, 0, 360));

				this.addSubModel(structure);
			}

		}
		
		lastTimeInMillis = System.currentTimeMillis();
	}

	private void move() {
		for (Model3D subModel : this.getSubModels()) {
			subModel.move(0, 0, -moveSpeed);
			if (subModel.getOrigin().z < 0) {
				subModel.move(0, 0, depth);
				int noiseR = (int) Toolbox.map(NoiseGenerator.improvedPerlinNoise(noiseOffset, 0), -1, 1, 0, 255);
				int noiseG = (int) Toolbox.map(NoiseGenerator.improvedPerlinNoise(0, noiseOffset), -1, 1, 0, 255);		
				int noiseB = (int) Toolbox.map(NoiseGenerator.improvedPerlinNoise(noiseOffset, noiseOffset), -1, 1, 0, 255);
				subModel.setColor(new Color(noiseR, noiseG, noiseB));				
			}
		}
				
		noiseOffset += 0.01f;
		
		this.rotate(0, 0, (float)Toolbox.map(NoiseGenerator.improvedPerlinNoise(noiseOffset, 0), -1, 1, -5, 5));
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