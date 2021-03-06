package il.org.spartan.spartanizer.research;

import static org.junit.Assert.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;

import il.org.spartan.spartanizer.cmdline.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.research.patterns.*;

/** @author Ori Marcovitch
 * @since 2016 */
@SuppressWarnings("static-method") public class ExaminerTest {
  static final InteractiveSpartanizer spartanizer = new InteractiveSpartanizer();

  @BeforeClass public static void setUp() {
    spartanizer.add(MethodDeclaration.class, new Examiner());
  }

  @Test public void basic() {
    assertTrue(spartanizer.fixedPoint(makeAST.COMPILATION_UNIT.from("public class A{boolean examiner(){return field == 7;} }") + "")
        .contains("[[Examiner]]"));
  }

  @Test public void comlicated() {
    assertTrue(spartanizer.fixedPoint(makeAST.COMPILATION_UNIT.from(
        "public class A{boolean examiner(Is this, The... real){return life && just.fantasy() && (caught == landslide || noEscape.from(reality));} }")
        + "").contains("[[Examiner]]"));
  }

  @Test public void basicNot() {
    assertFalse(spartanizer.fixedPoint(makeAST.COMPILATION_UNIT.from("public class A{int examiner(){return 7;} }") + "").contains("[[Examiner]]"));
  }
}
