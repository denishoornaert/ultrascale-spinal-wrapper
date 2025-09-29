package example.kv260

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._


import ultrascaleplus.Config
import ultrascaleplus.clock.PLClockingArea
import ultrascaleplus.clock.pll._
import ultrascaleplus.ip.Ethernet
import ultrascaleplus.bus.amba.axi4.Axi4toAxiLite4


import kv260._


case class Axi4ToAxiLite4ConfigPort() extends KV260(
  config = new KV260Config(
    withPL_CLK0  = PLL.IO(250 MHz),
    withLPD_HPM0 =            true
  )
) {

  io.lpd.hpm0.associate(io.pl.clk0)

  val cd = new PLClockingArea(io.pl.clk0) {
    val lite = AxiLite4(AxiLite4Config(io.lpd.hpm0.config.addressWidth, 64))
    val port = new AxiLite4SlaveFactory(lite)
    lite << Axi4toAxiLite4(io.lpd.hpm0, lite.config)

    val counter0 = Reg(UInt(64 bits)) init(BigInt("0000000000000000", 16))
    val counter1 = Reg(UInt(64 bits)) init(BigInt("1111111111111111", 16))
    val counter2 = Reg(UInt(64 bits)) init(BigInt("2222222222222222", 16))
    val counter3 = Reg(UInt(64 bits)) init(BigInt("3333333333333333", 16))
    val counter4 = Reg(UInt(64 bits)) init(BigInt("4444444444444444", 16))
    val counter5 = Reg(UInt(64 bits)) init(BigInt("5555555555555555", 16))
    val counter6 = Reg(UInt(64 bits)) init(BigInt("6666666666666666", 16))
    val counter7 = Reg(UInt(64 bits)) init(BigInt("7777777777777777", 16))

    port.readAndWrite(counter0, io.lpd.hpm0.apertures(0).base+(0*8))
    port.readAndWrite(counter1, io.lpd.hpm0.apertures(0).base+(1*8))
    port.readAndWrite(counter2, io.lpd.hpm0.apertures(0).base+(2*8))
    port.readAndWrite(counter3, io.lpd.hpm0.apertures(0).base+(3*8))
    port.readAndWrite(counter4, io.lpd.hpm0.apertures(0).base+(4*8))
    port.readAndWrite(counter5, io.lpd.hpm0.apertures(0).base+(5*8))
    port.readAndWrite(counter6, io.lpd.hpm0.apertures(0).base+(6*8))
    port.readAndWrite(counter7, io.lpd.hpm0.apertures(0).base+(7*8))

    port.build()

  }

}

object Axi4ToAxiLite4ConfigPortVerilog extends App {
  Config.spinal.generateVerilog(Axi4ToAxiLite4ConfigPort())
}

object Axi4ToAxiLite4ConfigPortVhdl extends App {
  Config.spinal.generateVhdl(Axi4ToAxiLite4ConfigPort())
}
