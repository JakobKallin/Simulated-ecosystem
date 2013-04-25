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
	private static final double MAX_ENERGY = 200;

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
			double energyProportion = (double)energy/(double)MAX_ENERGY;
			double newEnergy = energy*energyProportion*(1-energyProportion)*0.1;
			System.out.println(newEnergy);
			energy += newEnergy;
		}
		int red = (int) (150.0 - 150.0 * (((double) energy) / MAX_ENERGY));
		int green = (int) (55.0 + 200.0 * (((double) energy) / MAX_ENERGY));
		this.color = new Color(red, green, 0);
		return null;

	}

	@Override
	public synchronized boolean tryConsumeAgent() {
		// Let the current agent be eaten if it has any energy
		if (energy >= 8) {
			energy -= 8;
			return true;
		}
		return false;
	}

	@Override
	public boolean looksTasty(IAgent agent, double visionRange) {

		double distance = agent.getPosition().getDistance(position)
				- (width + height) / 2;
		// If the agent has enough food for three agents, then it looks tasty!
		if (energy >= 24)
			return distance <= visionRange;
		return false;
	}
}
