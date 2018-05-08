# Scala CLI Tools

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/67d9aaafa59a414e9b808ef45abb3fb0)](https://www.codacy.com/app/wookietreiber/scala-cli-tools?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=wookietreiber/scala-cli-tools&amp;utm_campaign=Badge_Grade)

A collection of Scala command-line interface tools.

## Library

### Memory

Humanize or dehumanize bytes:

```scala
import scalax.cli.Memory

Memory.humanize(10485760) // "10 MiB"

implicit val MU = Memory.SI
Memory.dehumanize("10 MB") // Some(10000000)
```

### Table

Print a pretty command line table:

```scala
// create the table builder object

import scalax.cli.Table
import shapeless.Sized

val table = Table(Sized("h1", "h2", "h3"))

table.rows += Sized("a", "b", "c")
table.rows += Sized("d", "e", "f")

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
