package example


import spinal.core._
import spinal.lib._


import kv260._
import kv260.interface.axi._
import ultrascaleplus.scripts._
import ultrascaleplus.configport._
import ultrascaleplus.interface.axi.AbstractSecondaryAxi4


case class ConfigPortTest() extends KV260(
  frequency    = 100 MHz,
  withLPD_HPM0 = true
) {

  val port: AbstractSecondaryAxi4 = LPD_HPM0
  val config_port = new ConfigPort(io.lpd_hpm0, portName = io.lpd_hpm0.getPartialName(), LPD_HPM0.aperture.base)

  val clock_count = Reg(UInt(64 bits)) init(0)

  val soft_reset = Reg(UInt(8 bits)) init(1)

  val enabled = Reg(UInt(8 bits)) init(0)
  val test_byte_reg = Reg(UInt(8 bits)) init(0)

  val test_w_reg = Reg(UInt(64 bits)) init (0)
  val test_rw_reg = Reg(UInt(64 bits)) init (0)

  val test_unaligned_reg = Reg(UInt(16 bits)) init(0)

  val test_bundle = Reg(new Bundle {
    val alfa = UInt(16 bits)
    val beta = UInt(16 bits)
  })
  
  config_port.addAll[Data](
    Array((clock_count, R),
      (enabled, RW),
      (test_unaligned_reg, RW),
      (soft_reset, RW),
      (test_w_reg, W),
      (test_byte_reg, RW),
      (test_rw_reg, RW),
      (test_bundle, RW)))
  
  when (soft_reset =/= 0) {
    clock_count := 0
  }
  .elsewhen (enabled =/= 0) {
    clock_count := clock_count+1
  }

  this.generate()
  KernelModule.addIO(LPD_HPM0)
  KernelModule.generate()
}

object ConfigPortTestVerilog extends App {
  Config.spinal.generateVerilog(ConfigPortTest())
}

object ConfigPortTestVhdl extends App {
  Config.spinal.generateVhdl(ConfigPortTest())
}

