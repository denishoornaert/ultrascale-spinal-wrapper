package ultrascaleplus.clock.pll


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils.Log


case class PLL(name: String, frequency: HertzNumber) {

  def enabled: Boolean = {
    return this.frequency > HertzNumber(0)
  }

}


object PLL {

  case class Config(m: Int, d0: Int, d1: Int, d2: Int, fvco: HertzNumber, fout: HertzNumber) {}

  case class Ranges(m: Range, d0: Range, d1: Range) {}

  private def quantize(frequency: HertzNumber, scale: Int = 6): HertzNumber = {
    return HertzNumber(frequency.toBigDecimal.setScale(scale, BigDecimal.RoundingMode.HALF_UP))
  }
 
  /**
   * Branch and bound. Looking for best match
   */
  def tune(fin: HertzNumber, target: HertzNumber, ranges: Ranges): PLL.Config = {
    var bestConfig: PLL.Config  = null
    var bestError : HertzNumber = HertzNumber(Double.MinValue)

    for {
      d1 <- ranges.d1
      d0 <- ranges.d0
      m  <- ranges.m
    } {
      val fvco = fin*m
      // Detect if div2 is needed
      val div2 = if((fvco/target) > ranges.d0.max*ranges.d1.max) Seq(1, 2, 4, 8) else Seq(1)
      // Search
      for (d2 <- div2) {
        // Reportedly, there is a fixed /2 factor
        val fout = PLL.quantize(fvco/(2*d0*d1*d2))
        // Note error must be the smallest non strictly positive frequency
        val error = fout-target
        if ((error <= HertzNumber(0)) && (bestError < error)) {
          bestError = error
          bestConfig = PLL.Config(m, d0, d1, d2, fvco, fout)
          println(f"${error} -> Config(${m}, ${d0}, ${d1}, ${d2}, ${fvco}, ${fout})")
        }
      }
    }

    return bestConfig
  }

  object IO {

    val frequencies = Seq(333.329987 MHz, 299.997009 MHz, 249.997498 MHz, 199.998001 MHz, 142.855713 MHz, 99.999001 MHz, 49.999500 MHz)

    //val ranges = Ranges(45 to 90, 1 to 63, 1 to 63)
    val ranges = Ranges(60 to 90 by 30, 1 to 63, 1 to 63)

    def apply(target: HertzNumber): PLL = {
      val frequency = PLL.tune(33.333333 MHz, target, ranges).fout
      Log.info(f"[IOPLL] ${target} requested but ${frequency} selected.")
      return PLL("IOPLL", frequency)
    }

  }

}

