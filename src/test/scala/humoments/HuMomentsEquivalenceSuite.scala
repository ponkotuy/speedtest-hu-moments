// SPDX-License-Identifier: Apache-2.0

package humoments

import java.nio.file.Path

class HuMomentsEquivalenceSuite extends munit.FunSuite {
  test("generated grayscale images match OpenCV") {
    val paths = GeneratedImages.ensureDefaultImages()
    paths.foreach(assertMatchesOpenCv)
  }

  test("zero raster returns zero Hu Moments") {
    val values = HuMoments.compute(Array.fill(12, 18)(0.0))
    assertEquals(values.toSeq, Seq.fill(7)(0.0))
  }

  private def assertMatchesOpenCv(path: Path): Unit = {
    val expected = OpenCvHuMoments.compute(path)
    val existing = HuMoments.compute(ImageIoImages.readGrayscaleArray(path))
    val imageIo = ImageIoHuMoments.compute(path)

    HuMomentsComparison.requireClose("existing-array", path, expected, existing)
    HuMomentsComparison.requireClose("imageio-bufferedimage", path, expected, imageIo)
  }
}
