# Presburger Arithmetic Solver

## Presburger Arithmetic

Presburger Arithmetic is theory of natural numbers with addition and order.

- Constants Symbols
  : Every Integers
- Relation Symbols
  : Equality testing, = ⊂ N×N 
- Function Symbols
  : + : N×N⇒N

Here, the sentences we treat are first order logic over these.

## First Order Logic
First Order Logic is theory that allows quantifiers over single variable. 

Here, we allow universal quantifier -forall- and existential quantifier -exists-. 
Interpretation of both are as the name says.

## Presburger Arithmetic Solver
First, we assume that every input sentence is closed -it doesn't contain any free variable-. 

The solver should output whether sentence is true or false, however can not give evidence of it, since
proof of universal quantifier is not simple. 

If possible, actually, the evidence of universal quantifier is usually given as function, as evidence of forall x, P x is function mapping x to evidence of P x.
However, I will treat this problem later.

For specific algorithm, you can see [here](https://github.com/mekty2012/sumunyeon/blob/master/SeminarProof.pdf).

## Evidence Finding

First, we define 'evidence' for base case.

- a = b : the value a = b is evidence.
- a | b : the value c that b = a * c is evidence.
- C1 /\ C2 : pair of evidence for C1 and C2 is evidence.
- C1 \\/ C2 : If C1 is true with evidence E1, (1, E1) is evidence. Otherwise, (2, E2) is evidence.
- ~C : We expect our formula to have all its negation in base case. So, we can think as Not case as
  ~(a=b), ~(a|b). 
  + ~ (a=b) : the value a - b is evidence.
  + ~ (a|b) : the value b % a is evidence.
- Forall x. C : the function mapping x to evidence of C is evidence.
- Exists x. C : the pair of value of x and evidence of C x is evidence.

Problems : 
The algorithm presented above cannot solve forall statement, therefore transform Forall x. C into ~(Exists x. ~C).
Maybe it could be possible to create counter example class.

Counterexample

- ~ (a=b) : the value a - b is counterexample.
- ~ (a|b) : the value b % a is counterexample.
- ~ (C1 /\ C2) : If C1 is false with counterexample E1, (1, E1) is counterexample. Otherwise, (2, E2) is counterexample.
- ~ (C1 \\/ C2) : pair of counterexample for C1 and C2 is counterexample.
- ~ (~ C) : evidence of C is counterexample.
- ~ (Forall x.C) : pair of value of x and counterexample of C x is counterexample.

Merging evidence and counter example

Finding evidence for formula F
- (a = b) : the value a = b
- (a | b) : the value c s.t. b = a * c
- (C1 /\ C2) : the pair of evidence C1 and C2, (E1, E2)
- (C1 \\/ C2) : pair of index and evidence (i, Ei)
- (~ C) : counterexample of C
- Exists x. C : pair of x and evidence of C x

Finding counterexample for formula F
- (a = b) : the value b - a
- (a | b) : the value b % a
- (C1 /\ C2) : pair of index and counterexample
- (C1 \\/ C2) : pair of counterexamples
- (~ C) : evidence of C
- (Exists x. C) : function from x to counterexample of C x.