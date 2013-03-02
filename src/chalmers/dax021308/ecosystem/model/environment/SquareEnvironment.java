package chalmers.dax021308.ecosystem.model.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import chalmers.dax021308.ecosystem.model.environment.EcoWorld.OnFinishListener;
import chalmers.dax021308.ecosystem.model.population.IPopulation;
import chalmers.dax021308.ecosystem.model.util.Position;

/**
 * SquareEnvironment Class. Represents a environment in the shape of a square.
 * 
 * @author Henrik
 * 
 */
public class SquareEnvironment implements IEnvironment {
	// Should maybe use an abstract class Environment where suitable instance
	// variables can be declared
	private List<IPopulation> populations;
	private List<IObstacle> obstacles;
	private OnFinishListener mListener;
	private int height;
	private int width;
	
	private ExecutorService workPool;
	private List<Future<Runnable>> futures;

	/**
	 * 
	 * @param populations
	 *            The populations to be used in the environment
	 * @param obstacles
	 *            The obstacles to be used in the environment
	 * @param listener
	 *            The listener to this instance
	 * @param height
	 *            The height of the environment
	 * @param width
	 *            The width of the environment
	 */
	public SquareEnvironment(List<IPopulation> populations,
			List<IObstacle> obstacles, OnFinishListener listener, int height,
			int width) {
		this.populations = populations;
		this.obstacles = obstacles;
		this.mListener = listener;
		this.height = height;
		this.width = width;
		this.workPool = Executors.newFixedThreadPool(populations.size());
		this.futures = new ArrayList<Future<Runnable>>();
	}

	@Override
	/**
	 * Run method, called for each tick of the timer
	 * Updates each population and then informs EcoWorld once it's finished
	 */
	public void run() {
        Future f = workPool.submit(new PopulationWorker());
        futures.add(f);

        for (Future<Runnable> fut : futures)
        {
           try {
			fut.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        }
        //Remove all agents from the remove list.
		for (int i = 0; i < populations.size(); i++)
			populations.get(i).removeAgentsFromRemoveList();
		

        //Update all the positions, i.e. position = nextPosition.
		//for (int i = 0; i < populations.size(); i++)
		//	populations.get(i).removeAgentsFromRemoveList();
		
		// Callback function called to inform EcoWorld that the current update
		// is run
		mListener.onFinish(populations, obstacles);
	}
	
	private class PopulationWorker implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < populations.size(); i++)
				populations.get(i).update();
		}
	}

	@Override
	public boolean isFree(Position p) {

		// for each population, check if any agent is currently occupying
		// position p
		/*
		 * Needs some kind of lookup method in IPopulation to function easily.
		 * Or some kind of getPosition for each agent or something similar for
		 * (int i = 0; i < populations.size(); i++) if
		 * (populations.get(i).occupies(p)) return false;
		 */

		// for each obstacle, check if the position p lies inside any obstacle
		for (int i = 0; i < obstacles.size(); i++)
			if (obstacles.get(i).insideObstacle(p))
				return false;

		// If there is neither a population nor an obstacle at position p, then
		// it is free
		return true;
	}
}
