package scalax.cli

import scala.util.matching.Regex

/** Humanize or dehumanize bytes.
  *
  * {{{
  * import scalax.cli.Memory
  *
  * Memory.humanize(10485760) // "10 MiB"
  *
  * implicit val MU = Memory.SI
  * Memory.dehumanize("10 MB") // Some(10000000)
  * }}}
  *
  * @groupname units Memory Units
  * @groupname conversion Conversion
  */
object Memory {

  /** Represents [[https://en.wikipedia.org/wiki/Byte#Unit_symbol memory
    * units]].
    *
    * @group units
    */
  sealed abstract class MemoryUnit {

    /** Returns the base. */
    def base: Long

    /** Returns the units. */
    def units: List[String]

  }

  /** Memory unit with base 1024 and units like '''MiB'''.
    *
    * @group units
    */
  object IEC extends MemoryUnit {
    override val base: Long = 1024L
    override val units: List[String] =
      List("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB")
  }

  /** Memory unit with base 1000 and units like '''TB'''.
    *
    * @group units
    */
  object SI extends MemoryUnit {
    override val base: Long = 1000L
    override val units: List[String] =
      List("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
  }

  private[Memory] val units: List[String] =
    List(
      "(?:|B)",
      "[kK](?:|iB|B)",
      "M(?:|iB|B)",
      "G(?:|iB|B)",
      "T(?:|iB|B)",
      "P(?:|iB|B)",
      "E(?:|iB|B)",
      "Z(?:|iB|B)",
      "Y(?:|iB|B)"
    )

  private[Memory] val regexes: List[(Regex, Int)] = {
    val integral = """(?:0|[123456789][0123456789]*)"""
    val fractional = """(?:|[.][0123456789]+)""".r

    units.zipWithIndex map {
      case (unit, exponent) =>
        val regex = s"""(-?${integral}${fractional}) ?${unit}""".r
        (regex, exponent)
    }
  }

  /** Returns bytes.
    *
    * @group conversion
    */
  def dehumanize(s: String)(implicit mu: MemoryUnit = IEC): Option[Double] = {
    regexes
      .collect({
        case (regex, exponent) =>
          s match {
            case regex(value) =>
              Some(value.toDouble * math.pow(mu.base, exponent))
            case _ =>
              None
          }
      })
      .flatten
      .headOption
  }

  /** Returns human readable bytes.
    *
    * @group conversion
    */
  def humanize(bytes: Long)(implicit mu: MemoryUnit = IEC): String = {
    def recurse(units: List[String], n: Int): String = {
      val unit :: tail = units

      if (bytes > math.pow(mu.base, n + 1)) {
        recurse(tail, n + 1)
      } else {
        s"${(bytes / math.pow(mu.base, n)).round} $unit"
      }
    }

    recurse(mu.units, 0)
  }
}
