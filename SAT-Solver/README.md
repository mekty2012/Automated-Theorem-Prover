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

Since Horn formula can written as (p /\ q /\ r) -> s, where s is only positive literal 
we can force the partial state to fulfill formula. If it does not contain positive literal, 
we can view it as (p /\ q /\ r) -> False. 

A CNF Formula is 2-CNF if each clauses contain at most two literals.

First create implication graph, where each node are literals and there exists edge iff ~ L1 \/ L2 or 
L1 \/ ~L2 exists. Then 2-CNF's satisfiability is equivalent to consistency of implication graph, where
consistency of implication graph means that there is no cycle that p -> ... -> ~p -> ... -> p happens.

A Formula is X-SAT if it is conjunction of clauses, where each clause are composed with XOR. 

X-SAT is solvable by changing SAT to system of linear equation problem, over Z_2.
## SAT-solver

### Walk-SAT
Walk SAT is randomized algorithm that solves SAT problem. The existence of Walk SAT implies that NP is subset of PP.

TODO : Add algorithm of Walk-SAT

### DPLL algorithm
DPLL algorithm solves SAT problem, but highly tractable. It doesn't break P =!= NP, however it shows good results in many cases. 

TODO : Add algorithm of DPLL