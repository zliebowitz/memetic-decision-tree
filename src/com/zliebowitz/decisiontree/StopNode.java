package com.zliebowitz.decisiontree;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a tree node where the gp portion of the tree ends and local search begins
 */
public class StopNode extends GPNode {

	@Override
	public String toStringForHumans() {
		// TODO Auto-generated method stub
		return "S";
	}

	@Override
	public boolean nodeEquals(GPNode node) {
		return nodeEquivalentTo(node);
	}

	@Override
	public void eval(EvolutionState state, int thread, GPData input,
			ADFStack stack, GPIndividual individual, Problem problem) {
		// TODO Auto-generated method stub
		DecisionTreeData data = (DecisionTreeData) input;
		DecisionTreeProblem prob = (DecisionTreeProblem) problem;
		if (data.testSet.isEmpty())
			return;
		int classCount[] = new int[prob.classes];
		for (int x : data.trainingSet) {
			int c = (int) prob.values[prob.classAttribute][x];
			classCount[c]++;
		}
		if (data.depth > 0 && !data.trainingSet.isEmpty()) {
			int assumedClass = -1;
			boolean allSame = true;
			for (int i = 0; i < prob.classes; i++) {
				if (classCount[i] > 0) {
					if (assumedClass == -1)
						assumedClass = i;
					else {
						allSame = false;
						break;
					}
				}
			}
			
			if (!allSame) {
				List<Integer> toFilter = new ArrayList<Integer>();
				List<Integer> left = new ArrayList<Integer>();
				List<Integer> right = new ArrayList<Integer>();
				double minEntropy = Double.MAX_VALUE;
				int minEntropyAttribute = -1;
				double minEntropySplit = -1;
				for (int i = 0; i < prob.attributes; i++) {
					if (i == prob.classAttribute)
						continue;
					left.clear();
					right.addAll(data.trainingSet);
					int leftClasses[] = new int[prob.classes];
					int rightClasses[] = new int[prob.classes];
					System.arraycopy(classCount, 0, rightClasses, 0, prob.classes);
					
					for (double split : prob.getSortedAttributeSplits(i)) {
						toFilter.clear();
						toFilter.addAll(right);
						right.clear();
						for (int inst : toFilter) {
							if (prob.values[i][inst] < split) {
								left.add(inst);
								int c = (int) prob.values[prob.classAttribute][inst];
								leftClasses[c]++;
								rightClasses[c]--;
							} else {
								right.add(inst);
							}
						}
						if (left.size() == 0 || right.size() == 0)
							continue;
						double entropy = 0;
						int total = data.trainingSet.size();
						for (int side = 0; side < 2; side++) {
							int cCount[];
							int sum;
							if (side == 0) {
								cCount = leftClasses;
								sum = left.size();
							} else {
								cCount = rightClasses;
								sum = right.size();
							}

							for (int c = 0; c < prob.classes; c++) {
								//calculate the weighted entropy
								double percent = (double) cCount[c] / sum;
								entropy += -1 * (double) sum / total* percent * Math.log(percent);
							}
						}
						if (minEntropy > entropy) {
							minEntropy = entropy;
							minEntropyAttribute = i;
							minEntropySplit = split;
						}
					}
				}
				
				if (minEntropyAttribute != -1) {//recurse if there is a split
					left.clear();
					right.clear();
					for (int inst : data.trainingSet) {
						if (prob.values[minEntropyAttribute][inst] < minEntropySplit)
							left.add(inst);
						else
							right.add(inst);
					}
					List<Integer> leftTest = new ArrayList<Integer>();
					List<Integer> rightTest = new ArrayList<Integer>();
					for (int inst : data.testSet) {
						if (prob.values[minEntropyAttribute][inst] < minEntropySplit)
							leftTest.add(inst);
						else
							rightTest.add(inst);
					}
					data.depth--;
					data.trainingSet = left;
					data.testSet = leftTest;
					this.eval(state, thread, input, stack, individual, problem);
					data.trainingSet = right;
					data.testSet = rightTest;
					this.eval(state, thread, input, stack, individual, problem);
					data.depth++;
					return;
				}
			}
		} else { //no more splits due to constraint or purity		
			int classMax = -1;
			int count = -1;
			for (int i = 0; i < prob.classes; i++) {
				if (classCount[i] > classMax) {
					classMax = classCount[i];
					count = 1;
				} else if (classCount[i] == classMax)
					count++;
			}
			count = state.random[thread].nextInt(count);
			int guess = -1;
			for (int i = 0; i < prob.classes; i++) {
				if (classCount[i] == classMax) {
					if (count == 0) {
						guess = i;
						break;
					} else
						count--;
				}
			}
			for (int x : data.testSet) {
				if (prob.values[prob.classAttribute][x] == guess)
					data.matches++;
			}
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return toStringForHumans();
	}

}
