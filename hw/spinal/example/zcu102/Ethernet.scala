package example.zcu102


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi.Axi4
import spinal.lib.bus.amba4.axilite.AxiLite4


import ultrascaleplus.Config
import ultrascaleplus.clock.PLClockingArea
import ultrascaleplus.clock.pll._
import ultrascaleplus.ip.Ethernet
import ultrascaleplus.bus.amba.axi4.Axi4toAxiLite4


import zcu102._
import zcu102.io.ethernet._


case class EthernetBB() extends ZCU102(
  config    = new ZCU102Config(
    withPL_CLK0   = PLL.R(75.00 MHz),
    withLPD_HPM0  =             true,
    with_GT0      =             true,
    withSI570_MGT =       156.25 MHz
  )
) {

  io.lpd.hpm0.associate(io.pl.clk0)

  val plclk0 = new PLClockingArea(io.pl.clk0) {
    val ctrl = Ethernet(Ethernet0)
    ctrl.io.axi <> Axi4toAxiLite4(io.lpd.hpm0, ctrl.io.axi.config)
    ctrl.io.refclk <> io.user.si570.mgt
    ctrl.io.gt <> io.gt0
    ctrl.io.tx.axis.setIdle()
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
