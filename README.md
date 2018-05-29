Scala CLI Tools
===============

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/67d9aaafa59a414e9b808ef45abb3fb0)](https://www.codacy.com/app/wookietreiber/scala-cli-tools?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=wookietreiber/scala-cli-tools&amp;utm_campaign=Badge_Grade)
[![Scaladoc](https://javadoc-badge.appspot.com/com.github.wookietreiber/scala-cli-tools_2.12.svg?label=scaladoc)](https://javadoc-badge.appspot.com/com.github.wookietreiber/scala-cli-tools_2.12)

A collection of Scala command-line interface tools.

Table of Contents
-----------------

<!-- toc -->

- [Library](#library)
  * [Memory](#memory)
  * [Table](#table)
- [Apps](#apps)
- [Installation](#installation)
  * [Arch Linux](#arch-linux)
  * [Manual](#manual)

<!-- tocstop -->

Library
-------

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

Apps
----

- **dehumanize** un-pretty-print byte values
- **highlight** search for pattern and highlight matches
- **humanize** pretty-print byte values

Installation
------------

### Arch Linux

Install all apps with the [**scala-cli-tools** AUR package](https://aur.archlinux.org/packages/scala-cli-tools/):

```bash
pacaur -S scala-cli-tools
```

### Manual

Install all apps to `~/bin`:

```bash
PREFIX=$HOME NATIVE_MODE=release \
  sbt install
```
