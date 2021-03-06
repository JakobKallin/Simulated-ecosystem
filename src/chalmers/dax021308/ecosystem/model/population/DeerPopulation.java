package chalmers.dax021308.ecosystem.model.population;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import chalmers.dax021308.ecosystem.model.agent.IAgent;
import chalmers.dax021308.ecosystem.model.agent.DeerAgent;
import chalmers.dax021308.ecosystem.model.environment.SurroundingsSettings;
import chalmers.dax021308.ecosystem.model.genetics.GeneralGeneTypes;
import chalmers.dax021308.ecosystem.model.genetics.GeneticSettings;
import chalmers.dax021308.ecosystem.model.genetics.GenomeFactory;
import chalmers.dax021308.ecosystem.model.genetics.IGene;
import chalmers.dax021308.ecosystem.model.genetics.IGenome;
import chalmers.dax021308.ecosystem.model.util.Position;
import chalmers.dax021308.ecosystem.model.util.Vector;

/**
 *
 * @author Sebastian
 *
 */
public class DeerPopulation extends AbstractPopulation {

	public DeerPopulation(List<IAgent> agentList) {
		agents = agentList;
	}

	public DeerPopulation(String name, int initPopulationSize, Color color, double maxSpeed,
			double maxAcceleration, double visionRange, boolean groupBehaviour, SurroundingsSettings surroundings) {

		super(name, color, surroundings);
		//this.groupBehaviour = groupBehaviour;
		agents = initializePopulation(initPopulationSize, SurroundingsSettings.getGridDimension(), color,
				maxSpeed, maxAcceleration, visionRange);
	}

	private List<IAgent> initializePopulation(int populationSize,
			Dimension gridDimension, Color color, double maxSpeed,
			double maxAcceleration, double visionRange) {
		List<IAgent> newAgents = new ArrayList<IAgent>(populationSize);
		addNeutralPopulation(this);
		for (int i = 0; i < populationSize; i++) {
			Position randPos = getRandomPosition();
			Vector velocity = new Vector(maxSpeed, maxSpeed);

			// Create a random vector (uniformly) inside a circle with radius
			// maxSpeed.
			while (velocity.getNorm() > maxSpeed) {
				velocity.setVector(-maxSpeed + Math.random() * 2 * maxSpeed,
						-maxSpeed + Math.random() * 2 * maxSpeed);
			}

			IGenome<GeneralGeneTypes, IGene> genome = GeneticSettings.preySettings.getGenome().getCopy();
			// TODO comment this during sim?
			genome.getGene(GeneralGeneTypes.ISGROUPING).isGeneActive();
			genome.getGene(GeneralGeneTypes.GROUPING_COHESION).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.GROUPING_SEPARATION_FACTOR).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.GROUPING_ARRAYAL_FORCE).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.GROUPING_FORWARD_THRUST).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.ISSTOTTING).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.STOTTINGRANGE).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.STOTTINGLENGTH).setRandomStartValue(false);
			genome.getGene(GeneralGeneTypes.STOTTINGANGLE).setRandomStartValue(false);


			IAgent a = new DeerAgent("Deer", randPos,
					color, 5, 10, velocity, maxSpeed, maxAcceleration,
					visionRange, genome);
			/*
			 * IAgent a = new DeerAgent("Deer", randPos,
					color, 5, 10, velocity, maxSpeed, maxAcceleration,
					visionRange, GenomeFactory.deerGenomeFactory());
			 */

			newAgents.add(a);
		}
		return newAgents;
	}

	public double stottingProportion(){
		double stottingCount = 0;
		DeerAgent da = null;
		for(IAgent a: agents) {
			if(a instanceof DeerAgent) {
				da = (DeerAgent) a;
			}
			if(da.isAStottingDeer()){
				stottingCount++;
			}
		}
		return stottingCount/((double)agents.size());
	}

	public double agentsGroupingProportion(){
		double groupCount = 0;
		DeerAgent da = null;
		for(IAgent a: agents) {
			if(a instanceof DeerAgent) {
				da = (DeerAgent) a;
			}
			if(da.isAGroupingDeer()){
				groupCount++;
			}
		}
		return groupCount/((double)agents.size());
	}

	@Override
	public void updatePositions() {
		super.updatePositions();
		interestingPropertyProportion = agentsGroupingProportion();
	}
}
