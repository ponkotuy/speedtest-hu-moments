package humoments

import java.awt.image.BufferedImage
import java.nio.file.Path

object ImageIoHuMoments {
  def compute(path: Path): Array[Double] =
    compute(ImageIoImages.read(path))

  def compute(image: BufferedImage): Array[Double] =
    HuMoments.fromMoments(rawMoments(image))

  def rawMoments(image: BufferedImage): HuMoments.Moments = {
    val width = image.getWidth
    val height = image.getHeight
    val raster = image.getRaster
    val bands = raster.getNumBands

    var m00, m10, m01, m20, m11, m02, m30, m21, m12, m03 = 0.0

    var y = 0
    while (y < height) {
      val yd = y.toDouble
      val yd2 = yd * yd
      val yd3 = yd2 * yd
      var x = 0
      while (x < width) {
        val v =
          if (bands == 1) raster.getSampleDouble(x, y, 0)
          else ImageIoImages.rgbToGray(image.getRGB(x, y))

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

    HuMoments.Moments(m00, m10, m01, m20, m11, m02, m30, m21, m12, m03)
  }
}
