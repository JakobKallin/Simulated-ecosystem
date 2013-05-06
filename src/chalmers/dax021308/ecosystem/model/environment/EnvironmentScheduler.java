package chalmers.dax021308.ecosystem.model.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import chalmers.dax021308.ecosystem.model.environment.EcoWorld.OnFinishListener;
import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
import chalmers.dax021308.ecosystem.model.population.IPopulation;
import chalmers.dax021308.ecosystem.model.util.Log;
import chalmers.dax021308.ecosystem.model.util.Position;

/**
 * EnvironmentScheduler (Formerly SquareEnvironment) Class. 
 * <p>
 * Contains workers that execute the updates of the Population in parallel.
 * <p>
 * The workers is represented as a seperate thread in a {@link ExecutorService}.
 * 
 * @author Henrik, for concurrency: Erik Ramqvist
 * 
 */
public class EnvironmentScheduler implements Runnable {
	
	private static final int NUMAGENTS_PER_WORKPOOL = 300;
	
	// Should maybe use an abstract class Environment where suitable instance
	// variables can be declared
	private List<IPopulation> populations;
	private List<IObstacle> obstacles;
	private OnFinishListener mListener;

	/* Concurrent variables */
	private ExecutorService workPool;
	private List<Future<Runnable>> futures;
	private AdvancedPopulationWorker  popWorkers[];
	private FinalizeIteration finWorkers[];
	private AdvancedPopulationWorker extraPopWorker;
	private IPopulation lastSlowestPop;
	private long longestExecuteTime;

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
	public EnvironmentScheduler(List<IPopulation> populations,
			List<IObstacle> obstacles, OnFinishListener listener, int height,
			int width, int numThreads) {
		this.populations = populations;
		this.obstacles = obstacles;
		this.mListener = listener;
		
		//Create one Worker for each population.
		this.workPool = Executors.newFixedThreadPool(1);
		//Create the list of executing tasks, for barrier synchronization.
		this.futures = new ArrayList<Future<Runnable>>();
		
		//Create the worker objects. (Reusable for memory and performance.)
		this.popWorkers = new AdvancedPopulationWorker[populations.size()];
		this.extraPopWorker = new AdvancedPopulationWorker();
		for(int i = 0; i < popWorkers.length ; i++) {
			popWorkers[i] = new AdvancedPopulationWorker();
		}
		this.finWorkers = new FinalizeIteration[populations.size()];
		for(int i = 0; i < finWorkers.length ; i++) {
			finWorkers[i] = new FinalizeIteration();
		}
	}
	
	public List<IPopulation> getPopulations() {
		return populations;
	}

	/**
	 * Run method, called for each tick of the timer
	 * Updates each population and then informs EcoWorld once it's finished
	 */
	@Override
	public void run() {
//		int size = 0;
//		for(int i = 0 ; i < populations.size(); i ++) {
//			size = size + populations.get(i).getSize();
//		}
//		if(size < 1.5 * NUMAGENTS_PER_WORKPOOL) {
//			executeFastPopulationDividedAlgorithm();
//		} else {
			executeAgentDividedAlgorithm();
//		}

		// Callback function called to inform EcoWorld that the current update
		// is run
		mListener.onFinish(populations, obstacles);
	}
	
	/**
	 * Slow, but fast start, appropriate for small populations.
	 */
	private void executeFastPopulationDividedAlgorithm() {
        futures.clear();
		   //Assign objects to workers.
				longestExecuteTime = 0;
				for(int i = 0 ; i < populations.size(); i ++) {
					popWorkers[i].p = populations.get(i);
					//If this is the slowest population.
					if(popWorkers[i].p == lastSlowestPop) {
						popWorkers[i].dividePopulation = true;
						extraPopWorker.dividePopulation = true;
						extraPopWorker.p = lastSlowestPop;
						
						extraPopWorker.executeFirstHalf = false;
						popWorkers[i].executeFirstHalf = true;
						Future f = workPool.submit(extraPopWorker);
				        futures.add(f);
					}
					popWorkers[i].dividePopulation = false;
					Future f = workPool.submit(popWorkers[i]);
			        futures.add(f);
				}


				//Barrier synchronization here. Thread will wait for workers to finish execution.
		        for (Future<Runnable> fut : futures)
		        {
		           try {
					fut.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
		        }
		        futures.clear();

		        //Assign objects to workers.
				for(int i = 0 ; i < populations.size(); i ++) {
					finWorkers[i].p = populations.get(i);
					Future f = workPool.submit(finWorkers[i]);
			        futures.add(f);
				}
				
				//Log.v("Slowest population: " + lastSlowestPop + " Time in ms: " + (long) (0.000001*longestExecuteTime));


				//Barrier synchronization here. Thread will wait for workers to finish execution.
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
	}

	/**
	 * Algorithm based on dividing the work equal amongst the workers.
	 * The idea was to optimize the CPU cores allowing no core to sleep.
	 */
	private void executeAgentDividedAlgorithm() {
        futures.clear();
        //Assign objects to workers.
		longestExecuteTime = 0;
		for(int i = 0 ; i < populations.size(); i ++) {
			IPopulation p = populations.get(i);
			double computationalFactor = p.getComputationalFactor();
			int j = 0;
			while(j < p.getSize()) {
				PopulationWorker popWork = new PopulationWorker();
				popWork.startPos = j;
				popWork.p = p;
				j = (int) (j + computationalFactor*NUMAGENTS_PER_WORKPOOL);
				if(j > p.getSize()) {
					popWork.endPos = p.getSize();							
				} else {
					popWork.endPos = j;					
				}
				Future f = workPool.submit(popWork);
				//Log.v("Adding " + p + " to work between " + popWork.startPos + " and " + popWork.endPos + " ");
		        futures.add(f);
			}

		}


		//Barrier synchronization here. Thread will wait for workers to finish execution.
        for (Future<Runnable> fut : futures)
        {
           try {
			fut.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
			e.getCause().printStackTrace();
		}
        }
        futures.clear();

        //Assign objects to workers.
		for(int i = 0 ; i < populations.size(); i ++) {
			finWorkers[i].p = populations.get(i);
			Future f = workPool.submit(finWorkers[i]);
	        futures.add(f);
		}
		
		//Log.v("Slowest population: " + lastSlowestPop + " Time in ms: " + (long) (0.000001*longestExecuteTime));


		//Barrier synchronization here. Thread will wait for workers to finish execution.
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

	}
	
	private class PopulationWorker implements Runnable {
		private IPopulation p;
		
		private int startPos;
		private int endPos;
		
		
		@Override
		public void run() {
			//Calculate one iteration. But only calculate it!
			p.update(startPos, endPos);
		}
	}
	
	private class AdvancedPopulationWorker implements Runnable {
		private IPopulation p;
		private boolean dividePopulation;
		private boolean executeFirstHalf;
		
		@Override
		public void run() {
			//Calculate one iteration. But only calculate it!
			long start = System.nanoTime();
			if(!dividePopulation) {
				p.update();
			} else {
				if(executeFirstHalf) {
					//Execute first half.
					p.updateFirstHalf();
				} else {
					//Execute second half.
					p.updateSecondHalf();
				}
			}
			long elapsedTime = System.nanoTime() - start;
			//Quick fix for double pop size.
			if(dividePopulation) {
			 	elapsedTime = elapsedTime * 2;
			}
			if(elapsedTime > longestExecuteTime) {
				longestExecuteTime = elapsedTime;
				lastSlowestPop = p;
			}
		}
	}
	

	
	private class FinalizeIteration implements Runnable {
		private IPopulation p;
		
		@Override
		public void run() {
			//Remove agents that has been marked as remove.
			p.removeAgentsFromRemoveList();

	        //Update all the positions, i.e. position = nextPosition.
			p.updatePositions();
		}
	}



	public void shutdown() {
		workPool.shutdownNow();
	}

	/*@Override
	public boolean isFree(Position p) {

		// for each population, check if any agent is currently occupying
		// position p
		
		 * Needs some kind of lookup method in IPopulation to function easily.
		 * Or some kind of getPosition for each agent or something similar for
		 * (int i = 0; i < populations.size(); i++) if
		 * (populations.get(i).occupies(p)) return false;
		 

		// for each obstacle, check if the position p lies inside any obstacle
//		for (int i = 0; i < obstacles.size(); i++)
//			if (obstacles.get(i).insideObstacle(p))
//				return false;

		// If there is neither a population nor an obstacle at position p, then
		// it is free
		return true;
	}*/
}
