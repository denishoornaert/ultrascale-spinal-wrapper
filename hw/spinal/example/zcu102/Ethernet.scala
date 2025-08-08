package example.zcu102


import spinal.core._
import spinal.lib._


import ultrascaleplus.clock.PLClockingArea
import ultrascaleplus.ip.{Ethernet}


import zcu102._
import zcu102.io.ethernet._


case class EthernetBB() extends ZCU102(
  config    = new ZCU102Config(
    withPL_CLK0   = 332.00 MHz,
    with_GT0      =       true,
    withSI570_MGT = 156.25 MHz
  )
) {

  val plclk0 = new PLClockingArea(io.pl.clk0) {
    val ctrl = Ethernet(Ethernet0)
    ctrl.io.refclk <> io.user.si570.mgt
    ctrl.io.gt <> io.gt0
  }

  this.generate()
}


object EthernetBBVerilog extends App {
  val report = Config.spinal.generateVerilog(EthernetBB())
  report.mergeRTLSource("mergedRTL")
}

object EthernetBBVhdl extends App {
  Config.spinal.generateVhdl(EthernetBB())
}
