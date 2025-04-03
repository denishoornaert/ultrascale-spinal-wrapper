package ultrascaleplus.parameters

import spinal.core._
import spinal.lib._

import ultrascaleplus.utils.Aperture

object AddressMap {

  // PS to PL
  val LPD_HPM0 = new Aperture("LPD_HPM0", BigInt("0080000000", 16), 512 MiB)
  val FPD_HPM0 = new Aperture("FPD_HPM0", BigInt("1000000000", 16), 224 GiB)
  val FPD_HPM1 = new Aperture("FPD_HPM1", BigInt("4800000000", 16), 224 GiB)

  // PS DRAM
  val LOW_DDR  = new Aperture("LOW_DDR", BigInt("0000000000", 16),   2 GiB)
  val HIGH_DDR = new Aperture("HIGH_DDR", BigInt("0800000000", 16),   2 GiB)

  // Non-volatile memory
  val QSPI     = new Aperture("QSPI", BigInt("00C0000000", 16),  16 MiB)

  // On-chip Memory
  val OCM      = new Aperture("OCM", BigInt("00FFFC0000", 16), 256 KiB)

}
