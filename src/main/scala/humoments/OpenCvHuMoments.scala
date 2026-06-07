package humoments

import java.nio.file.Path
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

object OpenCvHuMoments {
  @volatile private var loaded = false

  def load(): Unit =
    if (!loaded) this.synchronized {
      if (!loaded) {
        OpenCV.loadLocally()
        loaded = true
      }
    }

  def readGrayscale(path: Path): Mat = {
    load()
    val mat = Imgcodecs.imread(path.toString, Imgcodecs.IMREAD_GRAYSCALE)
    if (mat.empty()) {
      throw new IllegalArgumentException(s"OpenCV could not read image: $path")
    }
    mat
  }

  def compute(path: Path): Array[Double] = {
    val mat = readGrayscale(path)
    try compute(mat)
    finally mat.release()
  }

  def compute(mat: Mat): Array[Double] = {
    load()
    val moments = Imgproc.moments(mat, false)
    val hu = new Mat()
    try {
      Imgproc.HuMoments(moments, hu)
      val out = new Array[Double](7)
      hu.get(0, 0, out)
      out
    } finally {
      hu.release()
    }
  }
}
