package chalmers.dax021308.ecosystem.controller;

import java.awt.Dimension;

import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
import chalmers.dax021308.ecosystem.view.SimulationView;

/**
 * Controller class
 * 
 * @author Henrik Ernstsson
 */
public class ToyController implements IController {

	private EcoWorld model;
	private SimulationView simView;

	public ToyController() {
		init();
	}

	@Override
	public void init() {
		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		d.height = d.height - 40;
		
		this.model = new EcoWorld(d, 17, Integer.MAX_VALUE);
		//Uncommend below to run without delay.
		//this.model = new EcoWorld(d);
		
		//Uncomment to start model.
		model.start();
		this.simView = new SimulationView(model, d, true);
		simView.init();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
	}

}
