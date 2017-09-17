package scalax.cli

import scala.collection.mutable.ListBuffer
import shapeless._
import shapeless.nat._
import shapeless.ops.nat.GT._
import shapeless.syntax.sized._

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
      // TODO must check that index < N
      def update(index: Int, element: Alignment): Unit =
        alignments(index) = element
    }

    private var _padding = 1

    /** Returns the table padding. */
    def padding: Int = _padding

    /** Sets the table padding. */
    def padding_=(p: Int): Unit = {
      require(p >= 0)
      _padding = p
    }

    /** Returns the ready-to-be-printed table. */
    def lines: List[String] = {
      Table.lines(rows.rows, padding, header, alignments.alignments)
    }

    /** Prints the table.
      *
      * @usecase def print(): Unit = ???
      *   @inheritdoc
      */
    def print(stream: java.io.PrintStream = Console.out): Unit =
      lines foreach stream.println
  }

  private[Table] def lines(rows: ListBuffer[Seq[String]],
                           padding: Int,
                           header: Seq[String],
                           alignments: Seq[Alignment]): List[String] =
    if (rows.isEmpty) {
      Nil
    } else {
      val size = header.size

      require(padding >= 0)

      object table {
        val data: Seq[Seq[String]] = Seq(header) ++ rows

        object size {
          val data: Seq[Int] = for (i <- header.indices)
            yield table.data.map(_(i).size).max

          val padded: Seq[Int] = if (data.size > 2) {
            val p = data.map(_ + padding * 2)
            p.updated(0, p.head - padding)
              .updated(p.size - 1, p.last - padding)
          } else {
            data.map(_ + padding)
          }
        }
      }

      def makeRow(row: Seq[String]): String = {
        (row.zipWithIndex zip table.size.data)
          .map({
            case ((cell, index), size) =>
              val l = cell.length

              alignments(index) match {
                case Alignment.Center =>
                  val prefix = " " * ((size - l) / 2)
                  val suffix = " " * (((size - l) / 2) + (size - l) % 2)

                  s"$prefix$cell$suffix"

                case Alignment.Left =>
                  val suffix = " " * (size - l)
                  s"$cell$suffix"

                case Alignment.Right =>
                  val prefix = " " * (size - l)
                  s"$prefix$cell"
              }
          })
          .mkString(" | ")
      }

      val buf = new ListBuffer[String]()

      // print header
      buf += makeRow(header)

      // print border between header and body
      buf += table.size.padded.map("-" * _).mkString("|")

      // print body
      for (row <- rows) {
        buf += makeRow(row)
      }

      buf.toList
    }

}
