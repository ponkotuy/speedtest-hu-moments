// SPDX-License-Identifier: Apache-2.0

package humoments

import java.awt.image.BufferedImage
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import org.opencv.core.Mat

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class HuMomentsBenchmark {
  private var path: Path = _
  private var image: BufferedImage = _
  private var array: Array[Array[Double]] = _
  private var mat: Mat = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val overrideImage = sys.props.get("hu.image").map(Path.of(_))
    path = overrideImage.getOrElse(GeneratedImages.writeIfMissing(GeneratedImages.defaultDirectory, GeneratedImages.DefaultLarge))

    image = ImageIoImages.read(path)
    array = ImageIoImages.toGrayscaleArray(image)
    mat = OpenCvHuMoments.readGrayscale(path)

    val expected = OpenCvHuMoments.compute(mat)
    HuMomentsComparison.requireClose("existing-array", path, expected, HuMoments.compute(array))
    HuMomentsComparison.requireClose("imageio-bufferedimage", path, expected, ImageIoHuMoments.compute(image))
  }

  @TearDown(Level.Trial)
  def tearDown(): Unit =
    if (mat != null) mat.release()

  @Benchmark
  def existingArrayEndToEnd(): Array[Double] =
    HuMoments.compute(ImageIoImages.readGrayscaleArray(path))

  @Benchmark
  def imageIoEndToEnd(): Array[Double] =
    ImageIoHuMoments.compute(path)

  @Benchmark
  def openCvEndToEnd(): Array[Double] =
    OpenCvHuMoments.compute(path)

  @Benchmark
  def existingArrayPreloaded(): Array[Double] =
    HuMoments.compute(array)

  @Benchmark
  def imageIoPreloaded(): Array[Double] =
    ImageIoHuMoments.compute(image)

  @Benchmark
  def openCvPreloaded(): Array[Double] =
    OpenCvHuMoments.compute(mat)
}
