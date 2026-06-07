package humoments

import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO

object ImageIoImages {
  def read(path: Path): BufferedImage = {
    val image = ImageIO.read(path.toFile)
    if (image == null) {
      throw new IllegalArgumentException(s"ImageIO could not read image: $path")
    }
    image
  }

  def readGrayscaleArray(path: Path): Array[Array[Double]] =
    toGrayscaleArray(read(path))

  def toGrayscaleArray(image: BufferedImage): Array[Array[Double]] = {
    val width = image.getWidth
    val height = image.getHeight
    val out = Array.ofDim[Double](height, width)
    val raster = image.getRaster
    val bands = raster.getNumBands

    var y = 0
    while (y < height) {
      val row = out(y)
      var x = 0
      while (x < width) {
        row(x) =
          if (bands == 1) raster.getSampleDouble(x, y, 0)
          else rgbToGray(image.getRGB(x, y))
        x += 1
      }
      y += 1
    }

    out
  }

  private[humoments] def rgbToGray(argb: Int): Double = {
    val r = (argb >> 16) & 0xff
    val g = (argb >> 8) & 0xff
    val b = argb & 0xff
    0.299 * r + 0.587 * g + 0.114 * b
  }
}
