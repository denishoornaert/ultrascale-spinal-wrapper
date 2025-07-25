package example.kv260


import spinal.core._
import spinal.lib._


import kv260._


case class Shell() extends KV260(
  config    = new KV260Config(
    withPL_CLK0    = 332 MHz,
    withPL_CLK1    = 332 MHz,
    withPL_CLK2    = 332 MHz,
    withPL_CLK3    = 332 MHz,
    withLPD_HPM0   =    true,
    withLPD_HP0    =    true,
    withFPD_HPM0   =    true,
    withFPD_HPM1   =    true,
    withFPD_HP0    =    true,
    withFPD_HP1    =    true,
    withFPD_HP2    =    true,
    withFPD_HP3    =    true,
    withFPD_HPC0   =    true,
    withFPD_HPC1   =    true,
    withFPD_ACP    =    true,
    withIO_PMOD0   =    true,
    withDBG_CTI0   =    true,
    withDBG_CTI1   =    true,
    withDBG_CTI2   =    true,
    withDBG_CTI3   =    true,
    withDBG_CTO0   =    true,
    withDBG_CTO1   =    true,
    withDBG_CTO2   =    true,
    withDBG_CTO3   =    true,
    withPL_PS_IRQ0 =       8,
    withPL_PS_IRQ1 =       8,
    withTRACE      =    true
  )
) {

  this.stub()

  io.lpd.hp0.associate(io.pl.clk0)
  io.lpd.hpm0.associate(io.pl.clk0)
  io.fpd.hpm0.associate(io.pl.clk0)
  io.fpd.hpm1.associate(io.pl.clk0)
  io.fpd.hp0.associate(io.pl.clk0)
  io.fpd.hp1.associate(io.pl.clk0)
  io.fpd.hp2.associate(io.pl.clk0)
  io.fpd.hp3.associate(io.pl.clk0)
  io.fpd.hpc0.associate(io.pl.clk0)
  io.fpd.hpc1.associate(io.pl.clk0)
  io.fpd.acp.associate(io.pl.clk0)

  plclk0.rework{
    // Create dummy register to force instantiation of cock and reset I/O
    val clock_count = Reg(UInt(64 bits)) init(0)
    clock_count := clock_count+1
  }

  plclk1.rework{
    // Create dummy register to force instantiation of cock and reset I/O
    val clock_count = Reg(UInt(64 bits)) init(0)
    clock_count := clock_count+1
  }

  plclk2.rework{
    // Create dummy register to force instantiation of cock and reset I/O
    val clock_count = Reg(UInt(64 bits)) init(0)
    clock_count := clock_count+1
  }

  plclk3.rework{
    // Create dummy register to force instantiation of cock and reset I/O
    val clock_count = Reg(UInt(64 bits)) init(0)
    clock_count := clock_count+1
  }

  this.generate()
}


object ShellVerilog extends App {
  Config.spinal.generateVerilog(Shell())
}

object ShellVhdl extends App {
  Config.spinal.generateVhdl(Shell())
}
