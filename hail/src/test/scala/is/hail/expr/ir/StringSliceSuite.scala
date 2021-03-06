package is.hail.expr.ir

import is.hail.utils._
import is.hail.TestUtils._
import is.hail.expr.types._
import is.hail.expr.types.virtual.TString
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

class StringSliceSuite extends TestNGSuite {
  @Test def zeroToLengthIsIdentity() {
    assertEvalsTo(StringSlice(Str("abc"), I32(0), I32(3)), "abc")
  }

  @Test def simpleSlicesMatchIntuition() {
    assertEvalsTo(StringSlice(Str("abc"), I32(3), I32(3)), "")
    assertEvalsTo(StringSlice(Str("abc"), I32(1), I32(3)), "bc")
    assertEvalsTo(StringSlice(Str("abc"), I32(2), I32(3)), "c")
    assertEvalsTo(StringSlice(Str("abc"), I32(0), I32(2)), "ab")
  }

  @Test def sizeZeroSliceIsEmptyString() {
    assertEvalsTo(StringSlice(Str("abc"), I32(2), I32(2)), "")
    assertEvalsTo(StringSlice(Str("abc"), I32(1), I32(1)), "")
    assertEvalsTo(StringSlice(Str("abc"), I32(0), I32(0)), "")
  }

  @Test def sliceMatchesJavaStringSubstring() {
    assertEvalsTo(
      StringSlice(Str("abc"), I32(0), I32(2)),
      "abc".substring(0, 2))
    assertEvalsTo(
      StringSlice(Str("foobarbaz"), I32(3), I32(5)),
      "foobarbaz".substring(3, 5))
  }

  @Test def isStrict() {
    assertEvalsTo(StringSlice(NA(TString()), I32(0), I32(2)), null)
    assertEvalsTo(StringSlice(NA(TString()), I32(-5), I32(-10)), null)
  }

  @Test def sliceCopyIsID() {
    assertEvalsTo(invoke("[:]", Str("abc")), "abc")
  }

  @Test def leftSliceMatchesIntuition() {
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(2)), "c")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(1)), "bc")
  }

  @Test def rightSliceMatchesIntuition() {
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(2)), "ab")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(1)), "a")
  }

  @Test def bothSideSliceMatchesIntuition() {
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(0), I32(2)), "ab")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(1), I32(3)), "bc")
  }

  @Test def leftSliceIsPythony() {
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(-1)), "c")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(-2)), "bc")
  }

  @Test def rightSliceIsPythony() {
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(-1)), "ab")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(-2)), "a")
  }

  @Test def sliceIsPythony() {
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-3), I32(-1)), "ab")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-3), I32(-2)), "a")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-2), I32(-1)), "b")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-2), I32(-2)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-3), I32(-3)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(1), I32(-1)), "b")
  }

  @Test def bothSidesSliceFunctionOutOfBoundsNotFatal() {
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(4), I32(4)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(3), I32(2)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-1), I32(2)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-1), I32(-1)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(3), I32(3)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-10), I32(-5)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-5), I32(-10)), "")
    assertEvalsTo(invoke("[*:*]", Str("abc"), I32(-10), I32(-1)), "ab")
  }

  @Test def leftSliceFunctionOutOfBoundsNotFatal() {
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(15)), "")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(4)), "")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(3)), "")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(-3)), "abc")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(-4)), "abc")
    assertEvalsTo(invoke("[*:]", Str("abc"), I32(-100)), "abc")
  }

  @Test def rightSliceFunctionOutOfBoundsNotFatal() {
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(15)), "abc")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(4)), "abc")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(3)), "abc")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(-3)), "")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(-4)), "")
    assertEvalsTo(invoke("[:*]", Str("abc"), I32(-100)), "")
  }

  @Test def testStringIndex() {
    assertEvalsTo(invoke("[]", In(0, TString()), I32(0)), IndexedSeq("Baz" -> TString()), "B")
    assertEvalsTo(invoke("[]", In(0, TString()), I32(1)), IndexedSeq("Baz" -> TString()), "a")
    assertEvalsTo(invoke("[]", In(0, TString()), I32(2)), IndexedSeq("Baz" -> TString()), "z")
    assertEvalsTo(invoke("[]", In(0, TString()), I32(-1)), IndexedSeq("Baz" -> TString()), "z")
    assertEvalsTo(invoke("[]", In(0, TString()), I32(-2)), IndexedSeq("Baz" -> TString()), "a")
    assertEvalsTo(invoke("[]", In(0, TString()), I32(-3)), IndexedSeq("Baz" -> TString()), "B")

    interceptFatal("string index out of bounds") {
      assertEvalsTo(invoke("[]", In(0, TString()), I32(3)), IndexedSeq("Baz" -> TString()), "B")
    }
    interceptFatal("string index out of bounds") {
      assertEvalsTo(invoke("[]", In(0, TString()), I32(-4)), IndexedSeq("Baz" -> TString()), "B")
    }
  }

  @Test def testStringCopy() {
    assertEvalsTo(invoke("[:]", In(0, TString())), IndexedSeq("Baz" -> TString()), "Baz")
  }
}
