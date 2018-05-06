package scalax.cli

import scala.collection.mutable.ListBuffer

/** Print a pretty command line table.
  *
  * {{{
  * val table = Table("h1", "h2", "h3")
  *
  * table.rows += Seq("a", "b", "c")
  * table.rows += Seq("d", "e", "f")
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

  // TODO compiler assurance: all rows have the same length
  // TODO compiler assurance: number of alignments is number of columns
  // TODO compiler assurance: number of header columns is number of columns

  /** Table column horizontal alignment. */
  sealed trait Alignment

  /** Contains alignments. */
  object Alignment {
    object Center extends Alignment
    object Left extends Alignment
    object Right extends Alignment
  }

  /** Returns a new table builder.
    *
    * @usecase def apply(header: String*): Builder = ???
    *   @inheritdoc
    */
  def apply(h: String, header: String*): Builder =
    new Builder(h +: header)

  /** Returns a new table builder. */
  def apply(header: Seq[String]): Builder =
    new Builder(header)

  /** A table builder.
    *
    * @param header Returns the table header.
    */
  class Builder private[Table] (val header: Seq[String]) {
    require(header.size > 0)

    /** The rows of the table. */
    object rows {
      private[Table] val rows = ListBuffer[Seq[String]]()

      /** Adds a row to the table. */
      def +=(row: Seq[String]): Unit = {
        require(row.size == header.size)
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
      Table.lines(rows.rows, padding)(header: _*)(alignments.alignments: _*)
    }

    /** Prints the table.
      *
      * @usecase def print(): Unit = ???
      *   @inheritdoc
      */
    def print(stream: java.io.PrintStream = Console.out): Unit = {
      lines foreach stream.println
    }
  }

  /** Pretty prints a table on the console.
    *
    * Rows, header and align must all have the same size.
    *
    * If rows is empty, nothing happens.
    */
  def lines(rows: Seq[Seq[String]], padding: Int = 1)(header: String*)(alignments: Alignment*): List[String] =
    if (rows.isEmpty) {
      Nil
    } else {
      val size = header.size

      require(rows.forall(_.size == size))
      require(alignments.size == size)
      require(padding >= 0)

      object table {
        val data: Seq[Seq[String]] = Seq(header) ++ rows // TODO should work without
        val padding = 1 // TODO not using padding here ...
        object size {
          val data: Seq[Int] = for (i <- header.indices) yield // TODO don't rely on indices
            table.data.map(_(i).size).max
          val padded: Seq[Int] = if (data.size > 2) {
            val p = data.map(_ + padding * 2)
            // TODO better solution than using updated?
            p.updated(0, p.head - padding).updated(p.size - 1, p.last - padding)
          } else {
            data.map(_ + padding)
          }
        }
      }

      def makeRow(row: Seq[String]): String = {
        (row.zipWithIndex zip table.size.data).map({
          case ((cell, index), size) =>
            val l = cell.length

            alignments(index) match {
              case Alignment.Center =>
                val prefix = " " *  ((size - l) / 2)
                val suffix = " " * (((size - l) / 2) + (size - l) % 2)

                s"$prefix$cell$suffix"

              case Alignment.Left =>
                val suffix = " " * (size - l)
                s"$cell$suffix"

              case Alignment.Right =>
                val prefix = " " * (size - l)
                s"$prefix$cell"
            }
        }).mkString(" | ")
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
