package kr260


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus._
import ultrascaleplus.signal.crosstrigger._
import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.io.pmod._
import ultrascaleplus.scripts._
import kr260.io.pmod._


class KR260Config(
  withPL_CLK0    : HertzNumber = 100 MHz,
  withPL_CLK1    : HertzNumber =   0 MHz,
  withPL_CLK2    : HertzNumber =   0 MHz,
  withPL_CLK3    : HertzNumber =   0 MHz,
  withLPD_HPM0   : Boolean     = false,
  withLPD_HP0    : Boolean     = false,
  withFPD_HPM0   : Boolean     = false,
  withFPD_HPM1   : Boolean     = false,
  withFPD_HP0    : Boolean     = false,
  withFPD_HP1    : Boolean     = false,
  withFPD_HP2    : Boolean     = false,
  withFPD_HP3    : Boolean     = false,
  withFPD_HPC0   : Boolean     = false,
  withFPD_HPC1   : Boolean     = false,
  withFPD_ACP    : Boolean     = false,
  withFPD_ACE    : Boolean     = false,
  withDBG_CTI0   : Boolean     = false,
  withDBG_CTI1   : Boolean     = false,
  withDBG_CTI2   : Boolean     = false,
  withDBG_CTI3   : Boolean     = false,
  withDBG_CTO0   : Boolean     = false,
  withDBG_CTO1   : Boolean     = false,
  withDBG_CTO2   : Boolean     = false,
  withDBG_CTO3   : Boolean     = false,
  withPL_PS_IRQ0 : Int         =     0,
  withPL_PS_IRQ1 : Int         =     0,
  withTRACE      : Boolean     = false,
  // here
  val withIO_PMOD0: Boolean    = false,
  val withIO_PMOD1: Boolean    = false,
  val withIO_PMOD2: Boolean    = false,
  val withIO_PMOD3: Boolean    = false
  ) extends UltraScalePlusConfig(
    withPL_CLK0    = withPL_CLK0   ,
    withPL_CLK1    = withPL_CLK1   ,
    withPL_CLK2    = withPL_CLK2   ,
    withPL_CLK3    = withPL_CLK3   ,
    withLPD_HPM0   = withLPD_HPM0  ,
    withLPD_HP0    = withLPD_HP0   ,
    withFPD_HPM0   = withFPD_HPM0  ,
    withFPD_HPM1   = withFPD_HPM1  ,
    withFPD_HP0    = withFPD_HP0   ,
    withFPD_HP1    = withFPD_HP1   ,
    withFPD_HP2    = withFPD_HP2   ,
    withFPD_HP3    = withFPD_HP3   ,
    withFPD_HPC0   = withFPD_HPC0  ,
    withFPD_HPC1   = withFPD_HPC1  ,
    withFPD_ACP    = withFPD_ACP   ,
    withFPD_ACE    = withFPD_ACE   ,
    withDBG_CTI0   = withDBG_CTI0  ,
    withDBG_CTI1   = withDBG_CTI1  ,
    withDBG_CTI2   = withDBG_CTI2  ,
    withDBG_CTI3   = withDBG_CTI3  ,
    withDBG_CTO0   = withDBG_CTO0  ,
    withDBG_CTO1   = withDBG_CTO1  ,
    withDBG_CTO2   = withDBG_CTO2  ,
    withDBG_CTO3   = withDBG_CTO3  ,
    withPL_PS_IRQ0 = withPL_PS_IRQ0,
    withPL_PS_IRQ1 = withPL_PS_IRQ1,
    withTRACE      = withTRACE
  ) {
}

class KR260IO(config: KR260Config) extends UltraScalePlusIO(config) {
  val pmod0 = (config.withIO_PMOD0) generate (out(PMOD(PMOD0.names)))
  val pmod1 = (config.withIO_PMOD1) generate (out(PMOD(PMOD1.names)))
  val pmod2 = (config.withIO_PMOD2) generate (out(PMOD(PMOD2.names)))
  val pmod3 = (config.withIO_PMOD3) generate (out(PMOD(PMOD3.names)))
}


class KR260(
  override val config   : KR260Config = new KR260Config(
    withPL_CLK0 = 100 MHz
  )
) extends UltraScalePlus(
  config    = config
) {

  override val board = "kr260_som"
  override val version = "1.1"
  override val boardPart = "xck26-sfvc784-2LV-c"

  override val io = new KR260IO(config)

  override val plclk0 = (config.withPL_CLK0 > (0 MHz)) generate new ClockingArea(io.pl.clk0.domain)
  override val plclk1 = (config.withPL_CLK1 > (0 MHz)) generate new ClockingArea(io.pl.clk1.domain)
  override val plclk2 = (config.withPL_CLK2 > (0 MHz)) generate new ClockingArea(io.pl.clk2.domain)
  override val plclk3 = (config.withPL_CLK3 > (0 MHz)) generate new ClockingArea(io.pl.clk3.domain)
}
