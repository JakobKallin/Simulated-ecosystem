package chalmers.dax021308.ecosystem.model.population;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import chalmers.dax021308.ecosystem.model.agent.GrassAgent;
import chalmers.dax021308.ecosystem.model.agent.GrassPatch;
import chalmers.dax021308.ecosystem.model.agent.IAgent;
import chalmers.dax021308.ecosystem.model.util.IShape;
import chalmers.dax021308.ecosystem.model.util.Position;
import chalmers.dax021308.ecosystem.model.util.Vector;

/**
 * The population for the grass, the lowest part of the food chain
 * 
 * @author Henrik
 * 
 */
public class GrassPopulation extends AbstractPopulation {
	private List<GrassPatch> grass;

	public GrassPopulation(String name, Dimension gridDimension,
			int initPopulationSize, Color color, double maxSpeed,
			double maxAcceleration, double visionRange, int capacity,
			IShape shape) {
		super(name, gridDimension, shape);
		grass = new ArrayList<GrassPatch>();
		this.color = color;
		agents = initializePopulation(initPopulationSize, gridDimension, color,
				maxSpeed, capacity);
	}

	private List<IAgent> initializePopulation(int populationSize,
			Dimension gridDimension, Color color, double maxSpeed, int capacity) {
		int nrOfPatches = 10;
		for (int i = 0; i < nrOfPatches; i++) {
			grass.add(new GrassPatch(shape.getRandomPosition(gridDimension),
					gridDimension, capacity, getName(), color));
		}
		
		List<IAgent> newAgents = new ArrayList<IAgent>(populationSize * 100);
		for (int i = 0; i < populationSize; i++) {
			int rdm = (int) (Math.random() * 10);
			
			IAgent a = grass.get(rdm).createGrass(populationSize,
					gridDimension, shape);
			System.out.println("KOmmer vi hit? Vad �r rdm?" + rdm + " Size? " + grass.size());
			newAgents.add(a);
			

		}
		return newAgents;
	}

	@Override
	public double calculateFitness(IAgent agent) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updatePositions() {
		List<IAgent> grassStraws = new ArrayList<>();
		int populationSize = agents.size();
		for (GrassPatch patch : grass) {
			grassStraws.addAll(patch.update(populationSize, gridDimension, shape));
		}
		if (grassStraws != null){
			agents.addAll(grassStraws);
			wg.addAll(grassStraws);
		}

	}

	@Override
	public double getComputationalFactor() {
		return 25;
	}

}
