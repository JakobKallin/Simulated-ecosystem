package chalmers.dax021308.ecosystem.model.agent;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
import chalmers.dax021308.ecosystem.model.population.IPopulation;
import chalmers.dax021308.ecosystem.model.util.Position;
import chalmers.dax021308.ecosystem.model.util.Vector;
import chalmers.dax021308.ecosystem.model.util.shape.IShape;

/**
 * Simple grass, lowest part of the food chain
 * 
 * @author Henrik
 */
public class GrassFieldAgent extends AbstractAgent {

	private static final double REPRODUCTION_RATE = 0.1;
	private static final int MAX_ENERGY = 100;

	public GrassFieldAgent(String name, Position pos, Color color, int width,
			int height, Vector velocity, double maxSpeed, int capacity) {
		super(name, pos, color, width, height, velocity, maxSpeed, 0, 0);
		this.capacity = capacity;
		energy = 50;
	}

	@Override
	public void calculateNextPosition(List<IPopulation> predators,
			List<IPopulation> preys, List<IPopulation> neutral, Dimension dim,
			IShape shape, List<IObstacle> obstacles) {
		// Do nothing, grass shouldn't move!
	}

	@Override
	public void updatePosition() {
		// Do nothing? Shouldn't get old or anything
	}

	@Override
	public List<IAgent> reproduce(IAgent agent, int populationSize,
			List<IObstacle> obstacles, IShape shape, Dimension gridDimension) {
		if (Math.random() < REPRODUCTION_RATE) {
			if (energy < MAX_ENERGY)
				energy += 1;
		}
		return null;

	}

	@Override
	public synchronized boolean tryConsumeAgent() {
		// TODO FIX FEEDING
		if (energy >= 20) {
			//energy -= 20;
			return true;
		}
		return false;
	}
}