package kv260

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
import interface.crosstrigger._


case class Interfaces(
  withLPD_HPM0               : Boolean = false,
  withFPD_HPM0               : Boolean = false,
  withFPD_HPM1               : Boolean = false,
  withFPD_HP0                : Boolean = false,
  withFPD_HP1                : Boolean = false,
  withFPD_HP2                : Boolean = false,
  withFPD_HP3                : Boolean = false,
  withFPD_ACE                : Boolean = false,
  withDBG_CTI0               : Boolean = false,
  withDBG_CTI1               : Boolean = false,
  withDBG_CTI2               : Boolean = false,
  withDBG_CTI3               : Boolean = false,
  withDBG_CTO0               : Boolean = false,
  withDBG_CTO1               : Boolean = false,
  withDBG_CTO2               : Boolean = false,
  withDBG_CTO3               : Boolean = false,
  withIO_PMOD0               : Boolean = false
) extends Bundle {
  val lpd_hpm0 = (withLPD_HPM0                  ) generate (LPD_HPM0.port)
  val fpd_hpm0 = (withFPD_HPM0                  ) generate (FPD_HPM0.port)
  val fpd_hpm1 = (withFPD_HPM1                  ) generate (FPD_HPM1.port)
  val fpd_hp0  = (withFPD_HP0                   ) generate (FPD_HP0.port )
  val fpd_hp1  = (withFPD_HP1                   ) generate (FPD_HP1.port )
  val fpd_hp2  = (withFPD_HP2                   ) generate (FPD_HP2.port )
  val fpd_hp3  = (withFPD_HP3                   ) generate (FPD_HP3.port )
//  val fpd_ace  = (withFPD_ACE                   ) generate (     slave(Axi4(KriaPorts.FPD_ACE_Config ))                             )
  val dbg_cti0 = (withDBG_CTI0                  ) generate (DBG_CTI0.port)
  val dbg_cti1 = (withDBG_CTI1                  ) generate (DBG_CTI1.port)
  val dbg_cti2 = (withDBG_CTI2                  ) generate (DBG_CTI2.port)
  val dbg_cti3 = (withDBG_CTI3                  ) generate (DBG_CTI3.port)
  val dbg_cto0 = (withDBG_CTO0                  ) generate (DBG_CTO0.port)
  val dbg_cto1 = (withDBG_CTO1                  ) generate (DBG_CTO1.port)
  val dbg_cto2 = (withDBG_CTO2                  ) generate (DBG_CTO2.port)
  val dbg_cto3 = (withDBG_CTO3                  ) generate (DBG_CTO3.port)
  val io_pmod0 = (withIO_PMOD0                  ) generate (IO_PMOD0.port)
}


case class KV260(
  withLPD_HPM0               : Boolean = false,
  withFPD_HPM0               : Boolean = false,
  withFPD_HPM1               : Boolean = false,
  withFPD_HP0                : Boolean = false,
  withFPD_HP1                : Boolean = false,
  withFPD_HP2                : Boolean = false,
  withFPD_HP3                : Boolean = false,
  withFPD_ACE                : Boolean = false,
  withDBG_CTI0               : Boolean = false,
  withDBG_CTI1               : Boolean = false,
  withDBG_CTI2               : Boolean = false,
  withDBG_CTI3               : Boolean = false,
  withDBG_CTO0               : Boolean = false,
  withDBG_CTO1               : Boolean = false,
  withDBG_CTO2               : Boolean = false,
  withDBG_CTO3               : Boolean = false,
  withIO_PMOD0               : Boolean = false
) extends Component {

  val io = new Interfaces(withLPD_HPM0, withFPD_HPM0, withFPD_HPM1, withFPD_HP0, withFPD_HP1, withFPD_HP2, withFPD_HP3, withFPD_ACE, withDBG_CTI0, withDBG_CTI1, withDBG_CTI2, withDBG_CTI3, withDBG_CTO0, withDBG_CTO1, withDBG_CTO2, withDBG_CTO3, withIO_PMOD0)
  
//  if (withFPD_ACE)
//    KriaPorts.setAceInterfaceAttributes(io.fpd_ace)

//  io.fpd_hpm0 <> io.fpd_hp0

  val config = new ConfigPort(LPD_HPM0.port)

  val addr  = UInt(64 bits)
  config.addElement(addr)
  val mask  = UInt(16 bits)
  config.addElement(mask)
  val errno = SInt(32 bits)
  config.addElement(errno)
  val vector = Vec.fill(4)(UInt(32 bits))
  config.addElement(vector)

  FPD_HPM0.port <> FPD_HP0.port

  io.dbg_cti0 <> io.dbg_cto0
  io.dbg_cti1 <> io.dbg_cto1

  IO_PMOD0.port.makeInput(5)
  IO_PMOD0.port.setAllOutputs()

  config.generateCStruct()
  BlockDiagram.draw(this)
}

object KV260Verilog extends App {
  Config.spinal.generateVerilog(KV260(withLPD_HPM0=true, withFPD_HPM0=true, withFPD_HP0=true, withDBG_CTO0=true, withDBG_CTI0=true, withDBG_CTO1=true, withDBG_CTI1=true, withIO_PMOD0=true))
}

object KV260Vhdl extends App {
  Config.spinal.generateVhdl(KV260())
}
