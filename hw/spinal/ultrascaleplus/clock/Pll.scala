package ultrascaleplus.clock.pll


import scala.math.{floor, ceil, round, pow}


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils.Log


case class PLL(val name: String, target: HertzNumber, mapping: Map[Int, Seq[HertzNumber]]) {

  private var multiplier: Int = 0

  var frequency : HertzNumber = 0 MHz

  def enabled: Boolean = {
    return this.frequency > HertzNumber(0)
  }

  private def round(target: HertzNumber, frequencies: Seq[HertzNumber]): HertzNumber = {
    val differences = frequencies.map(x => (x-target).toDouble).map(x => if (x > 0) -1.0/0 else x)
    val index       = differences.indexOf(differences.max)
    return frequencies(index)
  }

  // If target is 0 MHz, then disabled
  if (target != (0 MHz)) {
    // For all multiplier, pick one giving best target frequency fit
    for (frequencies <- mapping) {
      val estimation = round(target, frequencies._2)
      if (target-estimation < target-frequency) {
        multiplier = frequencies._1
        frequency  = estimation
      }
    }
    Log.info(f"[${name}] ${target} requested but ${frequency} selected.")
  }

}


object PLL {

  case class Config(m: Int, d0: Int, d1: Int, d2: Int, fvco: HertzNumber) {

    val fout: HertzNumber = fvco/(2*d0*d1*d2)

  }

  case class Ranges(m: Range, d0: Range, d1: Range) {}

  /**
   * Branch and bound. Looking for best match.
   *
   * Mix of logic found from xilinx sources:
   *  - https://github.com/Xilinx/linux-xlnx/blob/786191c466220b9fe979f81053b82cb095d18225/drivers/clk/zynqmp/pll.c#L101
   *  - https://github.com/Xilinx/embeddedsw/blob/45a18907084e77bb3a450a035d280130d7ff6e26/lib/sw_services/xilpm/src/zynqmp/client/common/pm_clock.c#L1107
   */
  def tune(fin: HertzNumber, target: HertzNumber, ranges: Ranges): PLL.Config = {
    val divRange = (0 to 63)
    var config: Option[PLL.Config] = None
    val m = 90
    val fvco = fin.toDouble*m
    val div = ceil(fvco/(2*target.toDouble)).toInt
    // Breakdown overall divisor in two divisors
    assert(
      assertion = div < pow(divRange.max, 2),
      message   = f"Rate (${fvco}) must be smaller than ${pow(2, divRange.max)}!"
    )
    // Fits in first divisor -> straight forward mapping
    if (divRange contains div) {
      config = Some(PLL.Config(m, div.toInt, 1, 1, HertzNumber(fvco)))
    }
    // Must be decomposed
    else {
      val decomposition = (2 to ceil(divRange.max/2).toInt).filter(d0 => fvco%d0 == 0).map(d0 => (d0, floor(fvco.toLong/d0).toInt))
      assert(
        assertion = decomposition(0)._2 != 0,
        message   = "Divisor is a prime number bigger than the encoding width."
      )
      config = Some(PLL.Config(m, decomposition(0)._1, decomposition(0)._2, 1, HertzNumber(fvco)))
    }
    assert(
      assertion = config.isDefined,
      message   = f"No PLL configuration could not be found for target ${target}."
    )
    println(f"Config(${config.get.m}, ${config.get.d0}, ${config.get.d1}, ${config.get.d2}, ${config.get.fvco}, ${config.get.fout})")
    return config.get
  }

  object IO {

    val mapping: Map[Int, Seq[HertzNumber]] = Map(
      60 -> Seq(333.329987 MHz, 249.997498 MHz, 199.998001 MHz, 142.855713 MHz, 99.999001 MHz, 71.427856 MHz, 49.999500 MHz),
      90 -> Seq(299.997009 MHz,  74.999252 MHz)
    )

    def apply(target: HertzNumber): PLL = {
      return new PLL("IOPLL", target, mapping)
    }

  }

  object R {

    val mapping: Map[Int, Seq[HertzNumber]] = Map(
      63 -> Seq(74.999252 MHz)
    )

    def apply(target: HertzNumber): PLL = {
      return new PLL("RPLL", target, mapping)
    }

  }
}

