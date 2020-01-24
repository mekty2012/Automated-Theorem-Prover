import java.io.File
import kotlin.math.abs

sealed class PropFormula
data class Var(val n:Int) : PropFormula()
data class Neg(val f:PropFormula) : PropFormula()
data class And(val lf:List<PropFormula>) : PropFormula()
data class Or(val lf:List<PropFormula>) : PropFormula()

typealias CNF = List<List<Pair<Int, Boolean>>>

fun eval(st:Map<Int, Boolean>, f:PropFormula) : Boolean = when (f) {
  is Var -> st.getOrElse(f.n) {throw java.lang.IllegalArgumentException("Variable ${f.n} uninitialized.")}
  is Neg -> !eval(st, f.f)
  is And -> f.lf.all {eval(st, it)}
  is Or -> f.lf.any {eval(st, it)}
}

fun eval(st:Map<Int, Boolean>, f:CNF) : Boolean =
  f.all {
    it.any {
      st.getOrElse(it.first, {throw java.lang.IllegalArgumentException("Variable ${it.first} uninitialized.")}) == it.second
    }
  }

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

fun deserialize(fileName:String) : CNF {
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
      info = text.split(" ")
    }
  }
  if (info == null) {
    throw java.lang.IllegalArgumentException("File $fileName doesn't contains any cnf form")
  }
  val nvar = info[2].toInt()
  val nclause = info[3].toInt()
  for (j in count until (count + nclause)) {
    val text = file[j]
    val clause = text.split(" ")
    formula.add(clause.map{
      abs(it.toInt()) to (it.toInt() > 0)
    })
  }
  return formula
}