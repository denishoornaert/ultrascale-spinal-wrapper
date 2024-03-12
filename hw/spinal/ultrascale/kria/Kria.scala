package ultrascale.kria

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import bus.amba4.ace._


case class Interfaces(
  withLPD_HPM0: Boolean = false,
  withFPD_HPM0: Boolean = false,
  withFPD_HPM1: Boolean = false,
  withFPD_HP0 : Boolean = false,
  withFPD_HP1 : Boolean = false,
  withFPD_HP2 : Boolean = false,
  withFPD_HP3 : Boolean = false,
  withFPD_ACE : Boolean = false
) extends Bundle {
  val lpd_hpm0 = (withLPD_HPM0) generate ( slave(Axi4(KriaPorts.LPD_HPM0_Config)))
  val fpd_hpm0 = (withFPD_HPM0) generate ( slave(Axi4(KriaPorts.FPD_HPM0_Config)))
  val fpd_hpm1 = (withFPD_HPM1) generate ( slave(Axi4(KriaPorts.FPD_HPM1_Config)))
  val fpd_hp0  = (withFPD_HP0 ) generate (master(Axi4(KriaPorts.FPD_HP0_Config )))
  val fpd_hp1  = (withFPD_HP1 ) generate (master(Axi4(KriaPorts.FPD_HP1_Config )))
  val fpd_hp2  = (withFPD_HP2 ) generate (master(Axi4(KriaPorts.FPD_HP2_Config )))
  val fpd_hp3  = (withFPD_HP3 ) generate (master(Axi4(KriaPorts.FPD_HP3_Config )))
  val fpd_ace  = (withFPD_ACE ) generate ( slave(Axi4(KriaPorts.FPD_ACE_Config )))
}


case class Kria(
    withLPD_HPM0: Boolean = false,
    withFPD_HPM0: Boolean = false,
    withFPD_HPM1: Boolean = false,
    withFPD_HP0 : Boolean = false,
    withFPD_HP1 : Boolean = false,
    withFPD_HP2 : Boolean = false,
    withFPD_HP3 : Boolean = false,
    withFPD_ACE : Boolean = false
  ) extends Component {

  val io = new Interfaces(withLPD_HPM0, withFPD_HPM0, withFPD_HPM1, withFPD_HP0, withFPD_HP1, withFPD_HP2, withFPD_HP3, withFPD_ACE)
  
  if (withLPD_HPM0)
    KriaPorts.setAxi4InterfaceAttributes(io.lpd_hpm0)
  if (withFPD_HPM0)
    KriaPorts.setAxi4InterfaceAttributes(io.fpd_hpm0)
  if (withFPD_HPM1)
    KriaPorts.setAxi4InterfaceAttributes(io.fpd_hpm1)
  if (withFPD_HP0)
    KriaPorts.setAxi4InterfaceAttributes(io.fpd_hp0)
  if (withFPD_HP1)
    KriaPorts.setAxi4InterfaceAttributes(io.fpd_hp1)
  if (withFPD_HP2)
    KriaPorts.setAxi4InterfaceAttributes(io.fpd_hp2)
  if (withFPD_HP3)
    KriaPorts.setAxi4InterfaceAttributes(io.fpd_hp3)
//  if (withFPD_ACE)
//    KriaPorts.setAceInterfaceAttributes(io.fpd_ace)

  io.fpd_ace <> io.fpd_hp0
}

object KriaVerilog extends App {
  Config.spinal.generateVerilog(Kria(withFPD_ACE=true, withFPD_HP0=true))
}

object KriaVhdl extends App {
  Config.spinal.generateVhdl(Kria())
}
