package scalax.cli

import utest._

object MemoryTests extends TestSuite {
  val tests = this {
    'iec {
      implicit val MU = Memory.IEC

      'humanize {
        'bytes {
          val orig = 1000
          val expect = "1000 B"
          val result = Memory.humanize(orig)

          assert(result == expect)
        }

        'mebibytes {
          val orig = 10485760
          val expect = "10 MiB"
          val result = Memory.humanize(orig)

          assert(result == expect)
        }
      }

      'dehumanize {
        'bytes {
          val orig = "1000"
          val expect = Some(1000)
          val result = Memory.dehumanize(orig)

          assert(result == expect)
        }

        'mebibytes {
          val orig = "10 MiB"
          val expect = Some(10485760)
          val result = Memory.dehumanize(orig)

          assert(result == expect)
        }

        'du {
          val orig = "9.9K"
          val expect = Some(10137.6)
          val result = Memory.dehumanize(orig)

          assert(result == expect)
        }
      }
    }

    'si {
      implicit val MU = Memory.SI

      'humanize {
        'bytes {
          val orig = 999
          val expect = "999 B"
          val result = Memory.humanize(orig)

          assert(result == expect)
        }

        'megabytes {
          val orig = 10000000
          val expect = "10 MB"
          val result = Memory.humanize(orig)

          assert(result == expect)
        }
      }

      'dehumanize {
        'bytes {
          val orig = "999 B"
          val expect = Some(999)
          val result = Memory.dehumanize(orig)

          assert(result == expect)
        }

        'megabytes {
          val orig = "10 MB"
          val expect = Some(10000000)
          val result = Memory.dehumanize(orig)

          assert(result == expect)
        }

        'du {
          val orig = "9.9K"
          val expect = Some(9900)
          val result = Memory.dehumanize(orig)

          assert(result == expect)
        }
      }
    }
  }
}
