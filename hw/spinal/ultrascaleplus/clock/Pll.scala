package ultrascaleplus.clock.pll


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils.Log


case class PLL(name: String, frequency: HertzNumber) {}


object PLL {

  object IO {

    val frequencies = Seq(333.329987 MHz, 299.997009 MHz, 249.997498 MHz, 199.998001 MHz, 142.855713 MHz, 99.999001 MHz, 49.999500 MHz)

    def apply(target: HertzNumber): PLL = {
      val differences = frequencies.map(x => (x-target).toDouble).map(x => if (x > 0) -1.0/0 else x)
      val index       = differences.indexOf(differences.max)
      Log.info(f"[IOPLL] ${target} requested but ${frequencies(index)} selected.")
      return PLL("IOPLL", frequencies(index))
    }

  }

}

