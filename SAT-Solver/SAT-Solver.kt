import java.io.File
import kotlin.math.abs

sealed class PropFormula
data class Var(val n:Int) : PropFormula()
data class Neg(val f:PropFormula) : PropFormula()
data class And(val lf:List<PropFormula>) : PropFormula()
data class Or(val lf:List<PropFormula>) : PropFormula()

typealias Clause = List<Pair<Int, Boolean>>
typealias CNF = List<Clause>
typealias State = Map<Int, Boolean>

// Given total state and formula, evaluates. This takes Poly Time.
fun eval(st:State, f:PropFormula) : Boolean = when (f) {
  is Var -> st.getOrElse(f.n) {throw java.lang.IllegalArgumentException("Variable ${f.n} uninitialized.")}
  is Neg -> !eval(st, f.f)
  is And -> f.lf.all {eval(st, it)}
  is Or -> f.lf.any {eval(st, it)}
}

// Given total state and clause, evaluates.
fun evalClause(st:State, f:Clause) : Boolean =
  f.any { st.getOrElse(it.first, {throw java.lang.IllegalArgumentException("Variable ${it.first} uninitialized.")}) == it.second }

// Given total state and CNF-formula, evaluates.
fun eval(st:State, f:CNF) : Boolean =
  f.all {
    it.any {
      st.getOrElse(it.first, {throw java.lang.IllegalArgumentException("Variable ${it.first} uninitialized.")}) == it.second
    }
  }

// This function tests whether function is CNF or not.
fun isCNF(f:PropFormula) : Boolean = when (f) {
  is And ->
    f.lf.all {
      when (it) {
        is Or -> {
          it.lf.all {
            when (it) {
              is Var -> true
              is Neg -> it.f is Var
              else -> false
            } 
          }
        }
        else -> {
          false
        }
      }
    }
  else -> {
    false
  }
}

// By applying de morgan's law push negation into innermost case.
fun pushNeg(f:PropFormula, curr:Boolean): PropFormula {
  return if (curr) {
    when (f) {
      is Var -> Neg(f)
      is Neg -> pushNeg(f.f, false)
      is And -> Or(f.lf.map{pushNeg(it, true)})
      is Or -> And(f.lf.map{pushNeg(it, true)})
    }
  }
  else {
    when (f) {
      is Var -> f
      is Neg -> pushNeg(f.f, true)
      is And -> And(f.lf.map{pushNeg(it, false)})
      is Or -> Or(f.lf.map{pushNeg(it, false)})
    }
  }
}

// TODO : Arbitrary formula to equisatisfiable CNF

// Read "*.cnf" form file, returns CNF form and number of variable.
fun deserialize(fileName:String) : Pair<CNF, Int> {
  val file = File(fileName).readLines()
  var formula = mutableListOf(listOf<Pair<Int, Boolean>>())
  var info : List<String>? = null
  var count = 0
  for (text in file) {
    count++
    if (text.startsWith("c")) {
      continue
    }
    if (info == null && text.startsWith("p")) {
      info = text.split(Regex("""\s+"""))
      break
    }
  }
  if (info == null) {
    throw java.lang.IllegalArgumentException("File $fileName doesn't contains any cnf form")
  }
  println(info.joinToString())
  val nvar = info[2].toInt()
  val nclause = info[3].toInt()
  for (j in count until (count + nclause)) {
    val text = file[j].trim{it == ' '}
    val clause = text.split(Regex("""\s+"""))
    formula.add(clause.map{
      abs(it.toInt()) to (it.toInt() > 0)
    })
  }
  return formula to nvar
}

// Preprocess CNF form, removing same variable in single cluase.
fun removeDuplicate(f:CNF):CNF = f.map{it.toHashSet().toList()}

// Helper function for removeTrue. Check if whether this clause contains p and ~p.
fun isRemovable(c:Clause):Boolean {
  val map = mutableMapOf<Int, Boolean>()
  for (pair in c) {
    if (map.getOrPut(pair.first, {pair.second}) != pair.second) {
      return true
    }
  }
  return false
}

// Remove trivial clauses, containing p and ~p.
fun removeTrue(f:CNF):CNF = f.filter { isRemovable(it) }

// Find variables that appears only positive or only negative, and return state that satisfies all of them.
fun findTrivial(f:CNF) : State {
  val map = mutableMapOf<Int, Pair<Boolean, Boolean>>()
  for (clause in f) {
    for (pair in clause) {
      if (pair.second) {
        map[pair.first] = true to map.getOrDefault(pair.first, false to false).second
      }
      else {
        map[pair.first] = map.getOrDefault(pair.first, false to false).first to true
      }
    }
  }
  return map.filter({!(it.value.first && it.value.second)}).mapValues({it.value.first})
}

// Check whether the formula is Horm formula or not.
fun isHorn(f:CNF): Boolean = f.all { it.count {it.second} <= 1 }

// Warning : This function requires input to be Horn formula. For non-Horn formula, it may not terminate.
// If input is horn formula, it returns partial mapping or null. Partial mapping is minimal solution, and null means result is UNSAT.
fun solveHorn(f:CNF, nvar:Int) : State? {
  val state = mutableMapOf<Int, Boolean>()
  for (i in 1..nvar) {state[i] = false}
  global@while (true) {
    for (clause in f) {
      if (evalClause(state, clause)) {
        continue
      }
      else {
        val res = clause.indexOfFirst{it.second}
        if (res == -1) {
          return null
        }
        else {
          state[res] = true
          continue@global
        }
      }
    }
    break
  }
  return state
}

// Checkes if formula is 2-CNF.
fun is2CNF(f:CNF) : Boolean = f.all { it.size <= 2 }

val pairToIndex = {n:Int, b:Boolean -> 2 * (n - 1) + (if (b) 0 else 1) }

fun solve2CNF(f:CNF, nvar:Int) : State? {
  val adjMatrix = List(nvar, {List(nvar, {false}).toMutableList()})
  for (clause in f) {
    if (clause.size == 2) {
      val (first, second) = clause
      adjMatrix[pairToIndex(first.first, !first.second)][pairToIndex(second.first, second.second)] = true
      adjMatrix[pairToIndex(second.first, !second.second)][pairToIndex(first.first, first.second)] = true
    }
    else if (clause.size == 1) {
      // In this case, we view clause p as p \/ p.
      val first = clause[0]
      adjMatrix[pairToIndex(first.first, !first.second)][pairToIndex(first.first, first.second)] = true
    }
  }
  // To create transitive closure of directed graph, we use floyd warshall algorithm.
  for (k in 0 until adjMatrix.size) {
    for (i in 0 until adjMatrix.size) {
      for (j in 0 until adjMatrix.size) {
        adjMatrix[i][j] = adjMatrix[i][k] && adjMatrix[k][j]
      }
    }
  }
  val result = mutableMapOf<Int, Boolean>()
  for (v in 1..nvar) {
    val vTonv = adjMatrix[pairToIndex(v, true)][pairToIndex(v, false)]
    val nvTov = adjMatrix[pairToIndex(v, false)][pairToIndex(v, true)]
    if (vTonv && nvTov) {
      return null
    }
    else if (vTonv) {
      result[v] = false
    }
    else if (nvTov) {
      result[v] = true
    }
  }
  return result
}

/*
 * Resolution is one of proof method in Propositional Logic.
 * For two clause (C_1 \/ r) and (C_2 \/ ~r), we have
 * (C_1 \/ r) /\ (C_2 \/ ~r) => (C_1 \/ C_2)
 */
fun resolution(first:Clause, second:Clause, p:Int) : Clause {
  if (first.contains(p to true) && second.contains(p to false)) {
    return ((first - (p to true)) + (second - (p to false))).toSet().toList()
  } else {
    throw IllegalArgumentException("Resolution requires first clause to contains p, second clause to contains ~p")
  }
}

/*
 *  Walk-SAT is randomized algorithm solving SAT problem.
 *  First, we initialize mapping, with every variable to false.
 *  We then execute following loop for given step.
 *  In each step of loop, first we check whether formula is satisfied or
 *  there exists some clauses not satisfied under current state.
 *  If already satisfied, return current state.
 *  If not, randomly choose one clause, and again randomly choose one
 *  variable in that clause, and flip the sign of that variable.
 *
 *  If result is null, Walk-SAT couldn't find solution, which doesn't
 *  mean that this formula is unsatisfiable.
 */
fun walkSAT(f:CNF, step:Int, nvar:Int, random:java.util.Random): State? {
  var current = mutableMapOf<Int, Boolean>()
  for (i in 1..nvar) {current[i] = false}
  glob@for (currStep in 1..step) {
    val failed = f.filter{!evalClause(current, it)}
    if (failed.size == 0) {
      return current
    }
    else {
      val clauseIndex = random.nextInt(failed.size)
      val chosenClause = failed[clauseIndex]
      val varIndex = random.nextInt(chosenClause.size)
      val chosenVar = chosenClause[varIndex].first
      current[chosenVar] = !current[chosenVar]!!
    }
  }
  return null
}

/*
 * DPLL algorithm - https://en.wikipedia.org/wiki/DPLL_algorithm
 * For flexivity, variable chooser is given as input.
 *
 * First, we initialize empty partial state mapping.
 * Then until no more propagation is performed, do unit propagation and pure literal elimination.
 * If there is no more propagation, choose new branching variable with given variableChooser.
 * Then recursively call itself with chosen one to true/false mapped.
 */
fun dpllSAT(f:CNF,
            s:State,
            variableChooser:(CNF, State) -> Pair<Int, Boolean>): State? {
  val curr = s.toMutableMap()
  while (true) {
    var noPropagation = true
    val unitRes = unitPropagation(f, curr)
    if (unitRes.size != 0) {
      noPropagation = false
      curr.putAll(unitRes)
    }
    val pureRes = pureLiteralElimination(f, curr)
    if (pureRes.size != 0) {
      noPropagation = false
      curr.putAll(pureRes)
    }
    if (noPropagation) {
      break
    }
  }
  val evaled = f.map{partialEval(it, curr)}
  if (evaled.all{it == null}) return curr
  if (evaled.all{it?.isEmpty() ?: false}) return null
  val chosen = variableChooser(f, curr)
  val first = dpllSAT(f, curr + (chosen.first to chosen.second), variableChooser)
  if (first == null) {
    return dpllSAT(f, curr + (chosen.first to chosen.second), variableChooser)
  }
  else {return first}
}

/*
 * This function partially evaluate clause.
 * If clause is satisfied, -there exists true literal- return null.
 * If cluase is unsatisfied, it eliminates false evaluated literals, and only returns non evaluated literals.
 * Note that when clause is conflict -every literal is false-, it returns empty clause.
 */
fun partialEval(c:Clause, ps:State) : Clause? {
  if (c.any {ps.containsKey(it.first) && (ps[it.first]!! == it.second)}) return null
  return c.filter {!ps.containsKey(it.first)}
}

/*
 * If some clause is {p} or {~p}, the only way to satisfy total formula is
 * assigning p to true or false, resp.
 * So this function partially evaluate each clause, then if the result is
 * unit clause -{p} or {~p}-, it assign p to true or false.
 * Note that this function only performs single unit propagation, so after
 * this function there still could exists unit clause.
 *
 * This function returns propagated variables, not merged state.
 */
fun unitPropagation(f:CNF, ps:State):State {
  var result = mutableMapOf<Int, Boolean>()
  for (c in f) {
    val res = partialEval(c, ps)
    if (res?.size == 1) {
      val propagated = res[0]
      result[propagated.first] = propagated.second
    }
  }
  return result
}

/*
 * If some variable only appears true or false, it is okay to assume that
 * variable is true or false, resp.
 * So this function founds such pure literals, and return those as partstate.
 */
fun pureLiteralElimination(f:CNF, ps:State):State {
  var checker = mutableMapOf<Int, Boolean?>()
  for (c in f) {
    val res = partialEval(c, ps)
    if (res != null) {
      for (l in res) {
        if (checker.getOrPut(l.first, {l.second}) != l.second) {
          checker[l.first] = null
        }
      }
    }
  }
  return checker.filterValues{it != null}.mapValues{it.value!!}
}