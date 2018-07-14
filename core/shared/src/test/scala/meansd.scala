package scalax.cli

import utest._

object MeansdTests extends TestSuite {
  val tests = this {
    'meansd {
      'empty {
        val numbers = Iterator()
        val (n, mean, sd) = Stats.meansd(numbers)

        assert(n == 0, mean == 0, sd == 0)
      }

      'simple {
        val numbers = Iterator(1.0, 2.0, 3.0)
        val (n, mean, sd) = Stats.meansd(numbers)

        assert(n == 3, mean == 2, sd == 1)
      }

      'long {
        val numbers = Iterator.fill(100000)(42.0)
        val (n, mean, sd) = Stats.meansd(numbers)

        assert(n == 100000, mean == 42, sd == 0)
      }
    }
  }
}
