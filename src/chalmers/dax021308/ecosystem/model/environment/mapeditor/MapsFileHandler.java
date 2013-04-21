package chalmers.dax021308.ecosystem.model.environment.mapeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import chalmers.dax021308.ecosystem.model.environment.obstacle.AbstractObstacle;
import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;

/**
 * Class for handling map files. Reading and writing on them.
 * <p>
 * Class needs testing!
 * 
 * @author Erik Ramqvist
 *
 */
public class MapsFileHandler {
	
	
	/**
	 * Gets all maps from the maps folder.
	 * @return
	 */
	public static List<SimulationMap> readMapsFromMapsFolder() {
		List<File> fileList = getMapFiles();
		if(fileList == null) {
			return null;
		}
		List<SimulationMap> result = new ArrayList<SimulationMap>();
		for(File f : getMapFiles()) {
			SimulationMap map = readMapFromFile(f);
			if(map != null && !map.getObsList().isEmpty()) {
				result.add(map);
			}
		}
		return result;
	}
	

	/**
	 * Reads a SimulationMap from the given File.
	 * @param fileMap
	 * @return null if no map is found, otherwise the read SimulationMap.
	 */
	public static SimulationMap readMapFromFile(File fileMap) {
		if (!fileMap.exists()) {
			return null;
		}
		if (!fileMap.canRead()) {
			return null;
		}
		try {
			FileInputStream fileStream = new FileInputStream(fileMap);
			Charset utf8 = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(fileStream, utf8));
			String name = br.readLine();
			String input = br.readLine();
			List<IObstacle> obsList = new ArrayList<IObstacle>();
			while(input != null) {
				IObstacle readObstacle = AbstractObstacle.createFromFile(input);
				if(readObstacle != null) {
					obsList.add(readObstacle);
				}
				input = br.readLine();
			}
			br.close();
			return new SimulationMap(obsList, name);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets all map files in maps-folder. 
	 * 
	 */
	public static List<File> getMapFiles() {
		File fileDir = new File("/maps");
		if(fileDir.exists() && fileDir.isDirectory()) {
			File[] files = fileDir.listFiles();
			List<File> result = new ArrayList<File>(files.length);
			for(File e : files) {
				if(e.getName().endsWith(".map")) {
					result.add(e);
				}
			}
			return result;
		}
		return null;
	}
	
	
	/**
	 * Saves the given SimulationMap to the given File destination.
	 * @param dest
	 * @param map
	 * @return
	 */
	public static boolean saveSimulationMap(File dest, SimulationMap map) {
		PrintWriter pw = null;
		try {
			dest.createNewFile();
			 pw = new PrintWriter(dest);
			if(dest.exists()) {
				dest.delete();
			}
			pw.println(map.getName());
			for(IObstacle o : map.getObsList()) {
				pw.println(o.toBinaryString());
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			pw.close();
		}
	}
	
}
