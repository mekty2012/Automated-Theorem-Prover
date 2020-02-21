sealed class PresburgerArithmetic

data class Var(val s:String) : PresburgerArithmetic()
data class Integer(val n:Int) : PresburgerArithmetic()
data class Add(val first:PresburgerArithmetic, val second:PresburgerArithmetic) : PresburgerArithmetic()

sealed class PresburgerFormula
data class Eq(val first:PresburgerArithmetic, val second:PresburgerArithmetic) : PresburgerFormula()
data class Div(val n : Int, val multiple:PresburgerArithmetic) : PresburgerFormula()
data class Neg(val f:PresburgerFormula) : PresburgerFormula()
data class And(val lf:List<PresburgerFormula>) : PresburgerFormula()
data class Or(val lf:List<PresburgerFormula>) : PresburgerFormula()
data class Exists(val q:String, val body:PresburgerFormula) : PresburgerFormula()
data class Forall(val q:String, val body:PresburgerFormula) : PresburgerFormula()

fun simplify(pa:PresburgerArithmetic) : Map<String?, Int> =
  when (pa) {
    is Var -> mapOf(pa.s to 1)
    is Integer -> mapOf(null to pa.n)
    is Add -> {
      val first = simplify(pa.first)
      val second = simplify(pa.second)
      val newKeySet = first.keys + second.keys
      return newKeySet.map{Pair(it, pa.first.getOrDefault(it, 0) + pa.second.getOrDefault(it, 0))}
    }
  }

fun isClosed(f:PresburgerArithmetic, env:Set<String>) : Boolean =
  when (f) {
    is Var -> env.contains(f.s)
    is Integer -> true
    is Add -> isClosed(f.first, env) && isClosed(f.second, env)
  }

fun isClosed(f:PresburgerFormula, env:Set<String>) : Boolean =
  when (f) {
    is Eq -> isClosed(f.first, env) && isClosed(f.second, env)
    is Div -> isClosed(f.multiple, env)
    is Neg -> isClosed(f.f, env)
    is And -> f.lf.all{isClosed(it, env)}
    is Or -> f.lf.all{isClosed(it, env)}
    is Exists -> isClosed(f.body, env + f.q)
    is Forall -> isClosed(f.body, env + f.q)
  }

fun isQuantifierFree(f:PresburgerFormula) : Boolean =
  when (f) {
    is Eq -> true
    is Div -> true
    is Neg -> isQuantifierFree(f.f)
    is And -> f.lf.all{isQuantifierFree(it)}
    is Or -> f.lf.all{isQuantifierFree(it)}
    is Exists -> false
    is Forall -> false
  }

fun compute(f:PresburgerArithmetic) : Int {
  return when (f) {
    is Var -> throw IllegalArgumentException("The formula is not closed.")
    is Integer -> f.n
    is Add -> compute(f.first) + compute(f.second)
  }
}

fun freeDecide(f:PresburgerFormula) : Boolean {
  return when (f) {
    is Eq -> compute(f.first) == compute(f.second)
    is Div -> (compute(f.multiple) % f.n) == 0
    is Neg -> !freeDecide(f.f)
    is And -> f.lf.all{freeDecide(it)}
    is Or -> f.lf.any{freeDecide(it)}
    else -> {throw IllegalArgumentException("The formula is not quantifier free.")}
  }
}

/*
 * This function pushes all the negation to bottom case for quantifier free formula.
 * More specifically, it uses De Morgan's law to push all negation, so that
 * all negation appearing in formula's parameter f is either Eq or Div.
 */
fun pushNegation(f:PresburgerFormula, b:Boolean) : PresburgerFormula {
  return when (f) {
    is Neg -> pushNegation(f.f, !b)
    is And -> if (b) And(f.lf.map{pushNegation(it, true)}) else Or(f.lf.map{pushNegation(it, false)})
    is Or -> if (b) Or(f.lf.map{pushNegation(it, true)}) else And(f.lf.map{pushNegation(it, false)})
    is Exists -> throw IllegalArgumentException("The formula is not quantifier free.")
    is Forall -> throw IllegalArgumentException("The formula is not quantifier free.")
    else -> {if (b) f else Neg(f)}
  }
}

/*
 * This function returns equivalent DNF form for quantifier free formula, having all its negation to bottom case.
 */
fun convertDNF(f:PresburgerFormula) : List<List<PresburgerFormula>> {
  return when (f) {
    is Neg -> listOf() // If we meet negation, it is already literal. So we just simply skip.
    is Or -> f.lf.map{convertDNF(it)}.flatten()
    is And -> f.lf.fold(listOf(listOf())){dnf : List<List<PresburgerFormula>>, curr:PresburgerFormula ->
      val currDNF = convertDNF(curr)
      val result = mutableListOf<List<PresburgerFormula>>()
      for (clauseFst in dnf) {
        for (clauseSnd in currDNF) {
          result.add(clauseFst + clauseSnd)
        }
      }
      return result
    } // This case is where exponentially increasing happens.
    else -> {throw IllegalArgumentException("The formula is not quantifier free.")}
  }
}

/*
 * This function returns formula that is equivalent to exists x, f, where f is quantifier free formula.
 * The input for f is expected to be DNF form.
 */
fun equivExist(dnf:List<List<PresburgerFormula>>, x:String) : PresburgerFormula {
  TODO()
}