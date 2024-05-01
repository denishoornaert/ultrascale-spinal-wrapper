package kria

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

//import bus.amba4.ace._
import interface.debug._
import interface.io._
import scripts._


case class Interfaces(
  withLPD_HPM0               : Boolean = false,
  withFPD_HPM0               : Boolean = false,
  withFPD_HPM1               : Boolean = false,
  withFPD_HP0                : Boolean = false,
  withFPD_HP1                : Boolean = false,
  withFPD_HP2                : Boolean = false,
  withFPD_HP3                : Boolean = false,
  withFPD_ACE                : Boolean = false,
  withCross_Trigger_Input    : Int     = 0,
  withCross_Trigger_Output   : Int     = 0,
  withPMOD                   : Int     = 0
) extends Bundle {
  val lpd_hpm0 = (withLPD_HPM0                  ) generate (     slave(Axi4(KriaPorts.LPD_HPM0_Config))                             )
  val fpd_hpm0 = (withFPD_HPM0                  ) generate (     slave(Axi4(KriaPorts.FPD_HPM0_Config))                             )
  val fpd_hpm1 = (withFPD_HPM1                  ) generate (     slave(Axi4(KriaPorts.FPD_HPM1_Config))                             )
  val fpd_hp0  = (withFPD_HP0                   ) generate (    master(Axi4(KriaPorts.FPD_HP0_Config ))                             )
  val fpd_hp1  = (withFPD_HP1                   ) generate (    master(Axi4(KriaPorts.FPD_HP1_Config ))                             )
  val fpd_hp2  = (withFPD_HP2                   ) generate (    master(Axi4(KriaPorts.FPD_HP2_Config ))                             )
  val fpd_hp3  = (withFPD_HP3                   ) generate (    master(Axi4(KriaPorts.FPD_HP3_Config ))                             )
//  val fpd_ace  = (withFPD_ACE                   ) generate (     slave(Axi4(KriaPorts.FPD_ACE_Config ))                             )
  val dbg_cti  = (withCross_Trigger_Input > 0   ) generate (Vec( slave(CrossTrigger()                 ), withCross_Trigger_Input)   )
  val dbg_cto  = (withCross_Trigger_Output > 0  ) generate (Vec(master(CrossTrigger()                 ), withCross_Trigger_Output)  )
  val pmod     = (withPMOD > 0                  ) generate (       out(PMOD(withPMOD, "pmod")         )                             )
}


case class Kria(
  withLPD_HPM0               : Boolean = false,
  withFPD_HPM0               : Boolean = false,
  withFPD_HPM1               : Boolean = false,
  withFPD_HP0                : Boolean = false,
  withFPD_HP1                : Boolean = false,
  withFPD_HP2                : Boolean = false,
  withFPD_HP3                : Boolean = false,
  withFPD_ACE                : Boolean = false,
  withCross_Trigger_Input    : Int     = 0,
  withCross_Trigger_Output   : Int     = 0,
  withPMOD                   : Int     = 0
) extends Component {

  val io = new Interfaces(withLPD_HPM0, withFPD_HPM0, withFPD_HPM1, withFPD_HP0, withFPD_HP1, withFPD_HP2, withFPD_HP3, withFPD_ACE, withCross_Trigger_Input, withCross_Trigger_Output, withPMOD)
  
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
  if (withCross_Trigger_Input > 0)
    for (i <- 0 until withCross_Trigger_Input)
      KriaPorts.setCrossTriggerInterfaceAttributes(io.dbg_cti(i))
  if (withCross_Trigger_Output > 0)
    for (i <- 0 until withCross_Trigger_Output)
      KriaPorts.setCrossTriggerInterfaceAttributes(io.dbg_cto(i))

//  io.fpd_ace <> io.fpd_hp0

  io.dbg_cti(0) <> io.dbg_cto(0)
  io.dbg_cti(1) <> io.dbg_cto(1)

  io.pmod.makeInput(5)
  io.pmod.setAllInputs()

  BlockDiagram.draw(this)
}

object KriaVerilog extends App {
  //Config.spinal.generateVerilog(Kria(withFPD_ACE=true, withFPD_HP0=true))
  Config.spinal.generateVerilog(Kria(withCross_Trigger_Input=2, withCross_Trigger_Output=2, withPMOD=8))
}

object KriaVhdl extends App {
  Config.spinal.generateVhdl(Kria())
}
