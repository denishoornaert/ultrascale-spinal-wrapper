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
import generic.interface.irq._


class KR260Config(
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
  withTo_PS_IRQ  : Boolean = false,
  withFrom_PS_IRQ: Boolean = false,
  // here
  val withIO_PMOD0: Boolean = false,
  val withIO_PMOD1: Boolean = false,
  val withIO_PMOD2: Boolean = false,
  val withIO_PMOD3: Boolean = false
  ) extends UltraScalePlusConfig(
    withLPD_HPM0    = withLPD_HPM0   ,
    withLPD_HP0     = withLPD_HP0    ,
    withFPD_HPM0    = withFPD_HPM0   ,
    withFPD_HPM1    = withFPD_HPM1   ,
    withFPD_HP0     = withFPD_HP0    ,
    withFPD_HP1     = withFPD_HP1    ,
    withFPD_HP2     = withFPD_HP2    ,
    withFPD_HP3     = withFPD_HP3    ,
    withFPD_HPC0    = withFPD_HPC0   ,
    withFPD_HPC1    = withFPD_HPC1   ,
    withFPD_ACP     = withFPD_ACP    ,
    withFPD_ACE     = withFPD_ACE    ,
    withDBG_CTI0    = withDBG_CTI0   ,
    withDBG_CTI1    = withDBG_CTI1   ,
    withDBG_CTI2    = withDBG_CTI2   ,
    withDBG_CTI3    = withDBG_CTI3   ,
    withDBG_CTO0    = withDBG_CTO0   ,
    withDBG_CTO1    = withDBG_CTO1   ,
    withDBG_CTO2    = withDBG_CTO2   ,
    withDBG_CTO3    = withDBG_CTO3   ,
    withTo_PS_IRQ   = withTo_PS_IRQ  ,
    withFrom_PS_IRQ = withFrom_PS_IRQ
  ) {
}

class KR260IO(config: KR260Config) extends UltraScalePlusIO(config) {
  val pmod0 = (config.withIO_PMOD0) generate (out(PMOD(PMOD0.names)))
  val pmod1 = (config.withIO_PMOD1) generate (out(PMOD(PMOD1.names)))
  val pmod2 = (config.withIO_PMOD2) generate (out(PMOD(PMOD2.names)))
  val pmod3 = (config.withIO_PMOD3) generate (out(PMOD(PMOD3.names)))
}


class KR260(
  frequency: HertzNumber = 99.999001 MHz,
  override val config   : KR260Config = new KR260Config()
) extends UltraScalePlus(
  frequency = frequency,
  config    = config
) {

  override val board = "kr260_som"
  override val version = "1.1"
  override val boardPart = "xck26-sfvc784-2LV-c"

  override def generate(): Unit = {
    if (this.config.withIO_PMOD0) {
      Constraints.add(io.pmod0.getXDC())
    }
    if (this.config.withIO_PMOD1) {
      Constraints.add(io.pmod1.getXDC())
    }
    if (this.config.withIO_PMOD2) {
      Constraints.add(io.pmod2.getXDC())
    }
    if (this.config.withIO_PMOD3) {
      Constraints.add(io.pmod3.getXDC())
    }
    super.generate()
  }

  override val io = new KR260IO(config)

}
