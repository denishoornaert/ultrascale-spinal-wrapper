package bus.amba4.ace

import spinal.core._
import spinal.lib._


case class AceAC(config: AceConfig) extends Bundle {
  val addr  = UInt(config.addressWidth bits)
  val snoop = UInt(4 bits)
  val prot  = if(config.useProt) Bits(3 bits) else null
}

case class AceCR(config: AceConfig) extends Bundle {
  val resp = UInt(5 bits)
}

case class AceCD(config: AceConfig) extends Bundle {
  val data = Bits(config.dataWidth bits)
  val last = Bool()
}
