package example.zcu102


import spinal.core._
import spinal.lib._


import zcu102.io.ethernet._
import ultrascaleplus.ip.{Ethernet}


import zcu102._


case class EthernetBB() extends ZCU102(
  frequency = 332 MHz,
  config    = new ZCU102Config(
    with_GT0 = true
  )
) {

  val ctrl = Ethernet(Ethernet0)
  ctrl.io.gt <> io.gt0

  this.generate()
}


object EthernetBBVerilog extends App {
  val report = Config.spinal.generateVerilog(EthernetBB())
  report.mergeRTLSource("mergedRTL")
}

object EthernetBBVhdl extends App {
  Config.spinal.generateVhdl(EthernetBB())
}
