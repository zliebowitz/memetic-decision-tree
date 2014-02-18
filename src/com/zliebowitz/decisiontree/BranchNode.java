package com.zliebowitz.decisiontree;


import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import ec.util.Code;
import ec.util.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a tree node where the GP portion splits
 */
public class BranchNode extends ERC {
	
	int attribute;
	double divPoint;

	EvolutionState state;
	DecisionTreeProblem problem;
	
	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
		this.state = state;
	}

	@Override
	public int expectedChildren() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String toStringForHumans() {
		return problem.attributeNames[attribute] + " " + divPoint;
	}

	@Override
	public void resetNode(EvolutionState state, int thread) {
		// TODO Auto-generated method stub
		if (problem == null)
			problem = (DecisionTreeProblem) state.evaluator.p_problem;
		attribute =state.random[thread].nextInt(problem.attributes);
        if (attribute == problem.classAttribute)
        {
            resetNode(state, thread);
            return;
        }
		double options[] = problem.getSortedAttributeSplits(attribute);
        if (options.length == 0)
        {
            resetNode(state, thread);
            return;
        }
		int choice = state.random[thread].nextInt(options.length);
		divPoint =  options[choice];
	}

	@Override
	public void mutateERC(EvolutionState state, int thread) {
		throw new RuntimeException("Point Mutation not implemented via ERC");
	}

	public List<Integer>[] split(List<Integer> list) {
		ArrayList<Integer> ret[] = new ArrayList[2];
		ret[0] = new ArrayList<Integer>();
		ret[1] = new ArrayList<Integer>();
		for (Integer x: list)
			if (problem.values[attribute][x] < divPoint)
				ret[0].add(x);
			else
				ret[1].add(x);
		return ret;
	}
	
	@Override
	public void eval(EvolutionState state, int thread, GPData input,
			ADFStack stack, GPIndividual individual, Problem problem) {
		// TODO Auto-generated method stub
		DecisionTreeData data = (DecisionTreeData) input;
		List<Integer> test[] = split(data.testSet);
		List<Integer> train[] = split(data.trainingSet);
		
		for (int i = 0; i < 2; i++) {
			data.testSet = test[i];
			data.trainingSet = train[i];
			children[i].eval(state, thread, input, stack, individual, problem);
		}
		
	}

	@Override
	public boolean nodeEquals(GPNode node) {
		// TODO Auto-generated method stub
		if (node instanceof BranchNode)
		{
			BranchNode other = (BranchNode) node;
			return attribute == other.attribute && divPoint == other.divPoint;
		} else
		return false;
	}

	@Override
	public String encode() {
		// TODO Auto-generated method stub
		return Code.encode(attribute) + Code.encode(divPoint);
	}


}
