sealed class PresburgerArithmetic

typealias PA = PresburgerArithmetic
data class Var(val s:String) : PresburgerArithmetic()
data class Int(val n:Int) : PresburgerArithmetic()
data class Add(val first:PA, val second:PA) : PresburgerArithmetic()

sealed class PresburgerFormula
typealias PF = PresburgerFormula
data class Eq(val first:PA, val second:PA) : PresburgerFormula()
data class Div(val n : Int, val multiple:PA) : PresburgerFormula()
data class Neg(val f:PF) : PresburgerFormula()
data class And(val lf:List<PF>) : PresburgerFormula()
data class Or(val lf:List<PF>) : PresburgerFormula()
data class Exists(val q:String, val body:PF) : PresburgerFormula()
data class Forall(val q:String, val body:PF) : PresburgerFormula()