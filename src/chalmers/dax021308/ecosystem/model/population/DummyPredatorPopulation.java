package chalmers.dax021308.ecosystem.model.population;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import chalmers.dax021308.ecosystem.model.agent.IAgent;
import chalmers.dax021308.ecosystem.model.agent.SimpleAgent;
import chalmers.dax021308.ecosystem.model.agent.SimplePredatorAgent;
import chalmers.dax021308.ecosystem.model.util.Position;
import chalmers.dax021308.ecosystem.model.util.Vector;

/**
 * 
 * @author Henrik
 * 
 */
public class DummyPredatorPopulation extends AbstractPopulation {
	
	
	private double maxSpeed;
	private double visionRange;
	
	

	public DummyPredatorPopulation(Dimension gridDimension,
			int initPopulationSize, Color color, double maxSpeed,
			double maxAcceleration, double visionRange) {
		super(gridDimension);
		this.visionRange = visionRange;
		agents = initializePopulation(initPopulationSize, gridDimension, color,
				maxSpeed, maxAcceleration, visionRange);
	}

	private List<IAgent> initializePopulation(int populationSize,
			Dimension gridDimension, Color color, double maxSpeed,
			double maxAcceleration, double visionRange) {
		List<IAgent> newAgents = new ArrayList<IAgent>(populationSize);
		
		for (int i = 0; i < populationSize; i++) {
			Position randPos = new Position(gridDimension.getWidth()
					* Math.random(), gridDimension.getHeight() * Math.random());
			Vector velocity = new Vector(maxSpeed, maxSpeed);
			while (Math.sqrt(Math.pow(velocity.getX(), 2)
					+ Math.pow(velocity.getY(), 2)) > maxSpeed) {
				velocity.setVector(-maxSpeed + Math.random() * 2 * maxSpeed,
						-maxSpeed + Math.random() * 2 * maxSpeed);
			}
			IAgent a = new SimplePredatorAgent("Big Hungry", randPos, color, 10,
					10, velocity, maxSpeed, maxAcceleration, visionRange);
			newAgents.add(a);
		}
		return newAgents;
	}

	@Override
	public double calculateFitness(IAgent agent) {
		// TODO Auto-generated method stub
		return 0;
	}
}
