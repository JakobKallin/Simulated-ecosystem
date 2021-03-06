package chalmers.dax021308.ecosystem.model.environment;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;

import chalmers.dax021308.ecosystem.model.environment.mapeditor.DefaultMaps;
import chalmers.dax021308.ecosystem.model.environment.mapeditor.MapFileHandler;
import chalmers.dax021308.ecosystem.model.environment.mapeditor.SimulationMap;
import chalmers.dax021308.ecosystem.model.population.IPopulation;

/**
 * Container class for various simulation settings.
 * 
 * @author Erik Ramqvist
 *
 */
public class SimulationSettings {
	
	/* Static presets profiles */
	public static final SimulationSettings DEFAULT;
	public static final SimulationSettings LARGESIM;
	public static final SimulationSettings EXTREME;
	public static final SimulationSettings FASTRUN;
	public static final SimulationSettings[] PROFILE_VALUES;
	
	public static final String DEFAULT_SETTINGSFILE = "saved_settings.property";
	
	
	/* Shape Constants */
	public static final String SHAPE_SQUARE   = "Square";
	public static final String SHAPE_CIRCLE   = "Circle";
	public static final String SHAPE_TRIANGLE = "Triangle";

	/* Population constants */
	public static final String POP_PIG        = "Pig Population";
	public static final String POP_DUMMYPREY  = "Dummy Prey Population";
	public static final String POP_DEER       = "Deer Population";
	public static final String POP_GRASS      = "Grass Population";
	public static final String POP_DUMMYPRED  = "Dummy Predator Population";
	public static final String POP_WOLF       = "Wolf Population";
	public static final String POP_GRASS_FIELD = "Grass Field Population";
	
	/* Population names */
	public static final String NAME_GRASS_FIELD = "Grass Field";

	/* Population array based on predator-prey model, the view uses these values. */
	public static final String[] PREY_VALUES  = { POP_DEER, POP_PIG, POP_DUMMYPREY };
	public static final String[] PRED_VALUES  = { POP_WOLF, POP_DUMMYPRED };
	public static final String[] GRASS_VALUES = { POP_GRASS, POP_GRASS_FIELD };
	
	/* Dimension constants */
	public static final String DIM_SMALL  = "500  x 500";
	public static final String DIM_MEDIUM = "1000 x 1000";
	public static final String DIM_LARGE  = "1500 x 1500";
	public static final String DIM_XLARGE = "2000 x 2000";

	public static final Dimension D_SMALL  = new Dimension(500, 500);
	public static final Dimension D_MEDIUM = new Dimension(1000, 1000);
	public static final Dimension D_LARGE  = new Dimension(1500, 1500);
	public static final Dimension D_XLARGE = new Dimension(2000, 2000);

	public static final String[] DIM_VALUES = { DIM_SMALL, DIM_MEDIUM, DIM_LARGE, DIM_XLARGE };
	
	/* Simulation profiles settings */
	static {
		DEFAULT = new SimulationSettings("Default", POP_WOLF, 50, POP_DEER, 250, POP_GRASS, 600, SHAPE_SQUARE, 4, false, false, 16, Integer.MAX_VALUE, DefaultMaps.empty_map);
		DEFAULT.setSimulationDimension(DIM_XLARGE);
		LARGESIM = new SimulationSettings("Large simulation", POP_WOLF, 100, POP_DEER, 1000, POP_GRASS, 4000, SHAPE_SQUARE, 4, false, false, 16, Integer.MAX_VALUE, DefaultMaps.elliptical_map);
		LARGESIM.setSimulationDimension(DIM_XLARGE);
		EXTREME = new SimulationSettings("Large simulation", POP_WOLF, 650, POP_DEER, 2200, POP_GRASS, 10000, SHAPE_SQUARE, 8, true, false, 0, Integer.MAX_VALUE, DefaultMaps.empty_map);
		EXTREME.setSimulationDimension(new Dimension(2600, 2600));
		FASTRUN = new SimulationSettings("Default", POP_WOLF, 50, POP_DEER, 250, POP_GRASS, 600, SHAPE_SQUARE, 4, true, false, 0, Integer.MAX_VALUE, DefaultMaps.empty_map);
		FASTRUN.setSimulationDimension(DIM_XLARGE);
		
		PROFILE_VALUES = new SimulationSettings[2];
		PROFILE_VALUES[0] = DEFAULT;
		PROFILE_VALUES[1] = LARGESIM;
	}

	private String simulationProfileName;
	private String predatorModel;
	private int predPopSize;
	private String preyModel;
	private int preyPopSize;
	private String grassModel;
	private int grassPopSize;
	private String shapeModel;
	private Dimension simDimension;
	private String simDimensionConstant;
	private int numThreads;
	private boolean runWithoutTimer;
	private boolean recordSimulation;
	private int delayLength;
	private int numIterations;
	private SimulationMap map;

	private List<IPopulation> finalPopulations;

	public SimulationSettings(String simulationProfileName, String predatorModel, int predPopSize, String preyModel, int preyPopSize, String grassModel, int grassPopSize, String shapeModel, int numThreads, boolean runWithoutTimer, boolean recordSimulation, int delayLength, int numIterations, SimulationMap map) {
		this.simulationProfileName = simulationProfileName;
		this.predatorModel = predatorModel;
		this.predPopSize = predPopSize;
		this.preyModel = preyModel;
		this.preyPopSize = preyPopSize;
		this.grassModel = grassModel;
		this.grassPopSize = grassPopSize;
		this.shapeModel = shapeModel;
		this.numThreads = numThreads;
		this.runWithoutTimer = runWithoutTimer;
		this.recordSimulation = recordSimulation;
		this.delayLength = delayLength;
		this.numIterations = numIterations;
		this.map = map;
	}
	
	public void updateLiveSettings(int delayLenght) {
		this.delayLength = delayLenght;
	}
	
	public int getDelayLength() {
		return delayLength;
	}
	
	public void setDelayLength(int delayLength) {
		this.delayLength = delayLength;
	}
	
	public int getNumIterations() {
		return numIterations;
	}
	
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

	public boolean isRecordSimulation() {
		return recordSimulation;
	}
	
	public boolean isRunWithoutTimer() {
		return runWithoutTimer;
	}
	
	public void setSimulationDimension(Dimension d) {
		this.simDimension = d;
		this.simDimensionConstant = null;
	}

	public void setSimulationDimension(String simDimensionConstant) {
		this.simDimension = null;
		this.simDimensionConstant = simDimensionConstant;
	}
	
	public Dimension getSimDimension() {
		return simDimension;
	}
	
	public String getSimDimensionConstant() {
		return simDimensionConstant;
	}

	public String getPredatorModel() {
		return predatorModel;
	}

	public void setPredatorModel(String predatorModel) {
		this.predatorModel = predatorModel;
	}

	public int getPredPopSize() {
		return predPopSize;
	}

	public void setPredPopSize(int predPopSize) {
		this.predPopSize = predPopSize;
	}

	public String getPreyModel() {
		return preyModel;
	}

	public void setPreyModel(String preyModel) {
		this.preyModel = preyModel;
	}

	public int getPreyPopSize() {
		return preyPopSize;
	}

	public void setPreyPopSize(int preyPopSize) {
		this.preyPopSize = preyPopSize;
	}

	public String getGrassModel() {
		return grassModel;
	}

	public void setGrassModel(String grassModel) {
		this.grassModel = grassModel;
	}

	public int getGrassPopSize() {
		return grassPopSize;
	}

	public void setGrassPopSize(int grassPopSize) {
		this.grassPopSize = grassPopSize;
	}

	public String getShapeModel() {
		return shapeModel;
	}

	public void setShapeModel(String shapeModel) {
		this.shapeModel = shapeModel;
	}

	public SimulationMap getMap() {
		return map;
	}
	
	public void setMap(SimulationMap map) {
		this.map = map;
	}
	
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public int getNumThreads() {
		return numThreads;
	}
	
	@Override
	public String toString() {
		return simulationProfileName;
	}
	
	public List<IPopulation> getFinalPopulations() {
		return finalPopulations;
	}
	
	public void setFinalPopulations(List<IPopulation> finalPopulations) {
		this.finalPopulations = finalPopulations;
	}
	

	/**
	 * Try to read a SimulationSettings from a file.
	 * <p>
	 * Is now hard-coded to read the default settings file, but can easily be changed to read arbitrary settings file.
	 * @param filePath
	 */
	public static SimulationSettings loadFromFile() {
		String fileName = DEFAULT_SETTINGSFILE;
		File recordedFile = new File(fileName);
		if (!recordedFile.exists()) {
			return null;
		}
		if (!recordedFile.canRead()) {
			return null;
		}
		try { 
			FileInputStream fileStream = new FileInputStream(recordedFile);
			Charset utf8 = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(fileStream, utf8));
			String inputLine = br.readLine();
			br.close();
			return fromDataString(inputLine);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Save this instance of SimulationSettings to a file. 
	 * Overwrites of there is already a file with that name.
	 * <p>
	 * Is now hard-coded to save to the default settings file, but can easily be changed to save to arbitrary settings file.
	 */
	public boolean saveToFile() {
		String filepath = DEFAULT_SETTINGSFILE;
		File f = new File(filepath);
		if(f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(filepath);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		pw.println(toDataString());
		pw.close();
		return true;
	}
	
	/**
	 * Tries to create a SimulationSettings from the given String.
	 * 
	 * @param s
	 * @return
	 */
	public static SimulationSettings fromDataString(String s) {
		String[] input = s.split(";");
		if(input.length < 12) {
			return null;
		}
		int i = -1;
		//public SimulationSettings(String simulationProfileName, String predatorModel, int predPopSize, String preyModel, int preyPopSize, String grassModel, int grassPopSize, String shapeModel, String obstacle, int numThreads, 
		//boolean runWithoutTimer, boolean recordSimulation, int delayLength, int numIterations) {
		SimulationSettings simSettings = new SimulationSettings(
				input[++i],
				input[++i], 
				Integer.parseInt(input[++i]),
				input[++i],
				Integer.parseInt(input[++i]),
				input[++i],
				Integer.parseInt(input[++i]),
				input[++i],
				Integer.parseInt(input[++i]),
				Boolean.valueOf(input[++i]),
				Boolean.valueOf(input[++i]),
				Integer.parseInt(input[++i]),
				Integer.parseInt(input[++i]),
				null);
				//TODO: Add map here.
		i++;
		int pos = i;
		try {
			int width = Integer.parseInt(input[i++]);
			int height = Integer.parseInt(input[i++]);
			simSettings.setSimulationDimension(new Dimension(width, height));
		} catch (NumberFormatException e) {
			String dimConstant = input[pos];
			i--;
			simSettings.setSimulationDimension(dimConstant);
		}
		if (input.length == (i+1) ) {
			String lastSelectedMap = input[i];
			SimulationMap map = MapFileHandler.getMapByName(lastSelectedMap);
			if(map == null) {
				for(SimulationMap m :DefaultMaps.getDefaultMaps()) {
					if(m.getName().equals(lastSelectedMap)) {
						simSettings.setMap(m);
						break;
					}
				}
			} else {
				simSettings.setMap(map);
			}
//			Log.e("Saved Map Found: " + lastSelectedMap);
		} else {
//			Log.e("No Saved Map Found");
		}
		return simSettings;
	}

	/**
	 * Converts the instance to a data String, for dumping to a textfile and reading later on.
	 * 
	 */
	public String toDataString() {
		StringBuilder sb = new StringBuilder();
		sb.append(simulationProfileName);
		sb.append(';');
		sb.append(predatorModel);
		sb.append(';');
		sb.append(predPopSize);
		sb.append(';');
		sb.append(preyModel);
		sb.append(';');
		sb.append(preyPopSize);
		sb.append(';');
		sb.append(grassModel);
		sb.append(';');
		sb.append(grassPopSize);
		sb.append(';');
		sb.append(shapeModel);
		sb.append(';');
		sb.append(numThreads);
		sb.append(';');
		sb.append(runWithoutTimer);
		sb.append(';');
		sb.append(recordSimulation);
		sb.append(';');
		sb.append(delayLength);
		sb.append(';');
		sb.append(numIterations);
		sb.append(';');
		if(simDimension == null) {
			sb.append(simDimensionConstant);
		} else {
			sb.append(simDimension.width);
			sb.append(';');
			sb.append(simDimension.height);
		}
		if(map != null) {
			sb.append(';');
			sb.append(map.getName());
		}
		return sb.toString();
	}

	public void setRunWithoutTimer(boolean runWithoutTimer) {
		this.runWithoutTimer = runWithoutTimer;
	}
	
	
	
}
