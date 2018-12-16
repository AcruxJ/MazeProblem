package algorithms.mdp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import learning.*;
import problems.maze.MazeProblemMDP;

public class PolicyIteration extends LearningAlgorithm {
	
	/** Max delta. Controls convergence.*/
	private double maxDelta = 0.01;
	
	/** 
	 * Learns the policy (notice that this method is protected, and called from the 
	 * public method learnPolicy(LearningProblem problem, double gamma) in LearningAlgorithm.
	 */	
	@Override
	protected void learnPolicy() {
		if (!(problem instanceof MDPLearningProblem)){
			System.out.println("The algorithm PolicyIteration can not be applied to this problem (model is not visible).");
			System.exit(0);
		}
		//Variables
		HashMap<State,Double> utilities = new HashMap<State,Double>();
		HashSet<State> S = new HashSet<>();
		MazeProblemMDP mProblem = (MazeProblemMDP) problem;
		Policy policy = new Policy();
		//Initialize policy
		S.addAll(mProblem.getAllStates());
		ArrayList<Action> actions = new ArrayList<>();
		for(State s : S) {
			actions = mProblem.getPossibleActions(s);
			if(!actions.isEmpty())
				policy.setAction(s, actions.get(0));
		}
		//Until we obtain the same policy repeat
		do{
			solution = policy;
			utilities = policyEvaluation(solution);
			policy = policyImprovement(utilities);
		}while(!solution.equals(policy));
	}
		
	
	/*
	 * Policy evaluation. Calculates the utility given the policy 
	 */
	private HashMap<State,Double> policyEvaluation(Policy policy){
		
		// Initializes utilities. In case of terminal states, the utility corresponds to
		// the reward. In the remaining (most) states, utilities are zero.		
		//Variables
		HashMap<State,Double> utilities = new HashMap<State,Double>();
		MazeProblemMDP mProblem = (MazeProblemMDP) problem;
		Collection<State> S = mProblem.getAllStates();
		double delta=0;
		
		//Initialization
		for(State s : S) {
			utilities.put(s, problem.getReward(s));
		}
		//Algorithm
		do{
			HashMap<State, Double> utilitiesPrime = new HashMap<>();
			delta = 0;
			for (State s : S) {
				if (!mProblem.isFinal(s)) {
					// Updates with policy's action
					utilitiesPrime.put(s,
							mProblem.getReward(s)+mProblem.gamma*mProblem.getExpectedUtility(s, policy.getAction(s), utilities, mProblem.gamma));
					// updates delta and the utilities
					if (Math.abs(utilitiesPrime.get(s) - utilities.get(s)) > delta)
						delta = Math.abs(utilitiesPrime.get(s) - utilities.get(s));
				} else {
					utilitiesPrime.put(s, mProblem.getReward(s));
				}
			}
		
			utilities=utilitiesPrime;
		}while(delta>=maxDelta);
		return utilities;
	}

	/*
	 * Improves the policy given the utility 
	 */
	private Policy policyImprovement(HashMap<State,Double> utilities){
		// Creates the new policy
		Policy newPolicy = new Policy();
		MDPLearningProblem MDPproblem = (MDPLearningProblem) this.problem;
		Collection<State> allStates = MDPproblem.getAllStates();
		
		for(State s : allStates) {
			if(!MDPproblem.isFinal(s)) {
				//Find the best action for the state
				
				Action action = null;
				double expectedUtility = Double.NEGATIVE_INFINITY;
				
				for(Action a : MDPproblem.getPossibleActions(s)) {
					if(MDPproblem.getExpectedUtility(s, a, utilities, MDPproblem.gamma) > expectedUtility) {
						expectedUtility = MDPproblem.getExpectedUtility(s, a, utilities, MDPproblem.gamma);
						action = a;
					}
				}
				
				newPolicy.setAction(s, action);
				
			}
		}
			
		return newPolicy;
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
		System.out.println("Policy Iteration");
		// Prints the policy
		System.out.println("\nOptimal policy");
		System.out.println(solution);
	}	
	
	/** Main function. Allows testing the algorithm with MDPExProblem */
	public static void main(String[] args){
		LearningProblem mdp = new problems.mdpexample2.MDPExProblem();
		mdp.setParams(null);
		PolicyIteration pi = new PolicyIteration();
		pi.setProblem(mdp);
		pi.learnPolicy(mdp);
		pi.printResults();
	}	
	
}
