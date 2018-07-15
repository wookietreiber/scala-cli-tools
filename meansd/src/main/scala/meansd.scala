package scalax.cli

import java.io.File
import scala.io.Source

object meansd extends App {

  final case class Config(
    files: List[File] = Nil,
    binWidth: Option[Long] = None
  )

  val parser = new scopt.OptionParser[Config](BuildInfo.name) {
    head(BuildInfo.name, BuildInfo.version)

    arg[File]("file...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(files = x :: c.files))
      .text("input files, reads STDIN if non are given")
      .validate(x =>
        if (x.exists) {
          success
        } else {
          failure(s"input file $x does not exist")
      })

    note("""|
            |print mean and standard deviation of numbers
            |
            |options:
            |""".stripMargin)

    opt[Long]('w', "bin-width")
      .action((x, c) => c.copy(binWidth = Some(x)))
      .text("specify bin width to group input by")

    note("\nother options:\n")

    help("help").text("prints this usage text")

    version("version")

    note("")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      val files = config.files.reverse

      val sources: List[Source] =
        if (files.isEmpty) {
          List(Source.stdin)
        } else {
          files.map(Source.fromFile)
        }

      val handler = (error: Throwable) => {
        Console.err.println(s"""${BuildInfo.name}: ${error.getMessage}""")
      }

      config.binWidth match {
        case Some(binWidth) =>
          for {
            (binbottom, bintop, n, mean, sd) <- Stats.meansdBinned(sources, binWidth)(handler)
          } println(s"""$binbottom - $bintop -> n=$n ∅ $mean ± $sd""")

        case None =>
          val (n, mean, sd) = Stats.meansd(sources)(handler)
          println(s"""n=$n ∅ $mean ± $sd""")
      }

      if (!files.isEmpty) {
        sources.foreach(_.close())
      }

    case None =>
  }
}
