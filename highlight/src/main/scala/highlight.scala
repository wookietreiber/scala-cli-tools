package scalax.cli

import java.io.File
import scala.io.AnsiColor
import scala.io.Source
import scala.util.matching.Regex

object highlight extends App with AnsiColor {

  final case class Config(
    ignoreCase: Boolean = false,
    pattern: String = "",
    files: List[File] = Nil
  )

  val parser = new scopt.OptionParser[Config](BuildInfo.name) {
    head(BuildInfo.name, BuildInfo.version)

    arg[String]("pattern")
      .action((x, c) => c.copy(pattern = x))
      .text("search pattern")

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
            |search for pattern and highlight matches
            |
            |options:
            |""".stripMargin)

    opt[Unit]('i', "ignore-case")
      .action((x, c) => c.copy(ignoreCase = true))
      .text("ignore case")

    note("\nother options:\n")

    help("help").text("prints this usage text")

    version("version")

    note("")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      val files = config.files.reverse
      val regex = if (config.ignoreCase) {
        s"(?i)${config.pattern}".r
      } else {
        config.pattern.r
      }

      val sources: List[Source] =
        if (files.isEmpty) {
          List(Source.stdin)
        } else {
          files.map(Source.fromFile)
        }

      for {
        source <- sources
        line <- source.getLines
      } println(highlight(line, regex))

    case None =>
  }

  def highlight(line: String, regex: Regex): String = {
    def replace(m: Regex.Match) =
      s"""${BOLD}${RED}${m.matched}${RESET}"""

    regex.replaceAllIn(line, replace(_))
  }
}
