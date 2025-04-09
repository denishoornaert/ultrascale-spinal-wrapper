package example


import spinal.core._
import spinal.lib._


import kv260._
import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.scripts._


case class Shell() extends KV260(
  frequency = 332 MHz,
  config    = new KV260Config(
    withLPD_HPM0 = true,
    withFPD_HPM0 = true,
    withFPD_HPM1 = true,
    withFPD_HP0  = true,
    withFPD_HP1  = true,
    withFPD_HP2  = true,
    withFPD_HP3  = true,
    withFPD_HPC0 = true,
    withFPD_HPC1 = true,
    withIO_PMOD0 = true
  )
) {

  io.lpd.hpm0.setBlocked()
  io.fpd.hpm0.setBlocked()
  io.fpd.hpm1.setBlocked()

  io.fpd.hp0.setIdle()
  io.fpd.hp1.setIdle()
  io.fpd.hp2.setIdle()
  io.fpd.hp3.setIdle()
  io.fpd.hpc0.setIdle()
  io.fpd.hpc1.setIdle()

  io.pmod0.asOutput()
  io.pmod0.clearAll()

  // Create dummy register to force instantiation of cock and reset I/O
  val clock_count = Reg(UInt(64 bits)) init(0)
  clock_count := clock_count+1

  this.generate()
}


object ShellVerilog extends App {
  Config.spinal.generateVerilog(Shell())
}

object ShellVhdl extends App {
  Config.spinal.generateVhdl(Shell())
}
