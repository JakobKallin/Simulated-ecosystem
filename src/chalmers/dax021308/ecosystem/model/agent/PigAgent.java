package chalmers.dax021308.ecosystem.model.agent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import chalmers.dax021308.ecosystem.model.environment.SurroundingsSettings;
import chalmers.dax021308.ecosystem.model.environment.obstacle.AbstractObstacle;
import chalmers.dax021308.ecosystem.model.population.IPopulation;
import chalmers.dax021308.ecosystem.model.util.AgentPath;
import chalmers.dax021308.ecosystem.model.util.Container;
import chalmers.dax021308.ecosystem.model.util.ForceCalculator;
import chalmers.dax021308.ecosystem.model.util.Log;
import chalmers.dax021308.ecosystem.model.util.Position;
import chalmers.dax021308.ecosystem.model.util.Vector;

/**
 * Pig Agent.
 *
 * @author Group 8, path finding edits by Erik Ramqvist
 */
public class PigAgent extends AbstractAgent {

	private static final int MAX_ENERGY = 1000;
	private static final int MAX_LIFE_LENGTH = Integer.MAX_VALUE;
	private boolean hungry = true;
	private static final double REPRODUCTION_RATE = 0.1;
	private boolean willFocusPreys = true;
	private static final int DIGESTION_TIME = 10;
	private int digesting = 0;
	private double STOTTING_RANGE = 20;
	private double STOTTING_LENGTH = 30;
	private double STOTTING_COOLDOWN = 150;
	private double stottingDuration = STOTTING_LENGTH;
	private double stottingCoolDown = 0;
	private boolean isAStottingDeer = true;
	private boolean isStotting = false;
	private Vector stottingVector = new Vector();
	private boolean alone;

	/*public PigAgent(String name, Position p, Color c, int width, int height,
			Vector velocity, double maxSpeed, double maxAcceleration,
			double visionRange, boolean groupBehaviour, List<IObstacle> obsList) {
		super(name, p, c, width, height, velocity, maxSpeed, visionRange,
				maxAcceleration);
		this.obstacles = obsList; // TODO Is this necessary?
		this.energy = MAX_ENERGY;
		this.groupBehaviour = groupBehaviour;
		this.focusedPreyPath = new AgentPath();

	}*/

	// TODO I removed obsList.
	public PigAgent(String name, Position p, Color c, int width, int height,
			Vector velocity, double maxSpeed, double maxAcceleration,
			double visionRange, boolean groupBehaviour) {
		super(name, p, c, width, height, velocity, maxSpeed, visionRange,
				maxAcceleration);
		this.energy = MAX_ENERGY;
		this.groupBehaviour = groupBehaviour;
		this.focusedPreyPath = new AgentPath();

	}

	@Override
	public List<IAgent> reproduce(IAgent agent, int populationSize,
			SurroundingsSettings surroundings) {
		if (hungry)
			return null;
		else {
			hungry = true;
			List<IAgent> spawn = new ArrayList<IAgent>();
			if (Math.random() < REPRODUCTION_RATE) {
				Position pos;
				do {
					double xSign = Math.signum(-1 + 2 * Math.random());
					double ySign = Math.signum(-1 + 2 * Math.random());
					double newX = this.getPosition().getX() + xSign
							* (0.001 + 0.001 * Math.random());
					double newY = this.getPosition().getY() + ySign
							* (0.001 + 0.001 * Math.random());
					pos = new Position(newX, newY);
				} while (!SurroundingsSettings.getWorldShape().isInside(SurroundingsSettings.getGridDimension(), pos));
				/*IAgent child = new PigAgent(name, pos, color, width, height,
						new Vector(velocity), maxSpeed, maxAcceleration,
						visionRange, groupBehaviour, surroundings.getObstacles());*/
				IAgent child = new PigAgent(name, pos, color, width, height,
						new Vector(velocity), maxSpeed, maxAcceleration,
						visionRange, groupBehaviour);
				spawn.add(child);

			}
			return spawn;
		}
	}

	/**
	 * Calculates the next position of the agent depending on the forces that
	 * affects it. Note: The next position is not set until updatePosition() is
	 * called.
	 *
	 * @author Sebbe
	 */
	@Override
	public void calculateNextPosition(List<IPopulation> predators,
			List<IPopulation> preys, List<IPopulation> neutral,
			SurroundingsSettings surroundings) {

		updateNeighbourList(neutral, preys, predators);
		Vector predatorForce = getPredatorForce();
		Vector preyForce = ForceCalculator.getPreyForce(willFocusPreys, surroundings, focusedPrey,
				this, preyNeighbours, visionRange,
				focusedPreyPath, 10);
		if (predatorForce.isNullVector())
			alone = true;
		if (digesting > 0 && alone) {
			digesting--;
		} else {
			Vector mutualInteractionForce = new Vector();
			Vector forwardThrust = new Vector();
			Vector arrayalForce = new Vector();
			if (groupBehaviour) {
				mutualInteractionForce = ForceCalculator.mutualInteractionForce(
						neutralNeighbours, this, separationConstant, cohesionConstant);
				forwardThrust = ForceCalculator.forwardThrust(velocity, forwardThrustConstant);
				arrayalForce = ForceCalculator.arrayalForce(neutralNeighbours, this, arrayalConstant);
			}

			Vector environmentForce = ForceCalculator.getEnvironmentForce(SurroundingsSettings.getGridDimension(), SurroundingsSettings.getWorldShape(),
					position);
			Vector obstacleForce = ForceCalculator.getObstacleForce(SurroundingsSettings.getObstacles(), position);

			/*
			 * Sum the forces from walls, predators and neutral to form the
			 * acceleration force. If the acceleration exceeds maximum
			 * acceleration --> scale it to maxAcceleration, but keep the
			 * correct direction of the acceleration.
			 */
			Vector acceleration;
			if (isAStottingDeer && isStotting) {
				acceleration = predatorForce;
			} else {
				acceleration = predatorForce.multiply(5)
						.add(mutualInteractionForce)
						.add(forwardThrust)
						.add(arrayalForce);
				// if (alone) {
				//Vector preyForce = getPreyForce(shape, gridDimension, obstacles, focusedPreyPath, 20);
				acceleration.add(preyForce.multiply(10 * (1 - energy
						/ MAX_ENERGY)));
			}
			// }
			double accelerationNorm = acceleration.getNorm();
			if (accelerationNorm > maxAcceleration) {
				acceleration.multiply(maxAcceleration / accelerationNorm);
			}

			acceleration.add(environmentForce).add(obstacleForce);

			/*
			 * The new velocity is then just: v(t+dt) = (v(t)+a(t+1)*dt)*decay,
			 * where dt = 1 in this case. There is a decay that says if they are
			 * not affected by any force, they will eventually stop. If speed
			 * exceeds maxSpeed --> scale it to maxSpeed, but keep the correct
			 * direction.
			 */
			Vector newVelocity = Vector.addVectors(this.getVelocity(),
					acceleration);
			newVelocity.multiply(VELOCITY_DECAY);
			double speed = newVelocity.getNorm();
			if (speed > maxSpeed) {
				newVelocity.multiply(maxSpeed / speed);
			}
			// if (alone) {
			// newVelocity.multiply(0.9);
			// }
			this.setVelocity(newVelocity);

			/* Reusing the same position object, for less heap allocations. */
			// if (reUsedPosition == null) {
			nextPosition = Position.positionPlusVector(position, velocity);
			// } else {
			// nextPosition.setPosition(reUsedPosition.setPosition(position.getX()
			// + velocity.x, position.getY() + velocity.y));
			// }
		}
	}

// TODO ändrade denna metod. Den använde sig av obstacles i AbstractAgent, men tror den ej skulle det.
	// metoden används visserligen ej förtillfället.
	/**
	 * Special version of getPreyForce. Determines the shortest path if any obstacles is blocking the way.
	 * <p>
	 * The path_ttl value should scale with the distance to the target.
	 * <p>
	 * @return returns The force the preys attracts the agent with
	 * @author Sebastian/Henrik/Erik
	 */
	private Vector getPreyForce(SurroundingsSettings surroundings, Container<IAgent> focusedPreyContainer, AgentPath path, int initial_ttl) {
	//private Vector getPreyForce(IShape shape, Dimension dim, List<IObstacle> obsList, Container<IAgent> focusedPreyContainer, AgentPath path, int initial_ttl){
		if (willFocusPreys && focusedPreyContainer.get() != null && focusedPreyContainer.get().isAlive()) {
			Position p = focusedPreyContainer.get().getPosition();
			double distance = getPosition().getDistance(p);
			if (distance <= EATING_RANGE) {
				if (focusedPreyContainer.get().tryConsumeAgent()) {
					focusedPreyContainer.set(null);
					hungry = false;
					energy = MAX_ENERGY;
					digesting = DIGESTION_TIME;
				}
			} else {
				if(!focusedPreyPath.isEmpty() && focusedPreyPath.isValid()) {
					Position nextPathPosition = focusedPreyPath.peek();
					focusedPreyPath.decreaseTTL();

					//If we are not near our current path target, move towards it.
					if(position.getDistance(nextPathPosition) > EATING_RANGE) {
						return new Vector(nextPathPosition, position);
					} else if(focusedPreyPath.size() > 1) {
						//Remove the next path, we are close to it, and go to next.
						focusedPreyPath.pop();
						return new Vector(focusedPreyPath.peek(), position);
					} else if(focusedPreyPath.size() == 1) {
						return new Vector(focusedPreyPath.pop(), position);
					}
				} else {
					if(AbstractObstacle.isInsidePathList(SurroundingsSettings.getObstacles(), position, focusedPreyContainer.get().getPosition())) {
						//focusedPreyPath.setPath(Position.getShortestPath(position, focusedPreyContainer.get().getPosition(), obstacles, surroundings.getWorldShape(), surroundings.getGridDimension()), initial_ttl);
						focusedPreyPath.setPath(
								Position.getShortestPath(position, focusedPreyContainer.get().getPosition(),
										SurroundingsSettings.getObstacles(), SurroundingsSettings.getWorldShape(),
										SurroundingsSettings.getGridDimension(), surroundings.getObstacleSafetyDistance()),
										initial_ttl);
						if(focusedPreyPath.isEmpty()) {
							//Unreachable target.
							return Vector.emptyVector();
						} else {
							return new Vector(focusedPreyPath.peek(), position);
						}
					} else {
						focusedPreyPath.clearPath();
						return new Vector(focusedPreyContainer.get().getPosition(), position);
					}
				}
			}
		}

		Vector preyForce = new Vector(0, 0);
		IAgent closestFocusPrey = null;
		int preySize = preyNeighbours.size();
		for (int i = 0; i < preySize; i++) {
			IAgent a = preyNeighbours.get(i);
			Position p = a.getPosition();
			double distance = getPosition().getDistance(p);
			if (distance <= visionRange) {
				if (distance <= EATING_RANGE) {
					if (a.tryConsumeAgent()) {
						hungry = false;
						energy = MAX_ENERGY;
						digesting = DIGESTION_TIME;
					}
				} else if (willFocusPreys && distance <= FOCUS_RANGE) {
					if (closestFocusPrey != null && a.isAlive()) {
						if (closestFocusPrey.getPosition().getDistance(
								this.position) > a.getPosition().getDistance(
								this.position)) {
							closestFocusPrey = a;
						}
					} else {
						closestFocusPrey = a;
					}
				} else if (closestFocusPrey == null) {
					/*
					 * Create a vector that points towards the prey.
					 */
					Vector newForce = new Vector(p, position);

					/*
					 * Add this vector to the prey force, with proportion to how
					 * close the prey is. Closer preys will affect the force
					 * more than those far away.
					 */
					double norm = newForce.getNorm();
					preyForce.add(newForce.multiply(1 / (norm * distance)));
				}
			}
		}

		double norm = preyForce.getNorm();
		if (norm != 0) {
			preyForce.multiply(maxAcceleration / norm);
		}

		if (willFocusPreys && closestFocusPrey != null) {
			focusedPreyContainer.set(closestFocusPrey);
			if(AbstractObstacle.isInsidePathList(SurroundingsSettings.getObstacles(), position, focusedPreyContainer.get().getPosition())) {
				focusedPreyPath.setPath(Position.getShortestPath(position, focusedPreyContainer.get().getPosition(), SurroundingsSettings.getObstacles(), SurroundingsSettings.getWorldShape(), SurroundingsSettings.getGridDimension(), 5), initial_ttl);
				if(focusedPreyPath.isEmpty()) {
					return Vector.emptyVector();
				} else {
					return new Vector(focusedPreyPath.peek(), position);
				}
			} else {
				focusedPreyPath.clearPath();
				return new Vector(focusedPreyContainer.get().getPosition(), position);
			}

		}
		return preyForce;
	}


	private Vector getPreyForce(SurroundingsSettings surroundings, Container<IAgent> focusedPreyContainer, int initial_ttl) {
		if (willFocusPreys && focusedPreyContainer.get() != null && focusedPreyContainer.get().isAlive()) {
			Position p = focusedPreyContainer.get().getPosition();
			double distance = this.getPosition().getDistance(p);
			//double size = (agent.getHeight() + agent.getWidth()) / 4;
			double size = 0;
			if (distance <= EATING_RANGE - size) {
				if (focusedPreyContainer.get().tryConsumeAgent()) {
					focusedPreyContainer.set(null);
					this.eat();
				}
			} else {
				if(!focusedPreyPath.isEmpty() && focusedPreyPath.isValid()) {
					Position nextPathPosition = focusedPreyPath.peek();
					if(nextPathPosition.getDistance(position) < maxSpeed) {
						if(focusedPreyPath.size() > 1) {
							//Remove the next path, we are close to it, and go to next.
//							System.out.println("Go to next point in path.");
							focusedPreyPath.pop();
							nextPathPosition = focusedPreyPath.peek();
						} else {
//							System.out.println("Last point in path reached.");
							nextPathPosition = focusedPreyPath.pop();
						}
					}
					focusedPreyPath.decreaseTTL();

//					System.out.println(focusedPreyPath.getTTL());
//					System.out.println(position + " to " +nextPathPosition);
					return new Vector(nextPathPosition, position);
				} else {
					if(AbstractObstacle.isInsidePathList(SurroundingsSettings.getObstacles(), position, focusedPreyContainer.get().getPosition())) {
						focusedPreyPath.setPath(
								Position.getShortestPath(position, focusedPreyContainer.get().getPosition(),
										SurroundingsSettings.getObstacles(), SurroundingsSettings.getWorldShape(),
										SurroundingsSettings.getGridDimension(), surroundings.getObstacleSafetyDistance())
								,initial_ttl);
						if(focusedPreyPath.isEmpty()) {
//							System.out.println("NULL VECTOR");
							return Vector.emptyVector();
						} else {
//							System.out.println("New Path");
//							System.out.println(position);
//							System.out.println(focusedPreyPath);
							focusedPreyPath.pop();
							return new Vector(focusedPreyPath.peek(), position);
						}
					} else {
//						System.out.println("No Path");
						return new Vector(focusedPreyContainer.get().getPosition(), position);
					}
				}
			}
		}

		Vector preyForce = new Vector(0, 0);
		IAgent closestFocusPrey = null;
		int nrOfPreys = preyNeighbours.size();
		for (int i = 0; i < nrOfPreys; i++) {
			IAgent a = preyNeighbours.get(i);
			Position p = a.getPosition();
//			double preySize = (a.getHeight() + a.getWidth()) / 4;
			double distance = this.getPosition().getDistance(p); // - preySize;
			if (a.isLookingTasty(this)) {
				if (distance <= EATING_RANGE) {
					if (a.tryConsumeAgent()) {
						this.eat();
					}
				} else if (willFocusPreys && distance <= FOCUS_RANGE) {
					if (closestFocusPrey != null && a.isAlive()) {
						if (closestFocusPrey.getPosition().getDistance(
								this.getPosition()) > a.getPosition()
								.getDistance(this.getPosition())) {
							closestFocusPrey = a;
						}
					} else {
						closestFocusPrey = a;
					}
				} else if (closestFocusPrey == null) {
					/*
					 * Create a vector that points towards the prey.
					 */
					Vector newForce = new Vector(p, this.getPosition());

					/*
					 * Add this vector to the prey force, with proportion to how
					 * close the prey is. Closer preys will affect the force
					 * more than those far away.
					 */
					double norm = newForce.getNorm();
					preyForce.add(newForce.multiply(1 / (norm * distance)));
				}
			}
		}
		double norm = preyForce.getNorm();
		if (norm != 0) {
			preyForce.multiply(maxAcceleration / norm);
		}
		if (willFocusPreys && closestFocusPrey != null) {
			focusedPreyContainer.set(closestFocusPrey);
			if(AbstractObstacle.isInsidePathList(SurroundingsSettings.getObstacles(), position, focusedPreyContainer.get().getPosition())) {
				focusedPreyPath.setPath(
						Position.getShortestPath(position, focusedPreyContainer.get().getPosition(),
								SurroundingsSettings.getObstacles(), SurroundingsSettings.getWorldShape(),
								SurroundingsSettings.getGridDimension(), surroundings.getObstacleSafetyDistance())
						,initial_ttl);
				if(focusedPreyPath.isEmpty()) {
					return Vector.emptyVector();
				} else {
//					System.out.println("New focused prey needs path");
					focusedPreyPath.pop();
					return new Vector(focusedPreyPath.peek(), position);
				}
			} else {
				focusedPreyPath.clearPath();
				return new Vector(focusedPreyContainer.get().getPosition(), position);
			}
		}
		return preyForce;
	}



	/**
	 * @return returns The force the preys attracts the agent with
	 * @author Sebastian/Henrik
	 */
	private Vector getPreyForce(SurroundingsSettings surroundings, Container<IAgent> focusedPreyContainer) {
		if(focusedPreyContainer == null) {
			Log.v("NULL CONTAINER");
		}
		if (willFocusPreys && focusedPreyContainer.get() != null) {
			if(focusedPreyContainer.get().isAlive()) {
				Position p = focusedPreyContainer.get().getPosition();
				double distance = getPosition().getDistance(p);
				if (distance <= EATING_RANGE) {
					if (focusedPreyContainer.get().tryConsumeAgent()) {
						focusedPreyContainer.set(null);
						hungry = false;
						energy = MAX_ENERGY;
						digesting = DIGESTION_TIME;
					}
				} else {
					if(!focusedPreyPath.isEmpty()) {
						Position nextPathPosition = focusedPreyPath.peek();
						//If we are not near our current path target, move towards it.
						if(position.getDistance(nextPathPosition) > maxSpeed) {
							return new Vector(nextPathPosition, position);
						} else if(focusedPreyPath.size() > 1) {
							//Remove the next path, we are close to it, and go to next.
							focusedPreyPath.pop();
							return new Vector(focusedPreyPath.peek(), position);
						}
					} else {
						return new Vector(focusedPreyContainer.get().getPosition(), position);
					}
				}
			}
		}

		Vector preyForce = new Vector(0, 0);
		IAgent closestFocusPrey = null;
		int preySize = preyNeighbours.size();
		for (int i = 0; i < preySize; i++) {
			IAgent a = preyNeighbours.get(i);
			Position p = a.getPosition();
			double distance = getPosition().getDistance(p);
			if (distance <= visionRange) {
				if (distance <= EATING_RANGE) {
					if (a.tryConsumeAgent()) {
						hungry = false;
						energy = MAX_ENERGY;
						digesting = DIGESTION_TIME;
					}
				} else if (willFocusPreys && distance <= FOCUS_RANGE) {
					if (closestFocusPrey != null && a.isAlive()) {
						if (closestFocusPrey.getPosition().getDistance(
								this.position) > a.getPosition().getDistance(
								this.position)) {
							closestFocusPrey = a;
						}
					} else {
						closestFocusPrey = a;
					}
				} else if (closestFocusPrey == null) {
					/*
					 * Create a vector that points towards the prey.
					 */
					Vector newForce = new Vector(p, position);

					/*
					 * Add this vector to the prey force, with proportion to how
					 * close the prey is. Closer preys will affect the force
					 * more than those far away.
					 */
					double norm = newForce.getNorm();
					preyForce.add(newForce.multiply(1 / (norm * distance)));
				}
			}
		}

		double norm = preyForce.getNorm();
		if (norm != 0) {
			preyForce.multiply(maxAcceleration / norm);
		}

		if (willFocusPreys && closestFocusPrey != null) {
			focusedPreyContainer.set(closestFocusPrey);
			if(AbstractObstacle.isInsidePathList(SurroundingsSettings.getObstacles(), position, focusedPreyContainer.get().getPosition())) {
				focusedPreyPath.setPath(
						Position.getShortestPath(position, focusedPreyContainer.get().getPosition(),
								SurroundingsSettings.getObstacles(), SurroundingsSettings.getWorldShape(),
								SurroundingsSettings.getGridDimension(),surroundings.getObstacleSafetyDistance()));
				if(focusedPreyPath.isEmpty()) {
					return Vector.emptyVector();
				} else {
					return new Vector(focusedPreyPath.peek(), position);
				}
			} else {
				focusedPreyPath.clearPath();
				return new Vector(focusedPreyContainer.get().getPosition(), position);
			}

		}
		return preyForce;
	}

	/**
	 * "Predator Force" is defined as the sum of the vectors pointing away from
	 * all the predators in vision, weighted by the inverse of the distance to
	 * the predators, then normalized to have unit norm. Can be interpreted as
	 * the average sum of forces that the agent feels, weighted by how close the
	 * source of the force is.
	 *
	 * @author Sebbe
	 */
	private Vector getPredatorForce() {
		Vector predatorForce = new Vector(0, 0);
		if (isAStottingDeer && isStotting) {
			stottingDuration--;
			if (stottingDuration <= 0) {
				isStotting = false;
			}
			return stottingVector;
		} else {
			boolean predatorClose = false;
			int predSize = predNeighbours.size();
			IAgent predator;
			for (int i = 0; i < predSize; i++) {
				predator = predNeighbours.get(i);
				Position p = predator.getPosition();
				double distance = getPosition().getDistance(p);
				if (distance <= visionRange) { // If predator is in vision range
												// for prey

					/*
					 * Create a vector that points away from the predator.
					 */
					Vector newForce = new Vector(this.getPosition(), p);

					if (isAStottingDeer && distance <= STOTTING_RANGE) {
						predatorClose = true;
					}

					/*
					 * Add this vector to the predator force, with proportion to
					 * how close the predator is. Closer predators will affect
					 * the force more than those far away.
					 */
					double norm = newForce.getNorm();
					predatorForce.add(newForce.multiply(1 / (norm * distance)));
				}
			}

			double norm = predatorForce.getNorm();
			if (norm <= 0) { // No predators near --> Be unaffected
				alone = true;
			} else { // Else set the force depending on visible predators and
						// normalize it to maxAcceleration.
				predatorForce.multiply(maxAcceleration / norm);
				alone = false;
			}

			if (isAStottingDeer && stottingCoolDown <= 0 && predatorClose) {
				isStotting = true;
				stottingCoolDown = STOTTING_COOLDOWN;
				stottingDuration = STOTTING_LENGTH;
				double newX = 0;
				double newY = 0;
				if (Math.random() < 0.5) {
					newX = 1;
					newY = -predatorForce.getX() / predatorForce.getY();
				} else {
					newY = 1;
					newX = -predatorForce.getY() / predatorForce.getX();
				}
				stottingVector.setVector(newX, newY);
				stottingVector.multiply(predatorForce.getNorm()
						/ stottingVector.getNorm());
				stottingVector.add(predatorForce.multiply(-0.5));
				return stottingVector;
			}

		}
		return predatorForce;
	}

	/**
	 * This also decreases the deer's energy.
	 */
	@Override
	public void updatePosition() {
		super.updatePosition();
		this.energy--;
		stottingCoolDown--;
		if (energy == 0 || lifeLength > MAX_LIFE_LENGTH)
			isAlive = false;
	}

	@Override
	public void eat() {
		hungry = false;
		energy = MAX_ENERGY;
		digesting = DIGESTION_TIME;
	}
}
