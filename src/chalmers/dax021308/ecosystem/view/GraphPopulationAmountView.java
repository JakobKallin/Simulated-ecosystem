package chalmers.dax021308.ecosystem.view;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
import chalmers.dax021308.ecosystem.model.population.IPopulation;

import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.IAxis.*;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyManualTicks;
import info.monitorenter.gui.chart.rangepolicies.*;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import info.monitorenter.gui.util.ColorIterator;
import info.monitorenter.util.Range;

/**
 * 
 * @author Loanne
 * Shows population amount over time.
 * However, might not yet work for populations added after
 * simulation start. Will fix later if this should be possible.
 */
public class GraphPopulationAmountView implements IGraphView {

	private String name = "Population amount";
	private JFrame frame; // TODO temporary. 
	private Chart2D chart = new Chart2D();
	private List<ITrace2D> traces = new ArrayList<ITrace2D>();
	private long m_starttime = System.currentTimeMillis();
	private ColorIterator colors = new ColorIterator();
	
	// Values for axis. More values are set in init()
	private IAxis<IAxisScalePolicy> xAxis;
	private IAxis<IAxisScalePolicy> yAxis;
	private Range rangeX = new Range(0, 20000);
	private Range rangeY = new Range(0, 500);
	private String xAxisTitle = "Time (ms)";
	private String yAxisTitle = "Population amount";
	

	public GraphPopulationAmountView(EcoWorld model) {
		initJFrame(); // TODO temporary
		model.addObserver(this);  
	}

	private void initJFrame(){
		// TODO temporary stuff. Should just be a JPanel.
		frame = new JFrame(name);    
		this.frame.setSize(500, 500);
		this.frame.getContentPane().add(chart);
		this.frame.setVisible(true);
	}

	@Override
	public void init() {    
		this.colors.setStartColor(Color.BLUE);
		// Set how many different colored traces possible.
		this.colors.setSteps(10);

		xAxis = (IAxis<IAxisScalePolicy>)chart.getAxisX(); 
		xAxis.setAxisTitle(new AxisTitle(xAxisTitle));
		xAxis.setRangePolicy(new RangePolicyMinimumViewport(rangeX)); 
		xAxis.setMinorTickSpacing(2000);

		yAxis = (IAxis<IAxisScalePolicy>)chart.getAxisY(); 
		yAxis.setAxisTitle(new AxisTitle(yAxisTitle));
		yAxis.setRangePolicy(new RangePolicyMinimumViewport(rangeY)); 
		yAxis.setMinorTickSpacing(50);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		String eventName = event.getPropertyName();
		if(eventName == EcoWorld.EVENT_STOP) {
			//Model has stopped. Maybe hide view?
			//frame.setVisible(false);
		} else if(eventName == EcoWorld.EVENT_TICK) {

			if(event.getNewValue() instanceof List<?>) {

				List<IPopulation> newPops = (List<IPopulation>) event.getNewValue();

				if (traces.size() == 0) {
					createNewTraces(newPops);
				}

				// TODO update right population trace
				// update graph. 
				// For the moment, this assumes that the population list doesn't change after start.
				// 
				for (int i = 0; i < newPops.size(); ++i) {
					int numOfAgents = newPops.get(i).getAgents().size();
					traces.get(i).addPoint(((double) System.currentTimeMillis() - this.m_starttime), numOfAgents);					
				}	
			}
		}
	}

	private void createNewTraces(List<IPopulation> newPops){
		// used for naming, in case a population doesn't have a name.
		int popNum = 0; 

		for (IPopulation p: newPops) {		
			String pName = p.getName();	
			// if a population is unnamed.
			++popNum;
			if (pName == null) {
				pName = "Population " + popNum; 
			}
			ITrace2D newTrace = new Trace2DSimple(pName); 

			newTrace.setColor(colors.next());
			traces.add(newTrace);
			chart.addTrace(newTrace);	
		}
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
	
	/* TODO I don't know if we want this
	public void setRangeX(double min, double max) {
		this.rangeX.setMin(min);
		this.rangeX.setMax(max);	
		xAxis.setRangePolicy(new RangePolicyMinimumViewport(rangeX));
	}

	public void setRangeY(double min, double max) {
		this.rangeY.setMin(min);
		this.rangeY.setMax(max);
		yAxis.setRangePolicy(new RangePolicyMinimumViewport(rangeY));
	}
	 */




}
