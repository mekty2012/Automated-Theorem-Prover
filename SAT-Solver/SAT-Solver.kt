sealed class PropFormula
data class Var(val x:String) : PropFormula()
data class Neg(val f:PropFormula) : PropFormula()
data class And(val lf:List<PropFormula>) : PropFormula()
data class Or(val lf:List<PropFormula>) : PropFormula()

fun eval(st:Map<String, Boolean>, f:PropFormula) : Boolean = when (f) {
  is Var -> st.getOrElse(f.x) {throw java.lang.IllegalArgumentException("Variable ${f.x} uninitialized.")}
  is Neg -> !eval(st, f.f)
  is And -> f.lf.all {eval(st, it)}
  is Or -> f.lf.any {eval(st, it)}
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
