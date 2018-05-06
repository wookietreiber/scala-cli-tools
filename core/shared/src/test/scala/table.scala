package scalax.cli

import utest._
import shapeless.Sized

import Table.Alignment

object TableTests extends TestSuite {
  val tests = this {
    'left {
      val table = Table(Sized("1", "2", "3"))
      table.rows += Sized("a", "blah", "c")
      table.rows += Sized("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 | 2    | 3
           |--|------|--
           |a | blah | c
           |d | e    | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'right {
      val table = Table(Sized("1", "2", "3"))
      table.alignments(1) = Alignment.Right
      table.rows += Sized("a", "blah", "c")
      table.rows += Sized("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 |    2 | 3
           |--|------|--
           |a | blah | c
           |d |    e | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'centerodd {
      val table = Table(Sized("1", "2", "3"))
      table.alignments(1) = Alignment.Center
      table.rows += Sized("a", "foo", "c")
      table.rows += Sized("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 |  2  | 3
           |--|-----|--
           |a | foo | c
           |d |  e  | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'centereven {
      val table = Table(Sized("1", "2", "3"))
      table.alignments(1) = Alignment.Center
      table.rows += Sized("a", "blah", "c")
      table.rows += Sized("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 |  2   | 3
           |--|------|--
           |a | blah | c
           |d |  e   | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'padding {
      val table = Table(Sized("1", "2", "3"))
      table.rows += Sized("a", "blah", "c")
      table.rows += Sized("d", "e", "f")
      table.padding = 4

      val lines = table.lines

      val expected =
        """|1    |    2       |    3
           |-----|------------|-----
           |a    |    blah    |    c
           |d    |    e       |    f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }
  }
}
