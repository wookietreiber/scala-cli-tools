package scalax.cli

import scala.collection.mutable.ListBuffer
import shapeless.Nat
import shapeless.nat._0
import shapeless.ops.nat.GT.>
import shapeless.Sized

/** Print a pretty command line table.
  *
  * {{{
  * import scalax.cli.Table
  * import shapeless.Sized
  *
  * val table = Table(Sized("h1", "h2", "h3"))
  *
  * table.rows += Sized("a", "b", "c")
  * table.rows += Sized("d", "e", "f")
  *
  * table.alignments(1) = Table.Alignment.Right
  *
  * val lines: List[String] = table.lines
  * }}}
  *
  * Print it:
  *
  * {{{
  * table.print()
  * table.print(Console.err)
  *
  * // h1 | h2 | h3
  * // ---|----|---
  * // a  |  b | c
  * // d  |  e | f
  * }}}
  */
object Table {

  /** Table column horizontal alignment. */
  sealed abstract class Alignment

  /** Contains alignments. */
  object Alignment {
    object Center extends Alignment
    object Left extends Alignment
    object Right extends Alignment
  }

  /** Returns a new table builder. */
  def apply[N <: Nat](header: Sized[Seq[String], N])(
      implicit ev: N > _0): Builder[N] =
    new Builder(header)

  /** A table builder.
    *
    * @param header Returns the table header.
    */
  class Builder[N <: Nat] private[Table] (val header: Sized[Seq[String], N])(
      implicit ev: N > _0) {

    /** The rows of the table. */
    object rows {
      private[Table] val rows = ListBuffer[Seq[String]]()

      /** Adds a row to the table. */
      def +=(row: Sized[Seq[String], N]): Unit = {
        rows += row
      }
    }

    /** The alignments of the table columns. */
    object alignments {
      private[Table] val alignments =
        Array.fill[Alignment](header.size)(Alignment.Left)

      /** Updates the alignment of a column. */
      def update(index: Int, element: Alignment): Unit =
        alignments(index) = element
    }

    private[this] var _padding = 1

    /** Returns the table padding. */
    def padding: Int = _padding

    /** Sets the table padding. */
    def padding_=(p: Int): Unit = {
      require(p >= 0)
      _padding = p
    }

    /** Returns the ready-to-be-printed table. */
    def lines: List[String] = {
      Table.lines(header, rows.rows, alignments.alignments, padding)
    }

    /** Prints the table.
      *
      * @usecase def print(): Unit = ???
      *   @inheritdoc
      */
    def print(stream: java.io.PrintStream = Console.out): Unit =
      lines foreach stream.println
  }

  private[Table] def max(numbers: Seq[Int]): Int =
    numbers.foldLeft(0)(math.max(_, _))

  private[Table] def lines(header: Seq[String],
                           rows: ListBuffer[Seq[String]],
                           alignments: Seq[Alignment],
                           padding: Int): List[String] =
    if (rows.isEmpty) {
      Nil
    } else {
      val data = header +: rows

      val rawSizes: Seq[Int] = for {
        i <- header.indices
      } yield max(data.map(_(i).size))

      val paddedSizes: Seq[Int] =
        rawSizes.zipWithIndex map {
          case (size, i) if i == 0 || i == (header.size - 1) =>
            size + padding
          case (size, i) =>
            size + padding * 2
        }

      val buf = new ListBuffer[String]()

      // header
      buf += makeRow(header, rawSizes, alignments, padding)

      // border between header and body
      buf += paddedSizes.map("-" * _).mkString("|")

      // body
      for (row <- rows) {
        buf += makeRow(row, rawSizes, alignments, padding)
      }

      buf.toList
    }

  private[Table] def makeRow(row: Seq[String],
                             sizes: Seq[Int],
                             alignments: Seq[Alignment],
                             padding: Int): String = {
    val padded = for {
      ((content, col), size) <- (row.zipWithIndex zip sizes)
    } yield {
      val cell = alignContent(content, size, alignments(col))

      val prefix = spaces(if (col == 0) 0 else padding)
      val suffix = spaces(if (col == row.size - 1) 0 else padding)

      s"${prefix}${cell}${suffix}"
    }

    padded.mkString("|")
  }

  private[Table] def alignContent(content: String,
                                  size: Int,
                                  alignment: Alignment): String = {
    val l = content.length

    alignment match {
      case Alignment.Center =>
        val prefix = spaces((size - l) / 2)
        val suffix = spaces(((size - l) / 2) + (size - l) % 2)
        s"${prefix}${content}${suffix}"

      case Alignment.Left =>
        val suffix = spaces(size - l)
        s"${content}${suffix}"

      case Alignment.Right =>
        val prefix = spaces(size - l)
        s"${prefix}${content}"
    }
  }

  private[Table] def spaces(n: Int): String =
    " " * n

}
