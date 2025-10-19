import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import gframe.engine.Model3D;
import gframe.engine.Toolbox;

public class LightBeams extends Model3D {

	float beamLength;

	long timePassedInMillis;
	long startTimeInMillis;
	long totalTimePassedInMillis;
	long lastTimeInMillis;
	long timestepInMillis = 451; // 133 bpm

	boolean toggle = true;

	Color beamColor = Color.white;
	Color skyColor = Color.blue.darker().darker().darker();

	public LightBeams(float beamLength) {
		this.beamLength = beamLength;
		this.setColor(Color.white);

		for (int d = 0; d < 180;) {
			Model3D beam = new Model3D();
			beam.addVertex(0, 0, 0);

			float x = (float) Math.cos(Toolbox.degreeToRadiant(d)) * beamLength;
			float y = (float) Math.sin(Toolbox.degreeToRadiant(d)) * beamLength;
			beam.addVertex(x, y, 0);

			d += 10;
			x = (float) Math.cos(Toolbox.degreeToRadiant(d)) * beamLength;
			y = (float) Math.sin(Toolbox.degreeToRadiant(d)) * beamLength;
			beam.addVertex(x, y, 0);

			beam.stretchFace(2, 1, 0, skyColor);
			this.addSubModel(beam);
		}

		lastTimeInMillis = System.currentTimeMillis();
		startTimeInMillis = lastTimeInMillis;
	}

	@Override
	public void preDraw() {
		long currentTimeInMillis = System.currentTimeMillis();
		timePassedInMillis += (currentTimeInMillis - lastTimeInMillis);
		totalTimePassedInMillis = (currentTimeInMillis - startTimeInMillis);
		while (timePassedInMillis > timestepInMillis) {
			timePassedInMillis -= timestepInMillis;
			toggle = !toggle;

			if (totalTimePassedInMillis >= 231200) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 229500) {
				doubleRandomBeamLightning();
			} else if (totalTimePassedInMillis >= 214250) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 202050) {
				singleRandomBeamLightning();
			} else if (totalTimePassedInMillis >= 200110) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 187220) {
				sirenLightning();
			} else if (totalTimePassedInMillis >= 171200) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 158350) {
				doubleRandomBeamLightning();
			} else if (totalTimePassedInMillis >= 151000) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 137100) {
				singleRandomBeamLightning();
			} else if (totalTimePassedInMillis >= 106200) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 93300) {
				leftToRightBeamLightning();
			} else if (totalTimePassedInMillis >= 92000) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 79200) {
				sirenLightning();
			} else if (totalTimePassedInMillis >= 63100) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 50200) {
				randomBeamLightning();
			} else if (totalTimePassedInMillis >= 48350) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 36000) {
				sirenLightning();
			} else if (totalTimePassedInMillis >= 34600) {
				switchOffBeamLightning();
			} else if (totalTimePassedInMillis >= 28800) {
				singleRandomBeamLightning();
			} else if (totalTimePassedInMillis >= 21500) {
				rightToLeftBeamLightning();
			} else if (totalTimePassedInMillis >= 14200) {
				leftToRightBeamLightning();
			}
		}
		lastTimeInMillis = currentTimeInMillis;
	}

	private void switchOffBeamLightning() {
		for (Model3D subModel : this.getSubModels()) {
			subModel.setColor(skyColor);
		}
	}

	private void randomBeamLightning() {
		for (Model3D subModel : this.getSubModels()) {
			Color col = Math.random() < 0.5 ? beamColor : skyColor;
			subModel.setColor(col);
		}
	}

	private void singleRandomBeamLightning() {
		int onBeamIndex = -1;
		for (int i = 0; i < this.getSubModels().size(); i++) {
			if (this.getSubModels().get(i).getFaces().get(0).getColor().equals(beamColor)) {
				onBeamIndex = i;
				this.getSubModels().get(onBeamIndex).setColor(skyColor);
				break;
			}
		}

		int nextOnBeamIndex = (int) (Math.random() * this.getSubModels().size() - 1);
		while (nextOnBeamIndex == onBeamIndex) {
			nextOnBeamIndex = (int) (Math.random() * this.getSubModels().size() - 1);
		}

		for (int i = 0; i < this.getSubModels().size(); i++) {
			if (i == nextOnBeamIndex) {
				this.getSubModels().get(i).setColor(beamColor);
			} else {
				this.getSubModels().get(i).setColor(skyColor);
			}
		}
	}

	private void doubleRandomBeamLightning() {
		Set<Integer> onBeamIndices = new HashSet<>();
		for (int i = 0; i < this.getSubModels().size(); i++) {
			if (this.getSubModels().get(i).getFaces().get(0).getColor().equals(beamColor)) {
				onBeamIndices.add(i);
			}
		}
		switchOffBeamLightning();

		int nextOnBeamIndex = (int) (Math.random() * this.getSubModels().size() - 1);
		while (onBeamIndices.contains(nextOnBeamIndex)) {
			nextOnBeamIndex = (int) (Math.random() * this.getSubModels().size() - 1);
		}
		this.getSubModels().get(nextOnBeamIndex).setColor(beamColor);
		this.getSubModels().get(getSubModels().size() - 1 - nextOnBeamIndex).setColor(beamColor);
	}

	private void rightToLeftBeamLightning() {
		int onBeamIndex = -1;
		for (int i = 0; i < this.getSubModels().size(); i++) {
			if (this.getSubModels().get(i).getFaces().get(0).getColor().equals(beamColor)) {
				onBeamIndex = i;
				this.getSubModels().get(onBeamIndex).setColor(skyColor);
				break;
			}
		}

		if (onBeamIndex == this.getSubModels().size() - 1) {
			onBeamIndex = -1;
		}
		this.getSubModels().get(onBeamIndex + 1).setColor(beamColor);
	}

	private void leftToRightBeamLightning() {
		int onBeamIndex = this.getSubModels().size();
		for (int i = 0; i < this.getSubModels().size(); i++) {
			if (this.getSubModels().get(i).getFaces().get(0).getColor().equals(beamColor)) {
				onBeamIndex = i;
				this.getSubModels().get(onBeamIndex).setColor(skyColor);
				break;
			}
		}

		if (onBeamIndex == 0) {
			onBeamIndex = this.getSubModels().size();
		}
		this.getSubModels().get(onBeamIndex - 1).setColor(beamColor);
	}

	private void sirenLightning() {
		for (int i = 0; i < this.getSubModels().size(); i++) {
			if (toggle) {
				if (i % 2 == 0) {
					this.getSubModels().get(i).setColor(beamColor);
				} else {
					this.getSubModels().get(i).setColor(skyColor);
				}
			} else {
				if (i % 2 != 0) {
					this.getSubModels().get(i).setColor(beamColor);
				} else {
					this.getSubModels().get(i).setColor(skyColor);
				}
			}
		}
	}
}
