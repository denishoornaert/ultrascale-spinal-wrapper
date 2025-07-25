package example.kv260


import spinal.core._
import spinal.lib._


import kv260._
import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.scripts._
import ultrascaleplus.configport._


case class ConfigPortTest() extends KV260(
  config    = new KV260Config(
    withPL_CLK0  = 100 MHz,
    withLPD_HPM0 = true
  )
) {

  val config_port = ConfigPort(io.lpd.hpm0, io.lpd.hpm0.getPartialName())

  val clock_count = Reg(UInt(64 bits)) init(0)
  config_port.read(clock_count, io.lpd.hpm0.apertures(0).base)

  val soft_reset = Reg(UInt(8 bits)) init(1)
  config_port.readAndWrite(soft_reset, io.lpd.hpm0.apertures(0).base+(clock_count.getWidth/8))

  val enabled = Reg(UInt(8 bits)) init(0)
  config_port.readAndWrite(enabled, io.lpd.hpm0.apertures(0).base+(clock_count.getWidth/8)+1)

  
  when (soft_reset =/= 0) {
    clock_count := 0
  }
  .elsewhen (enabled =/= 0) {
    clock_count := clock_count+1
  }

  this.generate()
  KernelModule.addIO(io.lpd.hpm0)
  KernelModule.generate()
}

object ConfigPortTestVerilog extends App {
  Config.spinal.generateVerilog(ConfigPortTest())
}

object ConfigPortTestVhdl extends App {
  Config.spinal.generateVhdl(ConfigPortTest())
}

