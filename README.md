# Scala CLI Tools

A collection of Scala command-line interface tools.

## Table

Print a pretty command line table.

```scala
// create the table builder object

import scalax.cli.Table

val table = Table("h1", "h2", "h3")

table.rows += Seq("a", "b", "c")
table.rows += Seq("d", "e", "f")

table.alignments(1) = Table.Alignment.Right

// get lines

val lines: List[String] = table.lines

// print it directly

table.print()
table.print(Console.err)

// h1 | h2 | h3
// ---|----|---
// a  |  b | c
// d  |  e | f
```
