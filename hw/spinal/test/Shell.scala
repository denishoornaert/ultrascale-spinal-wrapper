package test


import spinal.core._
import spinal.lib._


import kv260._
import kv260.interface.axi._
import ultrascaleplus.scripts._


case class Shell() extends KV260(
  frequency    = 332 MHz,
  withLPD_HPM0 = true,
  withFPD_HPM0 = true,
  withFPD_HPM1 = true,
  withFPD_HP0  = true,
  withFPD_HP1  = true,
  withFPD_HP2  = true,
  withFPD_HP3  = true,
  withFPD_HPC0 = true,
  withFPD_HPC1 = true
) {

  io.lpd_hpm0.setBlocked()
  io.fpd_hpm0.setBlocked()
  io.fpd_hpm1.setBlocked()

  io.fpd_hp0.setIdle()
  io.fpd_hp1.setIdle()
  io.fpd_hp2.setIdle()
  io.fpd_hp3.setIdle()
  io.fpd_hpc0.setIdle()
  io.fpd_hpc1.setIdle()

  // Create dummy register to force instantiation of cock and reset I/O
  val clock_count = Reg(UInt(64 bits)) init(0)
  clock_count := clock_count+1

}


object ShellVerilog extends App {
  Config.spinal.generateVerilog(Shell())
}

object ShellVhdl extends App {
  Config.spinal.generateVhdl(Shell())
}
