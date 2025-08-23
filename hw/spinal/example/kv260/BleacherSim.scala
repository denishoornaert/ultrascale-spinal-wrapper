package example.kv260


import scala.collection.mutable


import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.misc.SizeMapping
import spinal.sim._
import spinal.lib.sim._


import kv260._
import ultrascaleplus.parameters._
import ultrascaleplus.bus.amba.axi4.sim._


object BleacherSim extends App {
  Config.sim.compile{
      val dut = new Bleacher()
      dut
    }.doSim { dut =>

    val primary = new Axi4CheckerPrimary(dut.io.fpd.hpm0, dut.clockDomain)
    val secondary = new Axi4CheckerSecondary(dut.io.fpd.hp0, dut.clockDomain)

    val expectedData = Seq(
      BigInt("01010101010101010101010101010101", 16),
      BigInt("02020202020202020202020202020202", 16),
      BigInt("03030303030303030303030303030303", 16),
      BigInt("04040404040404040404040404040404", 16)
    )

    val strobeBits = Seq(
      BigInt("FFFF", 16),
      BigInt("FFFF", 16),
      BigInt("FFFF", 16),
      BigInt("FFFF", 16)
    )

    var burstBeatCounter: Int = 0
    StreamMonitor(dut.io.fpd.hpm0.r, dut.clockDomain) { payload =>
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
    dut.clockDomain.forkStimulus(period = 10)

    // DUMP DATA
    for (t <- 0 until 32) {
      val aw = new Axi4AWJob(
        channel = dut.io.fpd.hpm0.aw,
        addr    = AddressMap.FPD_HPM0.base+(t*64),
        id      = 0x6800+(t*0x0020),
        len     = expectedData.length-1,
        size    = log2Up(dut.io.fpd.hpm0.aw.config.bytePerWord)
      )
      val w = new Axi4WJob(
        channel = dut.io.fpd.hpm0.w,
        data    = expectedData,
        strb    = strobeBits,
        parent  = aw
      )
      primary.addWrite(aw, w)
    }

    primary.startWrite()
    dut.clockDomain.waitRisingEdgeWhere(primary.allWritesCompleted())
    primary.stopWrite()
    val writeBW = primary.getWriteBandwidth()
    println(s"Write performed at ${writeBW} bytes per clock cycle.")

    // FETCH DATA
    for (t <- 0 until 32) {
      primary.addRead(
        new Axi4ARJob(
          channel = dut.io.fpd.hpm0.ar,
          addr    = AddressMap.FPD_HPM0.base+(t*64),
          id      = 0x6800+(t*0x0020),
          len     = 4-1,
          size    = log2Up(dut.io.fpd.hpm0.ar.config.dataWidth/8)
        )
      )
    }

    primary.startRead()
    dut.clockDomain.waitRisingEdgeWhere(primary.allReadsCompleted())
    primary.stopRead()
    val readBW = primary.getReadBandwidth()
    println(s"Read performed at ${readBW} bytes per clock cycle.")

  }
}
