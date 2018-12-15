package algorithms.mdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import learning.*;
import problems.maze.MazeProblemMDP;
import problems.maze.MazeState;

/** 
 * Implements the value iteration algorithm for Markov Decision Processes 
 */
public class ValueIteration extends LearningAlgorithm {
	
	/** Stores the utilities for each state */;
	private HashMap<State, Double> utilities;
	
	/** Max delta. Controls convergence.*/
	private double maxDelta = 0.01;
	
	/** 
	 * Learns the policy (notice that this method is protected, and called from the 
	 * public method learnPolicy(LearningProblem problem, double gamma) in LearningAlgorithm.
	 */
	@Override
	protected void learnPolicy() {
		// This algorithm only works for MDPs
		if (!(problem instanceof MDPLearningProblem)){
			System.out.println("The algorithm ValueIteration can not be applied to this problem (model is not visible).");
			System.exit(0);
		}
		//Variables
		HashMap<State, Double> utilitiesPrime = new HashMap<>();
		utilities = new HashMap<>();
		MazeProblemMDP mProblem = (MazeProblemMDP)problem;
		HashSet<State> S = new HashSet<>();
		double delta=0;
		//Initializes utilities
		S.addAll(mProblem.getAllStates());
		for(State s : S) {
			utilities.put(s, mProblem.getReward(s));
			utilitiesPrime.put(s, mProblem.getReward(s));
		}
		//Algorithm
		do{
			for (State s : S) {
				if (!mProblem.isFinal(s)) {
					delta = 0;
					//Updates with best action
					for (Action a : mProblem.getPossibleActions(s)) {
						double utilityAux = mProblem.getExpectedUtility(s, a, utilities, mProblem.gamma);
						if (utilityAux > utilitiesPrime.get(s)) {
							utilitiesPrime.replace(s, utilityAux);
						}
					}
					//updates delta and the utilities
					if (Math.abs(utilitiesPrime.get(s) - utilities.get(s)) > delta)
						delta = Math.abs(utilitiesPrime.get(s) - utilities.get(s));
					utilities=(HashMap<State, Double>)utilitiesPrime.clone();
				}
				else
					utilitiesPrime.replace(s, mProblem.getReward(s));
			}
		}while(delta>=maxDelta);
		//Añadimos policy
		for(State s : S) {
			Action actionForPolicy = null;
			double max=Double.NEGATIVE_INFINITY;
			for (Action a : mProblem.getPossibleActions(s)) {
				if (mProblem.getExpectedUtility(s, a, utilities, mProblem.gamma) > max) {
					max = mProblem.getExpectedUtility(s, a, utilities, mProblem.gamma);
					actionForPolicy = a;
				}
			}
			this.solution.setAction(s, actionForPolicy);
		}
	}
	
	
	/** 
	 * Sets the parameters of the algorithm. 
	 */
	@Override
	public void setParams(String[] args) {
		// In this case, there is only one parameter (maxDelta).
		if (args.length>0){
			try{
				maxDelta = Double.parseDouble(args[0]);
			} 
			catch(Exception e){
				System.out.println("The value for maxDelta is not correct. Using 0.01.");
			}	
		}
	}
	
	/** Prints the results */
	public void printResults(){
		// Prints the utilities.
		System.out.println("Value Iteration\n");
		System.out.println("Utilities");
		for (Entry<State,Double> entry: utilities.entrySet()){
			State state = entry.getKey();
			double utility = entry.getValue();
			System.out.println("\t"+state +"  ---> "+utility);
		}
		// Prints the policy
		System.out.println("\nOptimal policy");
		System.out.println(solution);
	}
	
	
	/** Main function. Allows testing the algorithm with MDPExProblem */
	public static void main(String[] args){
		LearningProblem mdp = new problems.mdpexample2.MDPExProblem();
		mdp.setParams(null);
		ValueIteration vi = new ValueIteration();
		vi.setProblem(mdp);
		vi.learnPolicy(mdp);
		vi.printResults();
	
	}

}
