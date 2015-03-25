#!/usr/bin/python
import mdp
import time

gamma = 0.8

m1 = mdp.makeRNProblem()
m1.gamma = gamma
startm1 = time.time()
m1.valueIteration()
endm1 = time.time()
m1.printValues()
m1.printActions()
print "Iterations:  ", m1.iterations
print "Time needed: ", endm1 - startm1
print

quit()

m2 = mdp.makeRNProblem()
m2.gamma = gamma
startm2 = time.time()
m2.policyIteration()
endm2 = time.time()
m2.printValues()
m2.printActions()
print "Iterations:  ", m2.iterations
print "Time needed: ", endm2 - startm2
print

