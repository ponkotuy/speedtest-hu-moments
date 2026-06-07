# speedtest-hu-moments

ScalaでHu Momentsの計算結果と速度を比較するためのリポジトリです。OpenCVの `Imgproc.moments(..., false)` と `Imgproc.HuMoments` の結果を正とし、独自実装との差分が許容値を超えた場合はエラーにします。

## 比較する実装

- `HuMoments`: 既存の依存なし実装。`ImageIO` で読んだ画像を `Array[Array[Double]]` に変換してから計算します。
- `ImageIoHuMoments`: Java標準の `ImageIO` / `BufferedImage` / `Raster` を直接走査して計算します。
- `OpenCvHuMoments`: OpenCV Java APIで画像をグレースケールとして読み、Hu Momentsを計算します。

画素値は2値化せず、グレースケールの重みとして扱います。つまりOpenCV側は `binaryImage = false` です。

## 必要なもの

- Java
- sbt

OpenCVは `org.openpnp:opencv` の同梱Jarをsbt依存関係として取得します。OSにOpenCVを別途インストールする必要はありません。

## 差分チェック

```bash
sbt test
```

または任意の画像を指定して確認できます。

```bash
sbt "runMain humoments.HuMomentsCheck /path/to/image.png"
```

画像を指定しない場合は、`target/hu-images` に小サイズ画像と `1280x2000` の決定的なグレースケール画像を生成して検証します。

```bash
sbt "runMain humoments.HuMomentsCheck"
```

許容値は absolute `1e-12`、relative `1e-8` です。差分が出ると、実装名、画像パス、Hu Momentsのindex、expected、actual、absDiff、relDiffを出して失敗します。

## ベンチマーク

JMHが認識しているベンチマーク名は次のコマンドで確認できます。

```bash
sbt "Jmh/run -l"
```

```bash
sbt "Jmh/run -i 5 -wi 3 -f1 -tu ms humoments.HuMomentsBenchmark"
```

Java 24以降ではJMH/OpenCV由来のnative access警告が出やすいため、`build.sbt` でJMH runner、JMH benchmark fork、テスト、`runMain` に警告抑制用のJVMオプションを渡しています。

ベンチマークは2種類あります。

- `*EndToEnd`: 画像読み込みを含めます。ImageIOとOpenCVでファイル処理が異なるため、主に見る指標です。
- `*Preloaded`: 事前ロード済みのデータからHu Moments計算だけを測ります。差の原因を切り分けるための指標です。

実画像で測る場合は次のように指定します。

```bash
sbt -Dhu.image=/path/to/image.png "Jmh/run -i 5 -wi 3 -f1 -tu ms humoments.HuMomentsBenchmark"
```

厳密な差分チェックはImageIOとOpenCVのデコード結果が一致することを前提にしています。生成画像と同様のグレースケール画像での利用を推奨します。

## ライセンス

このプロジェクトはApache License 2.0の下で提供されます。詳細は [LICENSE](LICENSE) を参照してください。

## 結果
[info] Benchmark                                  Mode  Cnt  Score   Error  Units
[info] HuMomentsBenchmark.existingArrayEndToEnd   avgt    5  7.675 ± 0.097  ms/op
[info] HuMomentsBenchmark.existingArrayPreloaded  avgt    5  1.576 ± 0.037  ms/op
[info] HuMomentsBenchmark.imageIoEndToEnd         avgt    5  6.501 ± 0.181  ms/op
[info] HuMomentsBenchmark.imageIoPreloaded        avgt    5  1.991 ± 0.011  ms/op
[info] HuMomentsBenchmark.openCvEndToEnd          avgt    5  5.110 ± 0.027  ms/op
[info] HuMomentsBenchmark.openCvPreloaded         avgt    5  1.024 ± 0.006  ms/op
