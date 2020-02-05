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