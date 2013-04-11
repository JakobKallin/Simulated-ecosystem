package chalmers.dax021308.ecosystem.model.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import chalmers.dax021308.ecosystem.model.environment.obstacle.AbstractObstacle;
import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
import chalmers.dax021308.ecosystem.model.environment.obstacle.RectangularObstacle;

/**
 * Class for testing A* algorithm.
 * 
 * @author Erik Ramqvist
 *
 */
public class ShortestPathTester extends JPanel {
	private static final long serialVersionUID = 3766084045600317521L;
	private static final double HEURISTIC_UPSAMPLE = 1;
	private static final long ITERATION_TIME = 16;
	private static final Dimension simulationDimension = new Dimension(750, 750);
	
	private static HashSet<AStarPosition> closedSet;
	private static HashSet<AStarPosition> openSet;
	private static AStarPosition current;
	
	private static List<IObstacle> obsList = new ArrayList<IObstacle>();
	static {
		obsList.add(new RectangularObstacle(200, 50, new Position(300, 350), Color.GRAY));
		obsList.add(new RectangularObstacle(50, 200, new Position(400, 200), Color.GRAY));
	}
	
	private AStarPosition start;
	private AStarPosition goal;
	private List<Position> result;
	
	
	public ShortestPathTester() {
//		Position start = new Position(5.0, 5000.0);
//		Position end = new Position(5.0, -32.0);
		Position start = new Position(124.0, 121.0);
		Position end = new Position(510.0, 621.0); 
		goal = new AStarPosition(end.x, end.y);
		if(AbstractObstacle.isInsideObstacleList(obsList, start) || AbstractObstacle.isInsideObstacleList(obsList, end)) {
			Log.e("Either positions is inside obstacle.");
		}
		System.out.println("Distance: " + start.getDistance(end));
		long time;
		double elapsed;
		boolean executeHashSet = true;
		Stat<Double> stat = new Stat<Double>();
		if(executeHashSet ) {
			for(int i = 0; i < 1000; i++) {
				time = System.nanoTime();
				result = Position.getShortestPath(start, end, obsList);
				elapsed = (System.nanoTime() - time)*0.000001;
				System.out.println("HashSet Completed in: " + elapsed + " ms. Positions: " + result.size());
				stat.addObservation(elapsed);
			}
		} else {
			for(int i = 0; i < 10; i++) {
				time = System.nanoTime();
				result = Position.getShortestPathPriorityQueue(start, end, obsList);
				elapsed = (System.nanoTime() - time)*0.000001;
				System.out.println("PriorityQueue Completed in: " + elapsed + " ms. Positions: " + result.size());
				stat.addObservation(elapsed);
			}
		}
		Log.v("Mean value:" + stat.getMean());
		//		time = System.nanoTime();
//		result = Position.getShortestPath(start, end);
//		elapsed = (System.nanoTime() - time)*0.000001;
//		System.out.println("Optimal solution Completed in: " + elapsed + " ms. Positions: " + result.size() + " Result: " + result);
/*
		time = System.nanoTime();
		result = getShortestPath(start, end, obsList);
		elapsed = (System.nanoTime() - time)*0.000001;
		System.out.println("LinkedList Completed in: " + elapsed + " ms. Positions: " + result.size());*/

		JFrame frame = new JFrame();
		frame.setSize(new Dimension(750, 750));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		setBackground(Color.WHITE);
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		for(IObstacle o : obsList ) {
			if(o instanceof RectangularObstacle) {
				g.setColor(Color.GRAY);
				g.fillRect((int) (o.getPosition().getX()-o.getWidth()),(int)  ((o.getPosition().getY()-o.getHeight()+10)),(int) o.getWidth()*2 ,(int) o.getHeight()*2 );
			}
		}
		if(closedSet != null) {
			for(AStarPosition a : closedSet) {
				drawAStarPosition(a, g, Color.BLACK);
			}
		}
		if(openSet != null) {
			for(AStarPosition a : openSet) {
				drawAStarPosition(a, g, Color.BLUE);
				
			}
		}
		if(current != null) {
			drawAStarPosition(current, g, Color.GREEN);
		}

		if(result != null) {
			for(Position a : result) {
				drawAStarPosition(a, g, Color.RED);
				
			}
		}

		if(goal != null) {
			drawAStarPosition(goal, g, Color.RED);
		}
	}

	
	
	private void drawAStarPosition(Position p, Graphics g, Color c) {
		//draw position
		g.setColor(c);
		g.fillRect((int) p.getX(),(int)  p.getY(), 10, 10);
	}
	
	public List<Position> getShortestPath(Position startPos, Position endPos, List<IObstacle> obsList) {
		double distance = startPos.getDistance(endPos);
		if(distance > 500) {
			double x = Math.round((endPos.getX() * 0.1))*10;
			double y = Math.round((endPos.getY() * 0.1))*10;
			Position newEnd = new Position(x, y);
			x = Math.round((startPos.getX() * 0.1))*10;
			y = Math.round((startPos.getY() * 0.1))*10;
			Position newStartPos = new Position(x, y);
			return getShortestPathPriorityQueue(newStartPos, newEnd, obsList, 10);
		} else if(distance > 55) {
			return getShortestPathPriorityQueue(startPos, endPos, obsList, 1);
		} else {
			//Execute hashset version of shortestpath.
			return getShortestPathPriorityQueue(startPos, endPos, obsList, 1);
		}
	}
	
	/**
	 * Calculates the shortest path to the target using A* search algorithm.
	 * <p>
	 * Fast for long distances.
	 * <p>
	 * TODO: Supply a obstacle-list and Shape?
	 * TODO: Make private when finished.
	 * 
	 * For use with target agents behind obstacles.
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">http://en.wikipedia.org/wiki/A*_search_algorithm</a>
	 * @param target
	 * @return
	 * @author Erik Ramqvist
	 */
	public List<Position> getShortestPathPriorityQueue(Position startPos, Position endPos, List<IObstacle> obsList, int coordinateUpsampling/*, IShape simShape*/) {
		start = new AStarPosition(startPos.getX(), startPos.getY());
		goal = new AStarPosition(endPos.getX(), endPos.getY());
		start.g_score = 0;
		start.f_score = heuristic_manhattan_distance(start, goal)*HEURISTIC_UPSAMPLE;
		closedSet = new HashSet<AStarPosition>();
		PriorityQueue<AStarPosition> openSet = new PriorityQueue<AStarPosition>(50, new AStarPositionComparator());
		openSet.add(start);
		current = start;//the node in openset having the lowest f_score[] value
		while(!openSet.isEmpty()) {
			Log.v("openset" + openSet);
			Log.v("closedSet" + closedSet);
//			Log.v("g_score" + g_score);
//			Log.v("f_score" + f_score);
			Log.v("current" + current);
			Log.v("--------");
			current = openSet.poll();
			
			//Slow down to see movement.
			
			repaint();
			try {
				Thread.sleep(ITERATION_TIME);
			} catch (InterruptedException e) {
				
			}
			closedSet.add(current);
			if(current.equals(goal)) {
				return reconstructPath(current);
			}
			for(AStarPosition neighbour : getNeighbours(current, obsList, coordinateUpsampling)) {
				double tentative_g_score = current.g_score + 1;
				if(closedSet.contains(neighbour)) {
					if(tentative_g_score >= neighbour.g_score) {
						continue;
					}
				}
				if(!openSet.contains(neighbour) || tentative_g_score < neighbour.g_score) {
					neighbour.came_from = current;
					neighbour.g_score = tentative_g_score;
					neighbour.f_score = neighbour.g_score + heuristic_manhattan_distance(goal, neighbour)*HEURISTIC_UPSAMPLE;
					if(!openSet.contains(neighbour)) {
						openSet.add(neighbour);
					}
				}
			}
		}	
		//Failure to find path.
		Log.e("Failed to find path to target!");
		return Collections.emptyList();
	}
	

	/**
	 * Calculates the shortest path to the target using A* search algorithm.
	 * <p>
	 * Fast for short distances.
	 * <p>
	 * TODO: Supply a obstacle-list and Shape?
	 * TODO: Make private when finished.
	 * 
	 * For use with target agents behind obstacles.
	 * 
	 * @see <a
	 *      href="http://en.wikipedia.org/wiki/A*_search_algorithm">http://en.wikipedia.org/wiki/A*_search_algorithm</a>
	 * @param target
	 * @return
	 * @author Erik Ramqvist
	 */
	public List<Position> getShortestPathHashSet(Position startPos,
			Position endPos, List<IObstacle> obsList/*, IShape simShape */) {
		AStarPosition start = new AStarPosition(startPos.getX(), startPos.getY());
		AStarPosition goal = new AStarPosition(endPos.getX(), endPos.getY());
		closedSet = new HashSet<AStarPosition>();
		openSet = new HashSet<AStarPosition>();
		openSet.add(start);
		start.g_score = 0.0;
		start.f_score = start.getDistance(goal)*HEURISTIC_UPSAMPLE;
		AStarPosition current = start;// the node in openset having the lowest
									// f_score[] value
		double lowScore = Integer.MAX_VALUE;
		while (!openSet.isEmpty()) {
			// Get the AStarPosition with the lowest estimated distance to target.
			lowScore = Integer.MAX_VALUE;
			for (AStarPosition n : openSet) {
				double score = n.f_score;
				if (score < lowScore) {
					current = n;
					lowScore = score;
				} else if(score == lowScore) {
					if(n != current) {
						current = n;
					}
				}
			}

			repaint();
			try {
				Thread.sleep(ITERATION_TIME);
			} catch (InterruptedException e) {
				
			}
			 Log.v("openset" + openSet);
//			 Log.v("closedSet" + closedSet);
//			 Log.v("g_score" + current.g_score);
//			 Log.v("f_score" + current.f_score);
			 Log.v("current: " + current);
			 Log.v("--------");
			if (current.equals(goal)) {
				return reconstructPath(current);
			}
			openSet.remove(current);
			closedSet.add(current);
			for (AStarPosition neighbour : getNeighbours(current, obsList)) {
				double tentative_g_score = current.g_score + 1;
				if (closedSet.contains(neighbour)) {
					if (tentative_g_score >= neighbour.g_score) {
						continue;
					}
				}
				if (!openSet.contains(neighbour) || tentative_g_score < neighbour.g_score) {
					neighbour.came_from = current;
					neighbour.g_score = tentative_g_score;
					neighbour.f_score = tentative_g_score + neighbour.getDistance(goal)*HEURISTIC_UPSAMPLE;
					if (!openSet.contains(neighbour)) {
						openSet.add(neighbour);
					}
				}
				Log.e(neighbour.toString());
			}
		}
		// Failure to find path.
		Log.e("Failed to find path to target!");
		return Collections.emptyList();
	}
	

	private static List<Position> reconstructPath(AStarPosition current_node) {
		List<Position> result = new ArrayList<Position>();
		AStarPosition current = current_node;
		while(current != null) {
			result.add(current);
			current = current.came_from;
		}
	    Collections.reverse(result);
		return result;
	}
	
	private static class AStarPositionComparator implements Comparator<AStarPosition> {

		@Override
		public int compare(AStarPosition o1, AStarPosition o2) {
			if(o1.getGF() > o2.getGF()) {
				return 1;
			} else if(o1.getGF() < o2.getGF()) {
				return -1;
			}
			return 0;
		}
	}
	

	/**
	 * Gets the neighbours of one AStarPosition.
	 * <p>
	 * Is shape really needed here? Since we guarantee Agents are inside the shape.
	 * 
	 * @param p1
	 * @param obsList
	 * @param shape
	 * @return
	 */
	public static List<AStarPosition> getNeighbours(AStarPosition p1, List<IObstacle> obsList/*, IShape shape*/) {
		List<AStarPosition> neighbours = new ArrayList<AStarPosition>(8);
		if(obsList.isEmpty()) {
			neighbours.add(new AStarPosition(p1.getX(), 	p1.getY()+1));
			neighbours.add(new AStarPosition(p1.getX()+1, p1.getY()));
			neighbours.add(new AStarPosition(p1.getX()-1, p1.getY()));
			neighbours.add(new AStarPosition(p1.getX(), 	p1.getY()-1));
			neighbours.add(new AStarPosition(p1.getX()+1, p1.getY()-1));
			neighbours.add(new AStarPosition(p1.getX()-1, p1.getY()+1));
			neighbours.add(new AStarPosition(p1.getX()+1, p1.getY()+1));
			neighbours.add(new AStarPosition(p1.getX()-1, p1.getY()-1));
		} else {
			double x = p1.getX();
			double y = p1.getY();
			AStarPosition p2 = new AStarPosition(x, y+1);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x+1, y);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x-1, y);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x, y-1);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x+1, y-1);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x-1, y+1);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x+1, y+1);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x-1, y-1);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			
		}
		return neighbours;
	}
	
	


	/**
	 * Gets the neighbours of one AStarPosition.
	 * <p>
	 * Is shape really needed here? Since we guarantee Agents are inside the shape.
	 * 
	 * @param p1
	 * @param obsList
	 * @param shape
	 * @return
	 */
	public static List<AStarPosition> getNeighbours(AStarPosition p1, List<IObstacle> obsList, int upsampleRate/*, IShape shape*/) {
		List<AStarPosition> neighbours = new ArrayList<AStarPosition>(8);
		if(obsList.isEmpty()) {
			neighbours.add(new AStarPosition(p1.getX(), 	p1.getY()+upsampleRate));
			neighbours.add(new AStarPosition(p1.getX(), 	p1.getY()-upsampleRate));
			neighbours.add(new AStarPosition(p1.getX()+upsampleRate, p1.getY()));
			neighbours.add(new AStarPosition(p1.getX()-upsampleRate, p1.getY()));
			neighbours.add(new AStarPosition(p1.getX()+upsampleRate, p1.getY()-upsampleRate));
			neighbours.add(new AStarPosition(p1.getX()-upsampleRate, p1.getY()+upsampleRate));
			neighbours.add(new AStarPosition(p1.getX()+upsampleRate, p1.getY()+upsampleRate));
			neighbours.add(new AStarPosition(p1.getX()-upsampleRate, p1.getY()-upsampleRate));
		} else {
			double x = p1.getX();
			double y = p1.getY();
			AStarPosition p2 = new AStarPosition(x, y+upsampleRate);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x+upsampleRate, y);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x-upsampleRate, y);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x, y-upsampleRate);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x+upsampleRate, y-upsampleRate);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x-upsampleRate, y+upsampleRate);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x+upsampleRate, y+upsampleRate);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			p2 = new AStarPosition(x-upsampleRate, y-upsampleRate);
			if(!AbstractObstacle.isInsideObstacleList(obsList, p2)) {
				neighbours.add(p2);
			}
			
		}
		return neighbours;
	}
	
	
	/**
	 * Internal Position class for use with A* shortest path algorithm.
	 * 
	 * @author Erik
	 *
	 */
	private static class AStarPosition extends Position {
		double g_score;
		double f_score;
		AStarPosition came_from;
		public AStarPosition(double x, double y) {
			super(x,y);
		}

		public double getGF() {
			return g_score+f_score;
		}
		
		@Override
		public String toString() {
			return "AStarPosition G: " + g_score + " F: " + f_score + " " + super.toString();
		}
	}

	
	@SuppressWarnings("unused")
	private static double heuristic_vector_distance(AStarPosition start, AStarPosition goal /*, List<IObstacle> obsList, IShape simShape*/) {
	    return Math.sqrt(Math.pow(Math.abs(goal.getX() - start.getX()), 2) + Math.pow(Math.abs(goal.getY() - start.getY()), 2));
	}
	
    public static double heuristic_manhattan_distance(AStarPosition a, AStarPosition b /*, List<IObstacle> obsList, IShape simShape*/){
      //  return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
        return a.getDistance(b);
    }	
	
	public static void main(String[] args) {
		new ShortestPathTester();
	}
}
