package example.kv260


import spinal.core._
import spinal.lib._


import kv260._
import ultrascaleplus.bus.amba.ace._
import ultrascaleplus.scripts._
import ultrascaleplus.configport._
import ultrascaleplus.clock._
import ultrascaleplus.clock.pll._


case class AceTest() extends KV260(
  config = new KV260Config(
    withPL_CLK0 = PLL.IO(100 MHz),
    withFPD_ACE = true
  )
) {

  io.fpd.ace.associate(io.pl.clk0)
  io.fpd.ace.ac.setBlocked()
  io.fpd.ace.cr.setIdle()
  io.fpd.ace.cd.setIdle()
  io.fpd.ace.ar.setIdle()
  io.fpd.ace.r.setBlocked()
  io.fpd.ace.aw.setIdle()
  io.fpd.ace.w.setIdle()
  io.fpd.ace.b.setBlocked()

  this.generate()
}

object AceTestVerilog extends App {
  Config.spinal.generateVerilog(AceTest())
}

object AceTestVhdl extends App {
  Config.spinal.generateVerilog(AceTest())
}
