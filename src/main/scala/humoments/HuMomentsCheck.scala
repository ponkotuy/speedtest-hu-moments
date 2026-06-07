package humoments

import java.nio.file.{Path, Paths}

object HuMomentsCheck {
  def main(args: Array[String]): Unit = {
    val paths =
      if (args.nonEmpty) args.toSeq.map(Paths.get(_))
      else if (sys.props.contains("hu.image")) Seq(Paths.get(sys.props("hu.image")))
      else GeneratedImages.ensureDefaultImages()

    paths.foreach(check)
  }

  def check(path: Path): Unit = {
    val expected = OpenCvHuMoments.compute(path)
    val existing = HuMoments.compute(ImageIoImages.readGrayscaleArray(path))
    val imageIo = ImageIoHuMoments.compute(path)

    HuMomentsComparison.requireClose("existing-array", path, expected, existing)
    HuMomentsComparison.requireClose("imageio-bufferedimage", path, expected, imageIo)

    println(s"OK $path")
  }
}
