package chalmers.dax021308.ecosystem.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import sun.misc.Cleaner;

import chalmers.dax021308.ecosystem.model.EcoWorld;
import chalmers.dax021308.ecosystem.model.IAgent;
import chalmers.dax021308.ecosystem.model.IObstacle;
import chalmers.dax021308.ecosystem.model.IPopulation;
import chalmers.dax021308.ecosystem.model.util.Log;
import chalmers.dax021308.ecosystem.model.util.Position;

public class SimulationView extends JPanel implements IView {
	
	private JFrame frame;
	private List<IPopulation> newPops;
	private List<IObstacle> newObs;
	private Random ran = new Random();
	private Dimension gridDimension;
	/**
	 * Create the panel.
	 */
	public SimulationView(EcoWorld model, Dimension size) {
		model.addObserver(this);
		frame = new JFrame("Simulation View");
		frame.add(this);
		frame.setSize(size);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		this.setBackground(Color.white);
		gridDimension = size;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String eventName = event.getPropertyName();
		if(eventName == EcoWorld.EVENT_STOP) {
			//Model has stopped. Maybe hide view?
			//frame.setVisible(false);
		} else if(eventName == EcoWorld.EVENT_TICK) {
			//Tick notification recived from model. Do something with the data.
			if(event.getNewValue() instanceof List<?>) {
				this.newPops = (List<IPopulation>) event.getNewValue();
			}
			if(event.getOldValue() instanceof List<?>) {
				this.newObs = (List<IObstacle>) event.getOldValue();
			}
			removeAll();
			repaint();
			revalidate();
		}
	}
	
	@Override
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Log.v("invalidate");
		for(IPopulation pop : newPops) {
			for(IAgent a : pop.getAgents()) {
				Position p = a.getPosition();
				g.setColor(a.getColor());
		        g.fillOval((int)(p.getX()), (int) (frame.getSize().getHeight() - p.getY()), a.getHeight(), a.getWidth());
			}
		}
		
		g.setColor(Color.black);
		int xLeft = 0;
		int xRight = (int)gridDimension.getWidth();
		int yBot = (int)(frame.getSize().getHeight());
		int yTop = (int)(frame.getSize().getHeight())-(int)gridDimension.getHeight();
		g.drawLine(xLeft, yBot, 
				   xLeft, yTop);
		g.drawLine(xLeft, yTop, 
				   xRight, yTop);
		g.drawLine(xRight, yTop,
				   xRight, yBot);
		g.drawLine(xRight, yBot,
				   xLeft, yBot);
    }

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addController(ActionListener controller) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

}
