package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable
import scala.collection.mutable.Map

import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.sim.{StreamDriver, StreamMonitor, StreamReadyRandomizer}
import spinal.lib.bus.amba4.axi._


/** Class simulating an Axi4 secondary actor. It handles: transaction scheduling,
 *  transaction emission, and basic protocol checks.
 *
 *  @constructor Creates an Axi4 secondary checker/handler
 *  @param axi The Axi4 bus to handle and check.
 *  @param clockDomain The clock domain associated with the bus.
 */
class Axi4CheckerSecondary(axi: Axi4, clockDomain: ClockDomain) {

  /** Simulated memory block */
  private val memory = SparseMemory()

  // AR
  private val ARCounter = new Axi4InFlightCounter(Seq(axi.ar), Seq(axi.r), clockDomain)
  // R
  private val RDriver = ChannelDriverRandom(axi.r, clockDomain)
  // AW
  private val AWQueue = new mutable.Queue[Axi4AWJob]()
  private val AWCounter = new Axi4InFlightCounter(Seq(axi.aw), Seq(axi.b), clockDomain)
  // W
  private var wBeatCount = 0
  // B
  private val BDriver = ChannelDriverRandom(axi.b, clockDomain)

  /** Method indicating whether the simulatated secondary target has reached 
   *  full capacity for read transactions.
   *
   *  @return notFull `true` if place is still available in the target.
   */
  private def readNotInFullCapacity(): Boolean = {
    return ARCounter.count < axi.config.readIssuingCapability
  }

  /** Method indicating whether the simulatated secondary target has reached 
   *  full capacity for read transactions.
   *
   *  @return notFull `true` if place is still available in the target.
   */
  private def writeNotInFullCapacity(): Boolean = {
    return AWCounter.count < axi.config.writeIssuingCapability
  }
  
  /** AXI AR: Random drive for AR ready signal. */
  StreamReadyRandomizer(axi.ar, clockDomain, readNotInFullCapacity).setFactor(1.1f)

  /** AXI AR: Catches and handles handshakes on AR channel.
   *
   *  Upon handshake on the address read phase, method constructs a data and 
   *  response job. Fills data with function looking-up the simulated memory 
   *  target. Enqueues the created job for serving later on.
   */
  StreamMonitor(axi.ar, clockDomain) { payload =>
    val context = new Axi4ARJob(payload)
    val transaction = new Axi4RJob(axi.r, payload)
    for (beat <- 0 to payload.len.toInt) {
      transaction.enqueue({
        memory.readBigInt(
          context.alignedNextAddress(context.aligned+beat, log2Up(payload.config.bytePerWord)),
          payload.config.bytePerWord
        )
      })
    }
    RDriver.storage.enqueue(transaction)
  }

  /** AXI AW: Random drive for AW ready signal. */
  StreamReadyRandomizer(axi.aw, clockDomain, writeNotInFullCapacity).setFactor(1.1f)

  /** AXI AW: Enqueue write address phase job upon reception. */
  StreamMonitor(axi.aw, clockDomain) { payload =>
    AWQueue.enqueue(new Axi4AWJob(axi.aw))
  }

  /** AXI W: Random drive for W ready signal. */
  StreamReadyRandomizer(axi.w, clockDomain).setFactor(1.1f)

  /** AXI W: Check whether write data phase has the proper length and last is 
   *  asserted at expected time.
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
      context.alignedNextAddress(
        context.aligned+this.wBeatCount,
        log2Up(axi.config.bytePerWord)
      ),
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
