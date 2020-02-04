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

For specific algorithm, you can see [here](https://github.com/mekty2012/sumunyeon/blob/master/19KPUmathseminar.pdf).