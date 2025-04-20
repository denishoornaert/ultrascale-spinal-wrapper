package zcu102


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus._
import ultrascaleplus.signal.crosstrigger._
import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.io.pmod._
import ultrascaleplus.scripts._
import zcu102.io.pmod._


class ZCU102Config(
  withLPD_HPM0   : Boolean = false,
  withLPD_HP0    : Boolean = false,
  withFPD_HPM0   : Boolean = false,
  withFPD_HPM1   : Boolean = false,
  withFPD_HP0    : Boolean = false,
  withFPD_HP1    : Boolean = false,
  withFPD_HP2    : Boolean = false,
  withFPD_HP3    : Boolean = false,
  withFPD_HPC0   : Boolean = false,
  withFPD_HPC1   : Boolean = false,
  withFPD_ACP    : Boolean = false,
  withFPD_ACE    : Boolean = false,
  withDBG_CTI0   : Boolean = false,
  withDBG_CTI1   : Boolean = false,
  withDBG_CTI2   : Boolean = false,
  withDBG_CTI3   : Boolean = false,
  withDBG_CTO0   : Boolean = false,
  withDBG_CTO1   : Boolean = false,
  withDBG_CTO2   : Boolean = false,
  withDBG_CTO3   : Boolean = false,
  withPL_PS_IRQ0 : Int     =     0,
  withPL_PS_IRQ1 : Int     =     0
  ) extends UltraScalePlusConfig(
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
    withPL_PS_IRQ1 = withPL_PS_IRQ1
  ) {
}

class ZCU102IO(config: ZCU102Config) extends UltraScalePlusIO(config) {
}


class ZCU102(
  frequency: HertzNumber = 99.999001 MHz,
  override val config   : ZCU102Config = new ZCU102Config()
) extends UltraScalePlus(
  frequency = frequency,
  config    = config
) {

  override val board = "zcu102"
  override val version = "3.4"
  override val boardPart = "xczu9eg-ffvb1156-2-e"

  override val io = new ZCU102IO(config)

}
