package example.kv260


import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.misc.SizeMapping
import spinal.sim._


import kv260._
import ultrascaleplus.parameters._
import ultrascaleplus.bus.amba.axi4.sim._



class Primary(axi: Axi4, clock: ClockDomain) extends Axi4CheckerPrimary(axi, clock) {
  
  // Hardcoded template data
  val expectedData = Seq(
    BigInt("01010101010101010101010101010101", 16),
    BigInt("02020202020202020202020202020202", 16),
    BigInt("03030303030303030303030303030303", 16),
    BigInt("04040404040404040404040404040404", 16)
  )

  var readTransactionsCreated: Int = 0
  override val readTransactionsCap: Int = 8

  override def genReadCmd(): Unit = {
    if (this.readInProgress & (readTransactionsCreated < readTransactionsCap)) {
      this.ARQueue += new Axi4RJob(axi.ar, AddressMap.FPD_HPM0.base+(readTransactionsCreated*64), 0x6800+(readTransactionsCreated*0x0020), 4-1)
      readTransactionsCreated += 1
    }
  }

  override def readDataAssertionFunction(id: Int, data: List[BigInt]): Unit = {
    for (b <- 0 until 4) {
      assert(
        assertion = (data(b) == expectedData(b)),
        message   = s"Data mismatch for read transaction ID = 0x${id.toHexString} at beat #${b}. Expected 0x${expectedData(b).toString(16)} but 0x${data(b).toString(16)} obtained."
      )
    }
  }
  
  var writeTransactionsCreated: Int = 0
  override val writeTransactionsCap: Int = 8

  override def genWriteCmd(): Unit = {
    if (this.writeInProgress & (writeTransactionsCreated < writeTransactionsCap)) {
      this.AWQueue += new Axi4WJob(axi.aw, AddressMap.FPD_HPM0.base+(writeTransactionsCreated*64), 0x6800+(writeTransactionsCreated*0x0020), 4-1, expectedData)
      writeTransactionsCreated += 1
    }
  }

}

class Secondary(axi: Axi4, clock: ClockDomain) extends Axi4CheckerSecondary(axi, clock) {}


object BleacherSim extends App {
  Config.sim.compile{
      val dut = new Bleacher()
      dut
    }.doSim { dut =>
    dut.clockDomain.forkStimulus(period = 10)

    val primary = new Primary(dut.io.fpd.hpm0, dut.clockDomain)
    val secondary = new Secondary(dut.io.fpd.hp0, dut.clockDomain)

    dut.clockDomain.waitSampling(50) // Arbitrary
    
    primary.startWrite()
    primary.waitForAllWriteCompleted()

    primary.startRead()
    primary.waitForAllReadCompleted()
    
    val readBW = primary.getReadBandwidth()
    println(s"Read performed at ${readBW} bytes per clock cycle.")
    val writeBW = primary.getWriteBandwidth()
    println(s"Write performed at ${writeBW} bytes per clock cycle.")
  }
}
