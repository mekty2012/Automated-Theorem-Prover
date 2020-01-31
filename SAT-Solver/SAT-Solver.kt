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
    val failed : List<Clause> = f.filter{!evalClause(current, it)}
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

typealias PartState = MutableMap<Int, Pair<Boolean, Int>>

fun dpllSAT(f:CNF,
            variableChooser:(CNF, PartState) -> Int): State? {
  val curr = mutableMapOf<Int, Pair<Boolean, Int>>()
  val evalIndexList = mutableListOf<Int>()
  TODO()
}

fun partialEvalClause(c:Clause, ps:PartState) : Clause? {
  if (c.any {ps.containsKey(it.first) && (ps[it.first]?.first == it.second)}) return null
  return c.filter {!ps.containsKey(it.first)}
}

// Perform single unit propagation.
fun unitPropagation(f:CNF, ps:PartState, evalList:List<Int>):PartState {
  var result = mutableMapOf<Int, Pair<Boolean, Int>>()
  for ((i, c) in f.withIndex()) {
    val res = partialEvalClause(c, ps)
    TODO()
  }
  return result
}