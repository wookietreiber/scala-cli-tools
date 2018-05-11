package scalax.cli

import java.io.File
import scala.io.Source
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scopt.Read

object humanize extends App {

  final case class Config(
      unit: Memory.MemoryUnit = Memory.IEC,
      files: List[File] = Nil
  )

  implicit val MemoryUnitRead: Read[Memory.MemoryUnit] =
    scopt.Read reads { s =>
      s match {
        case "IEC" => Memory.IEC
        case "SI" => Memory.SI
      }
    }

  val parser = new scopt.OptionParser[Config](BuildInfo.name) {
    head(BuildInfo.name, BuildInfo.version)

    arg[File]("file...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(files = x :: c.files))
      .text("input files, reads STDIN if non are given")
      .validate(x =>
        if (x.exists && x.isFile) {
          success
        } else {
          failure(s"input file $x does not exist")
      })

    note("""|
            |humanize byte values
            |
            |options:
            |""".stripMargin)

    opt[Memory.MemoryUnit]('u', "memory-unit")
      .action((x, c) => c.copy(unit = x))
      .text("memory unit, IEC or SI, defaults to IEC")

    note("\nother options:\n")

    help("help").text("prints this usage text")

    version("version")

    note("")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      implicit val MU = config.unit
      val files = config.files.reverse

      val sources: List[Source] =
        if (files.isEmpty) {
          List(Source.stdin)
        } else {
          files.map(Source.fromFile)
        }

      for {
        source <- sources
        line <- source.getLines
      } {
        Try(line.toLong).map(Memory.humanize) match {
          case Success(bytes) =>
            println(bytes)
          case Failure(error) =>
            Console.err.println(error)
        }
      }

    case None =>
  }
}
