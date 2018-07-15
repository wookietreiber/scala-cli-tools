package scalax.cli

import scala.io.Source
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/** Provides statistics related functions.
  *
  * == meansd ==
  *
  * The used algorithm is described
  * [[https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
  * here]].
  *
  * {{{
  * import scalax.cli.Stats
  *
  * // builds mean and standard deviation
  * val numbers = Iterator(1.0, 2.0, 3.0)
  * val (n, mean, sd) = Stats.meansd(numbers)
  * }}}
  */
object Stats {

  /** Returns amount of numbers read, their mean and standard deviation.
    *
    * @note sources are not closed
    */
  def meansd(sources: List[Source])(
      handler: Throwable => Unit): (Long, Double, Double) = {
    val numbers: Iterator[Double] = for {
      source <- sources.iterator
      line <- source.getLines

      number <- Try(line.toDouble) match {
        case Success(value) =>
          Some(value)
        case Failure(e) =>
          handler(e)
          None
      }
    } yield number

    meansd(numbers)
  }

  /** Returns amount of numbers read, their mean and standard deviation. */
  def meansd(numbers: Iterator[Double]): (Long, Double, Double) = {
    var n = 0L
    var mean = 0.0
    var m2 = 0.0
    var delta = 0.0

    for (x <- numbers) {
      n += 1
      delta = x - mean
      mean += delta / n
      m2 += delta * (x - mean)
    }

    val sd = math.sqrt(m2 / (n - 1))

    (n, mean, sd)
  }

  /** Returns amount of numbers read, their mean and standard deviation,
    * grouped by bins like in a histogram.
    *
    * @note sources are not closed
    */
  def meansdBinned(sources: List[Source], binWidth: Long)(
      handler: Throwable => Unit): Seq[(Long, Long, Long, Double, Double)] = {
    val numbers: Iterator[Double] = for {
      source <- sources.iterator
      line <- source.getLines

      number <- Try(line.toDouble) match {
        case Success(value) =>
          Some(value)
        case Failure(e) =>
          handler(e)
          None
      }
    } yield number

    meansdBinned(numbers, binWidth)
  }

  private[Stats] class Accumulate(
      var n: Long = 0L,
      var mean: Double = 0.0,
      var m2: Double = 0.0
  ) {
    def update(x: Double): Unit = {
      n += 1
      val delta = x - mean
      mean += delta / n
      m2 += delta * (x - mean)
    }
  }

  /** Returns amount of numbers read, their mean and standard deviation,
    * grouped by bins like in a histogram.
    */
  def meansdBinned(numbers: Iterator[Double],
                   binWidth: Long): Seq[(Long, Long, Long, Double, Double)] = {
    val data = collection.mutable.Map[Long, Accumulate]()

    for (x <- numbers) {
      val binindex = (x / binWidth).floor.round

      data.get(binindex) match {
        case Some(acc) =>
          acc.update(x)
        case None =>
          val acc = new Accumulate
          acc.update(x)
          data += ((binindex, acc))
      }
    }

    for ((binindex, acc) <- data.toSeq.sortBy(_._1)) yield {
      val binbottom = binindex * binWidth
      val bintop = (binindex + 1) * binWidth

      val sd = math.sqrt(acc.m2 / (acc.n - 1))

      (binbottom, bintop, acc.n, acc.mean, sd)
    }
  }

}
