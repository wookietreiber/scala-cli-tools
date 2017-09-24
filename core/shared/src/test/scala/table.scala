package scalax.cli

import utest._

import Table.Alignment

object TableTests extends TestSuite {
  val tests = this {
    'left {
      val table = Table("1", "2", "3")
      table.rows += Seq("a", "blah", "c")
      table.rows += Seq("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 | 2    | 3
           |--|------|--
           |a | blah | c
           |d | e    | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'right {
      val table = Table("1", "2", "3")
      table.alignments(1) = Alignment.Right
      table.rows += Seq("a", "blah", "c")
      table.rows += Seq("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 |    2 | 3
           |--|------|--
           |a | blah | c
           |d |    e | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'centerodd {
      val table = Table("1", "2", "3")
      table.alignments(1) = Alignment.Center
      table.rows += Seq("a", "foo", "c")
      table.rows += Seq("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 |  2  | 3
           |--|-----|--
           |a | foo | c
           |d |  e  | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }

    'centereven {
      val table = Table("1", "2", "3")
      table.alignments(1) = Alignment.Center
      table.rows += Seq("a", "blah", "c")
      table.rows += Seq("d", "e", "f")

      val lines = table.lines

      val expected =
        """|1 |  2   | 3
           |--|------|--
           |a | blah | c
           |d |  e   | f""".stripMargin

      assert(lines.mkString("\n") == expected)
    }
  }
}
