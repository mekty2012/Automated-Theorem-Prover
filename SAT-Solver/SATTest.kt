import org.junit.Assert.*
import org.junit.Test

class PropFormulaTest {
  @Test
  fun evalFormulas() {
    val st= mapOf("x" to true, "y" to false)
    val f1 = Var("x")
    assertEquals(eval(st, f1), true)
    val f2 = Var("y")
    assertEquals(eval(st, f2), false)

    val f3 = Neg(Var("x"))
    assertEquals(eval(st, f3), false)
    val f4 = Neg(Var("y"))
    assertEquals(eval(st, f4), true)

    val f5 = And(listOf(Var("x"), Var("x"), Var("x")))
    assertEquals(eval(st, f5), true)
    val f6 = And(listOf(Var("y"), Var("x"), Var("x")))
    assertEquals(eval(st, f6), false)


    val f7 = Or(listOf(Var("y"), Var("y"), Var("y")))
    assertEquals(eval(st, f7), false)
    val f8 = Or(listOf(Var("x"), Var("y"), Var("y")))
    assertEquals(eval(st, f8), true)
  }
  
  @Test
  fun isCNFTest() {
    val f1 = And(Or(Var("x"), Var("y")), Or(Var("x"), Neg(Var("y"))))
    assertEquals(isCNF(f1), true)
    assertEquals(isCNF(Var("x")), false)
    assertEquals(isCNF(Neg(Var("x"))), false)
  }
}

