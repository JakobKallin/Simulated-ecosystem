package chalmers.dax021308.ecosystem.model.environment;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import chalmers.dax021308.ecosystem.model.agent.IAgent;
import chalmers.dax021308.ecosystem.model.population.AbstractPopulation;
import chalmers.dax021308.ecosystem.model.population.DummyPreyPopulation;
import chalmers.dax021308.ecosystem.model.population.DummyPredatorPopulation;
import chalmers.dax021308.ecosystem.model.population.IPopulation;
import chalmers.dax021308.ecosystem.model.population.RabbitPopulation;
import chalmers.dax021308.ecosystem.model.util.Log;
import chalmers.dax021308.ecosystem.model.util.TimerHandler;

/**
 * Ecosystem main class.
 * <p>
 * Recieves notifications from the {@link TimerHandler} and the
 * {@link IEnvironment}.
 * 
 * @author Erik Ramqvist
 * 
 */
public class EcoWorld {

	public static final String EVENT_TICK              = "chalmers.dax021308.ecosystem.model.Ecoworld.event_tick";
	public static final String EVENT_STOP              = "chalmers.dax021308.ecosystem.model.Ecoworld.event_stop";
	public static final String EVENT_PAUSE		       = "chalmers.dax021308.ecosystem.model.Ecoworld.event_pause";
	public static final String EVENT_RECORDINGFINISHED = "chalmers.dax021308.ecosystem.model.Ecoworld.event_pause";
	
	private AtomicBoolean environmentFinished = new AtomicBoolean(false);
	private AtomicBoolean timerFinished = new AtomicBoolean(false);
	private AtomicBoolean shouldRun = new AtomicBoolean(false);
	private boolean runWithoutTimer;
	private boolean recordSimulation;
	
	private int numIterations;
	private TimerHandler timer;
	private IEnvironment env;
	private int tickTime;
	private PropertyChangeSupport observers;
	
	/**
	 * Each list in the list contains one snapshot of frame;
	 */
	private List<List<IPopulation>> recordedSimulation;
	/**
	 * Simple object, used for synchronizing the {@link TimerHandler} and the
	 * {@link IEnvironment} {@link OnFinishListener}
	 */
	private Object syncObject = new Object();
	private static final int NUM_THREAD = 1;
	private int numUpdates = 0;
	private Dimension d;
	private ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);

	private OnFinishListener mOnFinishListener = new OnFinishListener() {
		@Override
		public void onFinish(List<IPopulation> popList, List<IObstacle> obsList) {
			// Fire state changed to observers, notify there has been an update.
			if(recordSimulation) {
				recordedSimulation.add(clonePopulationList(popList));
			} else {
				observers.firePropertyChange(EVENT_TICK, obsList, popList);
			}
			if (runWithoutTimer) {
				scheduleEnvironmentUpdate();		
			} else {
				synchronized (syncObject) {
					//Log.v("Environment: Finished.");
					if (timerFinished.get()) {
						//Log.v("Environment: Timer is finished, doing Environment update");
						environmentFinished.set(false);
						timerFinished.set(false);
						scheduleEnvironmentUpdate();
					} else {
						//Log.v("Environment: Timer NOT finished, waiting...");
						environmentFinished.set(true);
					}
				}
			}
		}
	};

	private OnTickUpdate onTickListener = new OnTickUpdate() {
		@Override
		// När timer är klar.
		public void onTick() {
			synchronized (syncObject) {
				//Log.v("Timer: Finished.");
				if (environmentFinished.get()) {
					//Log.v("Timer: Environment is finished, doing Environment update");
					timerFinished.set(false);
					environmentFinished.set(false);
					scheduleEnvironmentUpdate();
				} else {
					//Log.v("Timer: Environment NOT finished, waiting...");
					timerFinished.set(true);
				}
			}
		}
	};
	

	/**
	 * Start EcoWorld with a tick-timer.
	 * 
	 * @param tickTime
	 *            Minimum time it will take for one tick to complete.
	 * @param numIterations
	 *            Number of iterations before the program finishes.
	 *            
	 * @param d Dimension of the simulation.
	 */
	public EcoWorld(Dimension d, int tickTime, int numIterations, boolean recordSimulation) {
		this.tickTime = tickTime;
		this.timer = new TimerHandler();
		this.d = d;
		this.recordSimulation = recordSimulation;
		if(recordSimulation) {
			recordedSimulation = new ArrayList<List<IPopulation>>(1000);
		}

		/* Uncomment to test ticking functionality */
		// this.env = new Environment(mOnFinishListener);

		/* Use SquareEnvironment instead. */
		this.env = new SquareEnvironment(createInitialPopulations(d),
				readObsticlesFromFile(), mOnFinishListener, d.height, d.width);

		this.runWithoutTimer = false;
		this.numIterations = numIterations;
		this.observers = new PropertyChangeSupport(this);
	}

	/**
	 * Start EcoWorld WITHOUT a tick-timer.
	 * <p>
	 * EcoWorld simulation will run as fast as it can, without delays.
	 * 
	 * @param numIterations
	 *            Number of iterations before the program finishes.
	 *            
	 * @param d Dimension of the simulation.
	 */
	public EcoWorld(Dimension d, int numIterations) {
		this(d, 0, numIterations, false);
		this.runWithoutTimer = true;
	}

	/**
	 * Start EcoWorld WITHOUT a tick-timer.
	 * <p>
	 * EcoWorld simulation will run as fast as it can, without delays. For a
	 * very long time.
	 *            
	 * @param d Dimension of the simulation.
	 * 
	 */
	public EcoWorld(Dimension d) {
		this(d, Integer.MAX_VALUE);
	}

	private List<IPopulation> createInitialPopulations(Dimension dim) {
		List<IPopulation> populations = new ArrayList<IPopulation>();
		IPopulation rabbits = new RabbitPopulation(1000, dim);
		rabbits.addPrey(rabbits);
		populations.add(rabbits);
		
//		IPopulation prey = new DummyPreyPopulation(dim, 300, Color.red, 1.5, 1, 250);
//		IPopulation predator = new DummyPredatorPopulation(dim,10, Color.green, 2, 0.5,275);
//		
//		prey.addPredator(predator);
//		predator.addPrey(prey);
//		populations.add(prey);
//		populations.add(predator);
		return populations;
	}

	private List<IObstacle> readObsticlesFromFile() {
		List<IObstacle> obsList = new ArrayList<IObstacle>();
		obsList.add(new Obstacle("Obstacle.txt"));
		return obsList;
	}

	/**
	 * Start the EcoWorld simulation program.
	 * 
	 */
	public void start() {
		shouldRun.set(true);
		scheduleEnvironmentUpdate();
		Log.i("EcoWorld started.");
	}

	/**
	 * Stops the scheduling algorithms.
	 * <p>
	 * Warning! Will not affect ongoing execution!
	 * 
	 */
	public void stop() {
		shouldRun.set(false);
		executor.shutdown();
		timer.stop();
		numUpdates = 0;
		Log.i("EcoWorld stopped.");
		if(recordSimulation) {
//			for(List<IPopulation> list : recordedSimulation) {
//				for(IPopulation pop : list) {
//					Log.v("Population: " + pop);
//					for(IAgent a : pop.getAgents()) {
//						Log.v("Population: " + pop.toString() + " Agent:" + a.toString());
//					}
//				}
//			}
			observers.firePropertyChange(EVENT_RECORDINGFINISHED, null, null);
		}
	}
	
	/**
	 * Plays the recorded simulation (if any), otherwise throws {@link IllegalArgumentException}.
	 * <P>
	 * Uses internal {@link TimerHandler} for smooth playing.
	 */
	public void playRecordedSimulation() {
		if(!recordSimulation) {
			throw new IllegalStateException("No simulation has been recorded");
		}
		final TimerHandler t = new TimerHandler();
		t.start(17, new OnTickUpdate() {
			@Override
			public void onTick() {
				if(recordedSimulation.size() > 0) {
					List<IPopulation> popList = recordedSimulation.get(0);
					recordedSimulation.remove(0);
					observers.firePropertyChange(EVENT_TICK, Collections.emptyList(), popList);
					t.start(17, this);
				} else {
					t.stop();
					observers.firePropertyChange(EVENT_STOP, Collections.emptyList(), Collections.emptyList());
				}
			}
		});
	}

	/**
	 * Forces the ongoing execution to stop!
	 * <p>
	 * Warning! Untested method, might not work.
	 * 
	 */
	public void forceStop() {
		shouldRun.set(false);
		executor.shutdownNow();
		timer.stop();
		numUpdates = 0;
		Log.i("EcoWorld stopped.");
	}

	/**
	 * Starts the {@link TimerHandler} and executes one Environment iteration.
	 */
	private void scheduleEnvironmentUpdate() {
		if (numIterations-- > 0) {
			if (!runWithoutTimer) {
				timer.start(tickTime, onTickListener);
			}
			Log.v("---- Simulation model Update ---- Number of updates: "
					+ ++numUpdates);
			executor.execute(env);
		} else {
			stop();
			playRecordedSimulation();
		}
	}
	
	/**
	 * Adjust the tick rate of the next iteration. The currently executing
	 * iteration will not be affected.
	 * 
	 * @param newTickRate
	 */
	public void adjustTickRate(int newTickRate) {
		this.tickTime = newTickRate;
	}

	/**
	 * Tick listener for the TimerHandler. Called when timer has expired.
	 * 
	 * @author Erik
	 * 
	 */
	public interface OnTickUpdate {
		public void onTick();
	}

	public void setRunWithoutTimer(boolean runWithoutTimer) {
		this.runWithoutTimer = runWithoutTimer;
	}

	/**
	 * Environment onFinish listener. Called when one iteration of the
	 * Environment is done.
	 * 
	 * @author Erik
	 * 
	 */
	public interface OnFinishListener {
		public void onFinish(List<IPopulation> popList,
				List<IObstacle> obstacleList);
	}

	public void addObserver(PropertyChangeListener listener) {
		observers.addPropertyChangeListener(listener);
	}
	
	public void removeObserver(PropertyChangeListener listener) {
		observers.removePropertyChangeListener(listener);
	}
	
	/**
	 * Clones the given list with {@link IPopulation#clonePopulation()} method.
	 */
	private List<IPopulation> clonePopulationList(List<IPopulation> popList) {
		List<IPopulation> list = new ArrayList<IPopulation>(popList.size());
		for(IPopulation p : popList) {
			list.add(p.clonePopulation());
		}
		return list;
	}

}
