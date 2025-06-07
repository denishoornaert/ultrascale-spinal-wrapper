package example


import spinal.core._
import spinal.lib._


import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.scripts._
import ultrascaleplus.configport._
import zcu102._


case class ConfigPortTestZCU() extends ZCU102(
  frequency = 100 MHz,
  config    = new ZCU102Config(
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

object ConfigPortTestZCUVerilog extends App {
  Config.spinal.generateVerilog(ConfigPortTestZCU())
}

object ConfigPortTestZCUVhdl extends App {
  Config.spinal.generateVhdl(ConfigPortTestZCU())
}

