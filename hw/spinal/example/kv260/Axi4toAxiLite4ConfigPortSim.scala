package example.kv260


import scala.collection.mutable


import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.misc.SizeMapping
import spinal.sim._
import spinal.lib.sim._


import kv260._
import ultrascaleplus.Config
import ultrascaleplus.parameters._
import ultrascaleplus.bus.amba.axi4.sim._


object Axi4ToAxiLite4ConfigPortSim extends App {
  Config.sim.compile{
      val dut = new Axi4ToAxiLite4ConfigPort()
      dut
    }.doSim { dut =>

    dut.clockDomain.forkStimulus(period = 10)

    val plclk0 = new ClockDomain(dut.io.pl.clk0.clock, dut.io.pl.clk0.reset)
    val primary = new Axi4CheckerPrimary(dut.io.lpd.hpm0, plclk0)

    val expectedData = Seq(
      BigInt("AAAAAAAAAAAAAAAA9999999999999999", 16),
      BigInt("33333333333333332222222222222222", 16),
      BigInt("55555555555555554444444444444444", 16),
      BigInt("77777777777777776666666666666666", 16)
    )

    val strobeBits = Seq(
      BigInt("FFFF", 16),
      BigInt("FFFF", 16),
      BigInt("FFFF", 16),
      BigInt("FFFF", 16)
    )

    var burstBeatCounter: Int = 0
    StreamMonitor(dut.io.lpd.hpm0.r, plclk0) { payload =>
      assert(
        assertion = (payload.data.toBigInt == expectedData(burstBeatCounter)),
        message   = s"Data mismatch for read transaction ID = 0x${payload.id.toBigInt.toString(16)} at beat #${burstBeatCounter}. Expected 0x${expectedData(burstBeatCounter).toString(16)} but 0x${payload.data.toBigInt.toString(16)} obtained."
      )
      // Reset for next transaction
      if (burstBeatCounter == 4-1)
        burstBeatCounter = 0
      else
        burstBeatCounter += 1
    }

    // Actually starts
    plclk0.forkStimulus(period = 10)

    // DUMP DATA
    val aw = new Axi4AWJob(
      channel = dut.io.lpd.hpm0.aw,
      addr    = AddressMap.LPD_HPM0.base,
      id      = 0x6800,
      len     = expectedData.length-1,
      size    = log2Up(dut.io.lpd.hpm0.aw.config.bytePerWord)
    )
    val w = new Axi4WJob(
      channel = dut.io.lpd.hpm0.w,
      data    = expectedData,
      strb    = strobeBits,
      parent  = aw
    )
    primary.addWrite(aw, w)

    primary.startWrite()
    plclk0.waitRisingEdgeWhere(primary.allWritesCompleted())
    primary.stopWrite()
    val writeBW = primary.getWriteBandwidth()
    println(s"Write performed at ${writeBW} bytes per clock cycle.")

    // FETCH DATA
    primary.addRead(
      new Axi4ARJob(
        channel = dut.io.lpd.hpm0.ar,
        addr    = AddressMap.LPD_HPM0.base,
        id      = 0x6800,
        len     = 4-1,
        size    = log2Up(dut.io.lpd.hpm0.ar.config.dataWidth/8)
      )
    )

    primary.startRead()
    plclk0.waitRisingEdgeWhere(primary.allReadsCompleted())
    primary.stopRead()
    val readBW = primary.getReadBandwidth()
    println(s"Read performed at ${readBW} bytes per clock cycle.")

  }
}
