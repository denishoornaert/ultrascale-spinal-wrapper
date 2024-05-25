package ultrascaleplus.parameters

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.SizeMapping

object AddressMap {

  // PS to PL
  val LPD_HPM0 = SizeMapping(BigInt("0080000000", 16), 512 MiB)
  val FPD_HPM0 = SizeMapping(BigInt("1000000000", 16), 224 GiB)
  val FPD_HPM1 = SizeMapping(BigInt("4800000000", 16), 224 GiB)

  // PS DRAM
  val LOW_DDR  = SizeMapping(BigInt("1000000000", 16),   2 GiB)
  val HIGH_DDR = SizeMapping(BigInt("8000000000", 16),   2 GiB)

  // On-chip Memory
  val OCM      = SizeMapping(BigInt("00FFFC0000", 16), 256 KiB)

}
