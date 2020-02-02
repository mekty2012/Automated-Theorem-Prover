import org.junit.Assert.*
import org.junit.Test

class PropFormulaTest {
  @Test
  fun evalFormulas() {
    val st= mapOf(1 to true, 2 to false)
    val f1 = Var(1)
    assertEquals(eval(st, f1), true)
    val f2 = Var(2)
    assertEquals(eval(st, f2), false)

    val f3 = Neg(Var(1))
    assertEquals(eval(st, f3), false)
    val f4 = Neg(Var(2))
    assertEquals(eval(st, f4), true)

    val f5 = And(listOf(Var(1), Var(1), Var(1)))
    assertEquals(eval(st, f5), true)
    val f6 = And(listOf(Var(2), Var(1), Var(1)))
    assertEquals(eval(st, f6), false)


    val f7 = Or(listOf(Var(2), Var(2), Var(2)))
    assertEquals(eval(st, f7), false)
    val f8 = Or(listOf(Var(1), Var(2), Var(2)))
    assertEquals(eval(st, f8), true)
  }
  
  @Test
  fun isCNFTest() {
    val f1 = And(listOf(Or(listOf(Var(1), Var(2))), Or(listOf(Var(1), Neg(Var(2))))))
    assertEquals(isCNF(f1), true)
    assertEquals(isCNF(Var(1)), false)
    assertEquals(isCNF(Neg(Var(1))), false)
  }

  @Test
  fun deserealizeTest() {
    deserialize("tests/uf75-325/uf75-01.cnf")
  }

  /*
   * TODO :
   *        Add tests for preprocessing functions.
   *        Add tests for polynomial solvable cases.
   *        Add tests for DPLL SAT-solver
   */
}

