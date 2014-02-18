package com.zliebowitz.decisiontree;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This problem generates Decision Trees using a memetic algorithm.
 *
 * All values are stored internally as doubles (and values as given must be numeric). Classes must be 0 - n consisting of whole numbers.
 *
 * Params:
 * base.csb = filename
 * base.csv.classAttribute = 0-based index of class (defaults to 0)
 * base.folds = number of folds to used internally for assessing fitness
 * base.depth = max depth of tree extended using j48
 *
 */
public class DecisionTreeProblem extends GPProblem {

    //number of attributes (including class)
	public int attributes;
    //number of instances
    public int instances;
    //names of attributes
	public String attributeNames[];
    //array of values accessed by values[attribute][instance]
	public double values[][];
    //number of folds used internally
	public	int folds;
    //number of classes
	public int classes;
    //depth of tree generated useing j48
	public int depth;
    //splitPoints[attribute] is an ordered list of points that can be divided on
    public double splitPoints[][];
    //0-indexed attribute of classes
    public int classAttribute = 0;
    //

	public double[] getSortedAttributeSplits(int attribute) {
        return splitPoints[attribute];
	}
	
	@Override
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;
		ind.evaluated = true;
		GPIndividual gpInd = (GPIndividual) ind;
		ArrayList<Integer> fold[] = new ArrayList[folds];
		int correct = 0;
		for (int i = 0; i < folds; i++)
			fold[i] = new ArrayList<Integer>();
		for (int i = 0; i < instances; i++)
			fold[state.random[threadnum].nextInt(folds)].add(i);
		for (int i = 0; i < folds; i++)
		{
			ArrayList<Integer> trainingSet = new ArrayList<Integer>();
			ArrayList<Integer> testSet = fold[i];
			for (int j = 0; j < folds; j++)
			{
				if (i == j)
					continue;
				trainingSet.addAll(fold[j]);
			}
			//Begin crossfold validation testing
				
				DecisionTreeData data = new DecisionTreeData();
				data.testSet = testSet;
				data.trainingSet = trainingSet;
				data.depth = depth;
				
				gpInd.trees[0].child.eval(state, threadnum, data, null, gpInd, this);
				
				
				correct += data.matches;
			
		}
		KozaFitness fit = (KozaFitness) gpInd.fitness;
		fit.setStandardizedFitness(state, 1f * (instances - correct) / instances + (float) Math.pow(Math.log(Math.pow(2,depth) * gpInd.size())/Math.log(100000), 8) );
	}
	
	private void processFile(File csvFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		attributeNames = br.readLine().split(",");
		attributes = attributeNames.length;
		
		//import data
		ArrayList<double[]> list = new ArrayList<double[]>();
		while (true)
		{
			String in = br.readLine();
			if (in == null)
				break;
			double instance[] = new double[attributes];
			String val[] = in.split(",");
			for (int i = 0; i < attributes; i++)
				instance[i] = Double.parseDouble(val[i]);
			list.add(instance);
		}
		
		//switch to attribute-major representation for faster data access
		instances = list.size();
		values = new double[attributes][instances];
		for (int i = 0; i < instances; i++)
		{
			double instance[] = list.get(i);
			for (int j = 0; j < attributes; j++)
				values[j][i] = instance[j];
		}

        //generates split points
        splitPoints = new double[attributes][];
        Integer instance[] = new Integer[instances];
        for (int i = 0;  i < instances; i++)
            instance[i] = i;
        for (int a = 0; a < attributes; a++) {
            if (a == classAttribute)
                continue;
            List<Double> points= new ArrayList<Double>();
            final int at = a;
            Arrays.sort(instance, new Comparator<Integer>() {

                @Override
                public int compare(Integer i, Integer i2) {
                    return (int) Math.signum(values[at][i] - values[at][i2]);
                }
            });
            for (int i = 0; i < instances -1; i++) {
            	if (points.size() > 0 && points.get(points.size() -1) == values[at][instance[i]])
            		continue;
                if (values[classAttribute][instance[i]] != values[classAttribute][instance[i+1]])
                    points.add( (values[at][instance[i]] + values[at][instance[i+1]]) / 2);
            }
            splitPoints[at] = new double[points.size()];
            for (int i = 0; i < points.size(); i++)
                splitPoints[at][i] = points.get(i);
        }

        //coutn classes
        Set<Double> possibleClasses = new HashSet<Double>();
        for (int i = 0; i < instances; i++) {
            Double v = values[classAttribute][i];
            if (!possibleClasses.contains(v))
                possibleClasses.add(v);
        }
        classes = possibleClasses.size();
    }
	
	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		
		folds = state.parameters.getIntWithDefault(base.push("folds"), null, 5);
		depth = state.parameters.getIntWithDefault(base.push("depth"), null, 0);
        classAttribute = state.parameters.getIntWithDefault(base.push("csv").push("classAttribute"), null, 0);
		try
		{
		File csvFile = state.parameters.getFile(base.push("csv"), null);
		processFile(csvFile);
		} catch (IOException e) {
			state.output.error("Error reading input csv file");
		}
	}	

}
