package com.zliebowitz.decisiontree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import ec.EvolutionState;
import ec.Statistics;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * Tests the best candidate result using seperate test data and the entire training data.
 */
public class DecisionTreeStat extends Statistics {
	GPIndividual bestSoFar;
	File testCSV;
	@Override
	public void finalStatistics(EvolutionState state, int result) {
		// TODO Auto-generated method stub
		super.finalStatistics(state, result);
		DecisionTreeProblem prob = (DecisionTreeProblem) state.evaluator.p_problem;
        Scanner sc;
        try {
			sc = new Scanner(testCSV);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			state.output.fatal("can't open test file");
			throw new RuntimeException();
		}
        sc.nextLine();
        ArrayList<double[]> tests = new ArrayList<double[]>();
        while(sc.hasNext())
        {
        	double inst[] = new double[prob.attributes];
        	String x[] = sc.nextLine().split(",");
        	for (int i = 0; i < prob.attributes; i++)
        	{
        		inst[i] = Double.parseDouble(x[i]);
        	}
        	tests.add(inst);
        }
        double oldvalues[][] = prob.values;
        double values[][] = new double[prob.attributes + 1][prob.instances + tests.size()];
        for (int i = 0; i < prob.attributes; i++)
        {
        	System.arraycopy(prob.values[i], 0, values[i], 0, prob.instances);
        	for (int j = 0; j < tests.size(); j++)
        		values[i][j + prob.instances] = tests.get(j)[i];
        }
        prob.values = values;
        DecisionTreeData data = new DecisionTreeData();
        data.depth = prob.depth;
        data.matches = 0;
        data.testSet = new ArrayList<Integer>();
        data.trainingSet = new ArrayList<Integer>();
        for (int i = 0; i < prob.instances; i++)
        	data.trainingSet.add(i);
        for (int i = 0; i < tests.size(); i++)
        	data.testSet.add(prob.instances + i);
        bestSoFar.trees[0].child.eval(state, 0, data, null, bestSoFar, state.evaluator.p_problem);
        state.output.println("% Correct: " + (double) data.matches * 100 / tests.size(), 0);
        prob.values = oldvalues;
    }

	@Override
	public void postEvaluationStatistics(EvolutionState state) {
		// TODO Auto-generated method stub
		super.postEvaluationStatistics(state);
		if (bestSoFar == null)
			bestSoFar = (GPIndividual) state.population.subpops[0].individuals[0];
		for (int i = 0; i < state.population.subpops[0].individuals.length; i++)
		{
			GPIndividual ind = (GPIndividual) (GPIndividual) state.population.subpops[0].individuals[i];
			if (ind.compareTo(bestSoFar) < 0)
				bestSoFar = ind;
		}
	}
	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
		testCSV = state.parameters.getFile(base.push("csv"), null);
		
	}

	

}
