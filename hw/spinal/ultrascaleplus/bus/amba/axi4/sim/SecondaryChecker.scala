package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable
import scala.collection.mutable.Map

import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.sim.{StreamDriver, StreamMonitor, StreamReadyRandomizer}
import spinal.lib.bus.amba4.axi._


class Axi4CheckerSecondary(axi: Axi4, clockDomain: ClockDomain) {

  // Defined response time for each transactions
  val writeAgeThreshold = 0

  // Simulated memory
  private val memory = SparseMemory()

  // Maintaining clock count
  private var clock_count: BigInt = 0

  // AR
  // R
  private val RDriver = ChannelDriverInOrder(axi.r, clockDomain)
  // AW
  private val AWQueue = new mutable.Queue[Axi4AWJob]()
  // W
  private var wBeatCount = 0
  // B
  private val BDriver = ChannelDriverInOrder(axi.b, clockDomain)

  def readNotInFullCapacity(): Boolean = {
    // Add scheduled entry if relevant (i.e., not done)
    return RDriver.storage.length+(!RDriver.isDone()).toInt < axi.config.readIssuingCapability
  }

  def writeNotInFullCapacity(): Boolean = {
    return AWQueue.length < axi.config.writeIssuingCapability
  }
  
  /** AXI AR
   *
   */
  StreamReadyRandomizer(axi.ar, clockDomain, readNotInFullCapacity).setFactor(1.0f)

  /** AXI AR
   *  Catches handshakes on AR channel
   */
  StreamMonitor(axi.ar, clockDomain) { payload =>
    val context = new Axi4ARJob(payload)
    val transaction = new Axi4RJob(axi.r, payload)
    for (beat <- 0 until payload.len.toInt) {
      transaction.enqueue({
        memory.readBigInt(
          context.alignedNextAddress(context.aligned+beat, log2Up(payload.config.bytePerWord)),
          payload.config.bytePerWord
        )
      })
    }
    RDriver.storage.enqueue(transaction)
  }

  /** AXI AW
   *
   */
  StreamReadyRandomizer(axi.aw, clockDomain, writeNotInFullCapacity).setFactor(1.0f)

  /** AXI AW
   *
   */
  StreamMonitor(axi.aw, clockDomain) { payload =>
    AWQueue.enqueue(new Axi4AWJob(axi.aw))
  }

  /** AXI W
   *
   */
  StreamReadyRandomizer(axi.w, clockDomain).setFactor(1.0f)

  /** AXI W
   *
   */
  StreamMonitor(axi.w, clockDomain) { payload =>
    val context = AWQueue.front
    
    if (this.wBeatCount == context.len) {
      assert(
        assertion = payload.last.toBoolean,
        message   = f"Expected axi.w.last to be asserted at the ${this.wBeatCount}th beat."
      )
    }

    // must be done before manipulating the wBeatcounter
    memory.writeArray(
      context.alignedNextAddress(context.aligned+this.wBeatCount, log2Up(axi.config.dataWidth/8)),
      payload.data.toBytes
    )
    
    if (payload.last.toBoolean) {
      assert(
        assertion = this.wBeatCount == context.len,
        message   = f"axi.w.last asserted before due: ${this.wBeatCount}/${context.len}."
      )
      this.wBeatCount = 0
      // Transfer
      BDriver.storage.enqueue(new Axi4BJob(axi.b, AWQueue.dequeue()))
    }
    else {
      this.wBeatCount += 1
    }
  }

}
