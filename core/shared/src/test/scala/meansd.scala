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

    'meansdBin {
      'simple {
        val numbers = Iterator(1.0, 2.0, 3.0, 6.0, 7.0, 8.0)
        val data = Stats.meansdBinned(numbers, 5L).toArray

        val (t0, b0, n0, m0, s0) = data(0)
        val (t1, b1, n1, m1, s1) = data(1)

        assert(
          t0 == 0, b0 ==  5, n0 == 3, m0 == 2, s0 == 1,
          t1 == 5, b1 == 10, n1 == 3, m1 == 7, s1 == 1
        )
      }
    }
  }
}
