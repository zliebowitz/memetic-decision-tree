Generation of Decision Trees using Memetic Algorithms
=====================================================

This is a prototype for a new approach to decision tree induction. The general idea is to combine the usage of genetic programming techniques and more traditional local search techniques to get a faster and smaller tree (smaller trees typically having a smaller generalization error).


How to Run
----------
This project depends on [ECJ](http://cs.gmu.edu/~eclab/projects/ecj/) 21. The simplest way to setup this project is to import ECJ and this into eclipse as two seperate projects and having this project have the ECJ project in it;s build path. Instructions on setting up ECJ with eclipse are linked on the ecj homepage or otherwise googleable. Note that none of the components that require files beyond the base download are used and you may ignore those (ie. GUI and PUSH).

The parameter file assumes that the working directory has a train.csv and test.csv file which are the testing and training files respectively. these (as well as most other things) can be changed in the [base parameter file](params/decisiontree-base.params).

If you are running from the repository root, it should be run as follows
```
java -classpath SET_IT_HERE -Xmx2g ec.Evolve -file params/decisiontree-memetic.params
```

Note the -Xmx which changes the maximum heap size which is usually necessary. Additionally, feel free to try the pther parameter files or make your own.
