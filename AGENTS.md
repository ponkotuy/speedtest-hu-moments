# AGENTS.md

## Repository Purpose

This repository compares Hu Moments implementations on Scala/JVM. OpenCV is the source of truth for numeric results.

## Project Rules

- Treat the repository as Apache-2.0 licensed. Keep `LICENSE` present and add `SPDX-License-Identifier: Apache-2.0` to new source/config files when practical.
- Keep Hu Moments semantics aligned with OpenCV `Imgproc.moments(image, false)`.
- Do not binarize pixels unless a future task explicitly changes the benchmark target.
- Any new implementation must be checked against `OpenCvHuMoments` before benchmark results are trusted.
- Generated benchmark/test images belong under `target/hu-images`; do not commit generated image binaries.
- Prefer simple loop-based implementations for benchmarked hot paths so allocation and iterator overhead are visible.

## Verification Commands

```bash
sbt test
```

```bash
sbt "runMain humoments.HuMomentsCheck"
```

```bash
sbt "Jmh/run -i 5 -wi 3 -f1 -tu ms humoments.HuMomentsBenchmark"
```

Use `-Dhu.image=/path/to/image.png` to run the checker or benchmark with a real image. Grayscale images are preferred because ImageIO and OpenCV can decode color images differently.
