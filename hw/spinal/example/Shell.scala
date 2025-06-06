package example


import spinal.core._
import spinal.lib._


import kv260._


case class Shell() extends KV260(
  frequency = 332 MHz,
  config    = new KV260Config(
    withLPD_HPM0   = true,
    withLPD_HP0    = true,
    withFPD_HPM0   = true,
    withFPD_HPM1   = true,
    withFPD_HP0    = true,
    withFPD_HP1    = true,
    withFPD_HP2    = true,
    withFPD_HP3    = true,
    withFPD_HPC0   = true,
    withFPD_HPC1   = true,
    withFPD_ACP    = true,
    withIO_PMOD0   = true,
    withDBG_CTI0   = true,
    withDBG_CTI1   = true,
    withDBG_CTI2   = true,
    withDBG_CTI3   = true,
    withDBG_CTO0   = true,
    withDBG_CTO1   = true,
    withDBG_CTO2   = true,
    withDBG_CTO3   = true,
    withPL_PS_IRQ0 =    8,
    withPL_PS_IRQ1 =    8,
    withTRACE      = true
  )
) {

  this.stub()

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
