package humoments

import java.nio.file.Path

object HuMomentsComparison {
  final val DefaultAbsoluteTolerance: Double = 1e-12
  final val DefaultRelativeTolerance: Double = 1e-8

  final case class Difference(
      index: Int,
      expected: Double,
      actual: Double,
      absoluteDiff: Double,
      relativeDiff: Double
  )

  def differences(
      expected: Array[Double],
      actual: Array[Double],
      absoluteTolerance: Double = DefaultAbsoluteTolerance,
      relativeTolerance: Double = DefaultRelativeTolerance
  ): Seq[Difference] = {
    require(expected.length == 7, s"expected Hu Moments length must be 7, got ${expected.length}")
    require(actual.length == 7, s"actual Hu Moments length must be 7, got ${actual.length}")

    val builder = Vector.newBuilder[Difference]
    var i = 0
    while (i < 7) {
      val absDiff = math.abs(expected(i) - actual(i))
      val scale = math.max(math.abs(expected(i)), math.abs(actual(i)))
      val relDiff = if (scale == 0.0) absDiff else absDiff / scale
      if (absDiff > absoluteTolerance && relDiff > relativeTolerance) {
        builder += Difference(i, expected(i), actual(i), absDiff, relDiff)
      }
      i += 1
    }
    builder.result()
  }

  def requireClose(
      implementation: String,
      image: Path,
      expectedOpenCv: Array[Double],
      actual: Array[Double],
      absoluteTolerance: Double = DefaultAbsoluteTolerance,
      relativeTolerance: Double = DefaultRelativeTolerance
  ): Unit = {
    val diffs = differences(expectedOpenCv, actual, absoluteTolerance, relativeTolerance)
    if (diffs.nonEmpty) {
      val details = diffs
        .map { diff =>
          f"index=${diff.index}%d expected=${diff.expected}%.17g actual=${diff.actual}%.17g absDiff=${diff.absoluteDiff}%.17g relDiff=${diff.relativeDiff}%.17g"
        }
        .mkString(System.lineSeparator())
      throw new IllegalStateException(
        s"""Hu Moments differ from OpenCV
           |implementation=$implementation
           |image=$image
           |absoluteTolerance=$absoluteTolerance
           |relativeTolerance=$relativeTolerance
           |$details""".stripMargin
      )
    }
  }
}
