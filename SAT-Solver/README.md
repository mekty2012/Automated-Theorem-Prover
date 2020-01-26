# SAT-Solver

This directory implements SAT-solver. 

## SAT Problem

The SATisfiability Problem is problem that determines whether given propositional logic is satisfiable, or unsatisfiable.

It is known that SAT problem is NP-complete. (Cook-Levin Theorem)

## CNF Form

In implementation, the usual format for input formula is CNF(Conjunctive Normal Form). 

For propositional variable p, p or ~p is a literal. 

1. A formula is CNF(Conjunctive Normal Form) if it is conjunction of disjunctions of literals.
2. A formula is DNF(Disjunctive Normal Form) if it is disjunction of conjunctions of literals.

Moreover, it is known that 3-SAT (Satisfiability problem over CNF form where each clauses contains at most 3 literals) is NP-Complete.

Specifically, for every formula, there exists equisatisfiable 3-SAT formula that can be computed in polynomial time.

For examples of CNF problem sets, refer [here](https://www.cs.ubc.ca/~hoos/SATLIB/benchm.html).

## Polynomial Time solvable SAT
These are list of polynomial time solvable SAT problems.

A CNF Formula is Horn formula if each clauses contain at most one positive literals.

TODO : Add algorithm for Horn Formula Solving

A CNF Formula is 2-CNF if each clauses contain at most two literals.

TODO : Add algorithm for 2-CNF solving.

## SAT-solver

### Walk-SAT
Walk SAT is randomized algorithm that solves SAT problem. The existence of Walk SAT implies that NP is subset of PP.

TODO : Add algorithm of Walk-SAT

### DPLL algorithm
DPLL algorithm solves SAT problem, but highly practical. It doesn't break P =!= NP, however it shows good results in many cases. 

TODO : Add algorithm of DPLL