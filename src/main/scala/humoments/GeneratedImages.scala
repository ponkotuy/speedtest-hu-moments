// SPDX-License-Identifier: Apache-2.0

package humoments

import java.awt.image.BufferedImage
import java.nio.file.{Files, Path, Paths}
import javax.imageio.ImageIO

object GeneratedImages {
  final case class Spec(name: String, width: Int, height: Int, variant: Int)

  val DefaultLarge: Spec = Spec("generated-1280x2000.png", 1280, 2000, 1)
  val DefaultSmall: Spec = Spec("generated-320x500.png", 320, 500, 2)

  def defaultDirectory: Path = Paths.get("target", "hu-images")

  def ensureDefaultImages(directory: Path = defaultDirectory): Seq[Path] =
    Seq(DefaultSmall, DefaultLarge).map(writeIfMissing(directory, _))

  def writeIfMissing(directory: Path, spec: Spec): Path = {
    val path = directory.resolve(spec.name)
    if (!Files.exists(path)) write(directory, spec)
    path
  }

  def write(directory: Path, spec: Spec): Path = {
    Files.createDirectories(directory)
    val image = new BufferedImage(spec.width, spec.height, BufferedImage.TYPE_BYTE_GRAY)
    val raster = image.getRaster

    val cx = spec.width * (0.42 + 0.04 * spec.variant)
    val cy = spec.height * (0.48 + 0.03 * spec.variant)
    val rx = spec.width * (0.23 + 0.02 * spec.variant)
    val ry = spec.height * (0.19 + 0.015 * spec.variant)

    var y = 0
    while (y < spec.height) {
      var x = 0
      while (x < spec.width) {
        val gradient = (x * 17 + y * 31 + (x * y + spec.variant * 97) % 251) & 0xff
        val wave = ((math.sin((x + spec.variant * 11) / 37.0) + 1.0) * 30.0).toInt
        val dx = (x - cx) / rx
        val dy = (y - cy) / ry
        val insideEllipse = dx * dx + dy * dy <= 1.0
        val diagonalBand = math.abs((y - spec.height * 0.18) - x * 0.72) < 26 + spec.variant * 6

        val value =
          if (insideEllipse) 80 + ((gradient + wave) % 176)
          else if (diagonalBand) 35 + ((gradient / 2 + wave) % 120)
          else if (((x / 41) + (y / 53) + spec.variant) % 7 == 0) 10 + (gradient % 35)
          else 0

        raster.setSample(x, y, 0, value)
        x += 1
      }
      y += 1
    }

    val path = directory.resolve(spec.name)
    ImageIO.write(image, "png", path.toFile)
    path
  }
}
