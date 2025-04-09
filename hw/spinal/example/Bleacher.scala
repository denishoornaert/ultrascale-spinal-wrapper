package example

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import kv260._
import ultrascaleplus.scripts._

import example.plim._


case class Bleacher() extends KV260(
  config = new KV260Config(
    withFPD_HPM0 = true,
    withFPD_HP0  = true
  )
) with Plim {  
  
  override def connectAWAddr(primary: UInt, secondary: UInt): Unit = {
    secondary := Cat(U"8'h08", U"4'h0", primary(31 downto 16), primary(11 downto 0)).asUInt
  }

  override def connectARAddr(primary: UInt, secondary: UInt): Unit = {
    secondary := Cat(U"8'h08", U"4'h0", primary(31 downto 16), primary(11 downto 0)).asUInt
  }

  override def connectAWId(primary: UInt, secondary: UInt): Unit = {
    secondary := primary(5, secondary.getWidth bits)
  }

  override def connectARId(primary: UInt, secondary: UInt): Unit = {
    secondary := primary(5, secondary.getWidth bits)
  }

  override def connectBId(primary: UInt, secondary: UInt): Unit = {
    primary := Cat(B"01101", secondary, B"00000").asUInt
  }

  override def connectRId(primary: UInt, secondary: UInt): Unit = {
    primary := Cat(B"01101", secondary, B"00000").asUInt
  }

  connect(io.fpd.hpm0, io.fpd.hp0)

  KernelModule.add(io.fpd.hpm0)
  KernelModule.generate()
  this.generate()
}


object BleacherVerilog extends App {
  Config.spinal.generateVerilog(Bleacher())
}

object BleacherVhdl extends App {
  Config.spinal.generateVhdl(Bleacher())
}
