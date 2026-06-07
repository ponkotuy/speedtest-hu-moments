package humoments

/**
 * Dependency-free Hu Moments implementation.
 *
 * Coordinates match OpenCV: x is the column index, y is the row index, and the
 * origin is the upper-left corner.
 */
object HuMoments {

  final case class Moments(
      m00: Double,
      m10: Double,
      m01: Double,
      m20: Double,
      m11: Double,
      m02: Double,
      m30: Double,
      m21: Double,
      m12: Double,
      m03: Double
  )

  /**
   * Computes spatial moments from a raster represented as img(y)(x).
   *
   * Pixel values are treated as grayscale weights. This corresponds to
   * OpenCV's moments(image, binaryImage = false).
   */
  def rawMoments(img: Array[Array[Double]]): Moments = {
    val h = img.length
    val w = if (h > 0) img(0).length else 0

    var m00, m10, m01, m20, m11, m02, m30, m21, m12, m03 = 0.0

    var y = 0
    while (y < h) {
      val row = img(y)
      val yd = y.toDouble
      val yd2 = yd * yd
      val yd3 = yd2 * yd
      var x = 0
      while (x < w) {
        val v = row(x)
        if (v != 0.0) {
          val xd = x.toDouble
          val xd2 = xd * xd
          val xd3 = xd2 * xd
          m00 += v
          m10 += xd * v
          m01 += yd * v
          m20 += xd2 * v
          m11 += xd * yd * v
          m02 += yd2 * v
          m30 += xd3 * v
          m21 += xd2 * yd * v
          m12 += xd * yd2 * v
          m03 += yd3 * v
        }
        x += 1
      }
      y += 1
    }

    Moments(m00, m10, m01, m20, m11, m02, m30, m21, m12, m03)
  }

  def fromMoments(m: Moments): Array[Double] = {
    val m00 = m.m00
    if (m00 == 0.0) return Array.fill(7)(0.0)

    val xBar = m.m10 / m00
    val yBar = m.m01 / m00

    val mu20 = m.m20 - xBar * m.m10
    val mu02 = m.m02 - yBar * m.m01
    val mu11 = m.m11 - xBar * m.m01
    val mu30 = m.m30 - 3 * xBar * m.m20 + 2 * xBar * xBar * m.m10
    val mu03 = m.m03 - 3 * yBar * m.m02 + 2 * yBar * yBar * m.m01
    val mu21 = m.m21 - 2 * xBar * m.m11 - yBar * m.m20 + 2 * xBar * xBar * m.m01
    val mu12 = m.m12 - 2 * yBar * m.m11 - xBar * m.m02 + 2 * yBar * yBar * m.m10

    val inv2 = 1.0 / (m00 * m00)
    val inv25 = 1.0 / math.pow(m00, 2.5)

    val n20 = mu20 * inv2
    val n02 = mu02 * inv2
    val n11 = mu11 * inv2
    val n30 = mu30 * inv25
    val n03 = mu03 * inv25
    val n21 = mu21 * inv25
    val n12 = mu12 * inv25

    val t0 = n30 + n12
    val t1 = n21 + n03
    val q0 = t0 * t0
    val q1 = t1 * t1
    val d = n20 - n02
    val s = n20 + n02

    val h1 = s
    val h2 = d * d + 4 * n11 * n11
    val h3 = (n30 - 3 * n12) * (n30 - 3 * n12) + (3 * n21 - n03) * (3 * n21 - n03)
    val h4 = q0 + q1
    val h5 =
      (n30 - 3 * n12) * t0 * (q0 - 3 * q1) +
        (3 * n21 - n03) * t1 * (3 * q0 - q1)
    val h6 = d * (q0 - q1) + 4 * n11 * t0 * t1
    val h7 =
      (3 * n21 - n03) * t0 * (q0 - 3 * q1) -
        (n30 - 3 * n12) * t1 * (3 * q0 - q1)

    Array(h1, h2, h3, h4, h5, h6, h7)
  }

  def compute(img: Array[Array[Double]]): Array[Double] =
    fromMoments(rawMoments(img))

  def matchShapesI1(a: Array[Double], b: Array[Double]): Double = {
    def transform(v: Double): Double =
      if (v == 0.0) 0.0 else math.signum(v) * math.log10(math.abs(v))

    var sum = 0.0
    var i = 0
    while (i < 7) {
      val ma = transform(a(i))
      val mb = transform(b(i))
      if (ma != 0.0 || mb != 0.0) sum += math.abs(1.0 / ma - 1.0 / mb)
      i += 1
    }
    sum
  }
}
