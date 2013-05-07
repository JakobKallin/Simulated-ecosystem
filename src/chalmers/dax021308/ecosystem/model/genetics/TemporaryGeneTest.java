package chalmers.dax021308.ecosystem.model.genetics;

import chalmers.dax021308.ecosystem.model.genetics.GeneticSettings.GenomeSpecification;
import chalmers.dax021308.ecosystem.model.genetics.newV.IGene;
import chalmers.dax021308.ecosystem.model.genetics.newV.IGenome;

public class TemporaryGeneTest {
	
	private static int nIterations = 100;
	private static double mutationRate = 0.1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IGenome<GeneralGeneTypes, IGene> genome = GenomeFactory.deerGenomeFactory();
		genome.getGene(GeneralGeneTypes.GROUPING_SEPARATION_FACTOR).setMutable(true);
		genome.getGene(GeneralGeneTypes.GROUPING_COHESION).setMutable(true);
		genome.getGene(GeneralGeneTypes.GROUPING_ARRAYAL_FORCE).setMutable(true);
		genome.getGene(GeneralGeneTypes.GROUPING_FORWARD_THRUST).setMutable(true);
		
		genome.getGene(GeneralGeneTypes.GROUPING_SEPARATION_FACTOR).setHasRandomStartValue(true);
		genome.getGene(GeneralGeneTypes.GROUPING_COHESION).setHasRandomStartValue(true);
		genome.getGene(GeneralGeneTypes.GROUPING_ARRAYAL_FORCE).setHasRandomStartValue(true);
		genome.getGene(GeneralGeneTypes.GROUPING_FORWARD_THRUST).setHasRandomStartValue(true);
		
		IGenome<GeneralGeneTypes, IGene> deerGenome = genome.getCopy();
		
		for(int i=0; i<nIterations; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(deerGenome.getGene(GeneralGeneTypes.GROUPING_SEPARATION_FACTOR).getCurrentValue());
			sb.append(" | ");
			sb.append(deerGenome.getGene(GeneralGeneTypes.GROUPING_COHESION).getCurrentValue());
			sb.append(" | ");
			sb.append(deerGenome.getGene(GeneralGeneTypes.GROUPING_ARRAYAL_FORCE).getCurrentValue());
			sb.append(" | ");
			sb.append(deerGenome.getGene(GeneralGeneTypes.GROUPING_FORWARD_THRUST).getCurrentValue());
			System.out.println(sb.toString());
			deerGenome = deerGenome.onlyMutate();
		}
		
	}

}
