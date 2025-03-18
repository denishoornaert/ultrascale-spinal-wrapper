package kv260


import Console.{RESET, YELLOW}

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

//import bus.amba4.ace._
import ultrascaleplus.configport._
import ultrascaleplus.interface.crosstrigger._
import ultrascaleplus.interface.axi._
import ultrascaleplus.interface.pmod._
import ultrascaleplus.scripts._

import interface.axi._
import interface.pmod._
import generic.interface.pmod._
import generic.interface.irq._
import interface.crosstrigger._


case class Interfaces(
  withLPD_HPM0               : Boolean = false,
  withFPD_HPM0               : Boolean = false,
  withFPD_HPM1               : Boolean = false,
  withFPD_HP0                : Boolean = false,
  withFPD_HP1                : Boolean = false,
  withFPD_HP2                : Boolean = false,
  withFPD_HP3                : Boolean = false,
  withFPD_HPC0               : Boolean = false,
  withFPD_HPC1               : Boolean = false,
  withFPD_ACE                : Boolean = false,
  withDBG_CTI0               : Boolean = false,
  withDBG_CTI1               : Boolean = false,
  withDBG_CTI2               : Boolean = false,
  withDBG_CTI3               : Boolean = false,
  withDBG_CTO0               : Boolean = false,
  withDBG_CTO1               : Boolean = false,
  withDBG_CTO2               : Boolean = false,
  withDBG_CTO3               : Boolean = false,
  withIO_PMOD0               : Boolean = false,
  withTo_PS_IRQ              : Boolean = false,
  withFrom_PS_IRQ            : Boolean = false
) extends Bundle {
  val lpd_hpm0 = (withLPD_HPM0   ) generate ( slave(Axi4(LPD_HPM0.config)))
  val fpd_hpm0 = (withFPD_HPM0   ) generate ( slave(Axi4(FPD_HPM0.config)))
  val fpd_hpm1 = (withFPD_HPM1   ) generate ( slave(Axi4(FPD_HPM1.config)))
  val fpd_hp0  = (withFPD_HP0    ) generate (master(Axi4(FPD_HP0.config )))
  val fpd_hp1  = (withFPD_HP1    ) generate (master(Axi4(FPD_HP1.config )))
  val fpd_hp2  = (withFPD_HP2    ) generate (master(Axi4(FPD_HP2.config )))
  val fpd_hp3  = (withFPD_HP3    ) generate (master(Axi4(FPD_HP3.config )))
  val fpd_hpc0 = (withFPD_HPC0   ) generate (master(Axi4(FPD_HPC0.config)))
  val fpd_hpc1 = (withFPD_HPC1   ) generate (master(Axi4(FPD_HPC1.config)))
//  val fpd_ace  = (withFPD_ACE                  ) generate (     slave(Axi4(KriaPorts.FPD_ACE_Config ))                             )
  val dbg_cti0 = (withDBG_CTI0   ) generate (DBG_CTI0.port)
  val dbg_cti1 = (withDBG_CTI1   ) generate (DBG_CTI1.port)
  val dbg_cti2 = (withDBG_CTI2   ) generate (DBG_CTI2.port)
  val dbg_cti3 = (withDBG_CTI3   ) generate (DBG_CTI3.port)
  val dbg_cto0 = (withDBG_CTO0   ) generate (DBG_CTO0.port)
  val dbg_cto1 = (withDBG_CTO1   ) generate (DBG_CTO1.port)
  val dbg_cto2 = (withDBG_CTO2   ) generate (DBG_CTO2.port)
  val dbg_cto3 = (withDBG_CTO3   ) generate (DBG_CTO3.port)
  val pmod0    = (withIO_PMOD0   ) generate (master(PMOD()))
  val pl_to_ps = (withTo_PS_IRQ  ) generate (master(IRQ()))
  val ps_to_pl = (withFrom_PS_IRQ) generate ( slave(IRQ()))
}


class KV260(
  frequency       : HertzNumber = 99.999001 MHz,
  withLPD_HPM0    : Boolean     = false,
  withFPD_HPM0    : Boolean     = false,
  withFPD_HPM1    : Boolean     = false,
  withFPD_HP0     : Boolean     = false,
  withFPD_HP1     : Boolean     = false,
  withFPD_HP2     : Boolean     = false,
  withFPD_HP3     : Boolean     = false,
  withFPD_HPC0    : Boolean     = false,
  withFPD_HPC1    : Boolean     = false,
  withFPD_ACE     : Boolean     = false,
  withDBG_CTI0    : Boolean     = false,
  withDBG_CTI1    : Boolean     = false,
  withDBG_CTI2    : Boolean     = false,
  withDBG_CTI3    : Boolean     = false,
  withDBG_CTO0    : Boolean     = false,
  withDBG_CTO1    : Boolean     = false,
  withDBG_CTO2    : Boolean     = false,
  withDBG_CTO3    : Boolean     = false,
  withIO_PMOD0    : Boolean     = false,
  withTo_PS_IRQ   : Boolean     = false,
  withFrom_PS_IRQ : Boolean     = false
) extends Component {

  def generate(): Unit = {
    if (withIO_PMOD0) {
      Constraints.add(PMOD0.getConstraints())
    }
    TCLFactory.generate()
  }

  // List of IOPLL clocks available for KV260
  val availableFrequencies = Seq(333.329987 MHz, 299.997009 MHz, 249.997498 MHz, 199.998001 MHz, 142.855713 MHz, 99.999001 MHz, 49.999500 MHz)
  // Looks for the available clock that is the closest to the requested one but not higher
  val differences          = availableFrequencies.map(x => (x-frequency).toDouble).map(x => if (x > 0) -1.0/0 else x)
  val index                = differences.indexOf(differences.max)
  val actualFrequency      = availableFrequencies(index)
  println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Frequency requested ${frequency}, but actual frequency is ${actualFrequency}.${RESET}")

  // Get name of the class (should be the off spring).
  TCLFactory(this)
  Constraints(this.getClass.getSimpleName)

  val io = new Interfaces(
    withLPD_HPM0,
    withFPD_HPM0,
    withFPD_HPM1,
    withFPD_HP0,
    withFPD_HP1,
    withFPD_HP2,
    withFPD_HP3,
    withFPD_HPC0,
    withFPD_HPC1,
    withFPD_ACE,
    withDBG_CTI0,
    withDBG_CTI1,
    withDBG_CTI2,
    withDBG_CTI3,
    withDBG_CTO0,
    withDBG_CTO1,
    withDBG_CTO2,
    withDBG_CTO3,
    withIO_PMOD0,
    withTo_PS_IRQ,
    withFrom_PS_IRQ
  )
  
  if (withLPD_HPM0) {
    LPD_HPM0.init(io.lpd_hpm0)
  }
  
  if (withFPD_HPM0) {
    FPD_HPM0.init(io.fpd_hpm0)
  }
  
  if (withFPD_HPM1) {
    FPD_HPM1.init(io.fpd_hpm1)
  }
  
  if (withFPD_HP0) {
    FPD_HP0.init(io.fpd_hp0)
  }
  
  if (withFPD_HP1) {
    FPD_HP1.init(io.fpd_hp1)
  }
  
  if (withFPD_HP2) {
    FPD_HP2.init(io.fpd_hp2)
  }
  
  if (withFPD_HP3) {
    FPD_HP3.init(io.fpd_hp3)
  }

  if (withFPD_HPC0) {
    FPD_HPC0.init(io.fpd_hpc0)
  }
  
  if (withFPD_HPC1) {
    FPD_HPC1.init(io.fpd_hpc1)
  }

  if (withIO_PMOD0) {
    PMOD0.init(io.pmod0)
  }
  
  if (withTo_PS_IRQ) {
    io.pl_to_ps.clearAllOutputs()
  }

  // Generate dummy register to infer a clock and reset IO
  val dummyRegForClockInUltraScalePlusPlatforms = Reg(Bool())
  dummyRegForClockInUltraScalePlusPlatforms := !dummyRegForClockInUltraScalePlusPlatforms

}
