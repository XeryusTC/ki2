#!/usr/bin/python
import mdp

m1 = mdp.makeRNProblem()
m1.valueIteration()
m1.printValues()
m1.printActions()

m2 = mdp.make2DProblem()
m2.valueIteration()
m2.printValues()
m2.printActions()
