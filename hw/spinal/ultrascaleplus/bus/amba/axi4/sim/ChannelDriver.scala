package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable


import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim.StreamDriver
import spinal.lib.bus.amba4.axi._


/** Class driving and scheduling Axi4Job on an AXI4 channel.
 *
 *  Abstract class containing with queue of AXI4 jobs that can be scheduled 
 *  according to a policy. The exact policy (i.e., next) and bus placement 
 *  functions are kept abstract to be overriden.
 *
 *  @constructor Create a channel driver with the associated channel and clock.
 *  @param channel The associated channel on which to drive the signal.
 *  @param cd The associated clock of the AXI4 channel.
 */
abstract class ChannelDriver[T <: Data](val channel: Stream[T], cd: ClockDomain) {

  /** Internal queue storing the unserved in-flight AXI4 job. */
  val storage = new Axi4JobQueue(cd)

  /** AXI4 job scheduled for placement on the AXI channel. */
  private var scheduled: Axi4Job = null

  /** 
   *  Method in charge of returning the index of the next job to be 
   *  scheduled.
   *
   *  @return index index of the job to be scheduled in the AXI4 job queue.
   *  Method returns -1 if no available job are found. 
   */
  protected def next(): Int

  /** Method picking the next job to be scheduled and placed on the channel. */
  private def schedule(): Unit = {
    val pick = this.next()
    // If no candidate, skip replacement
    if (pick != -1) {
      this.scheduled = this.storage(pick)
      this.storage.remove(pick)
    }
  }

  /**
   * Internal StreamDriver in charge of placing signals on the channel.
   * The driver also looks after schedulable jobs when the previous has been 
   * performed (or if none where previously scheduled).
   */
  private val ctrl = StreamDriver(channel, cd) { p =>
    // Schedule new job if any available AND ready
    if (this.storage.hasCandidate() && ((this.scheduled == null) || this.scheduled.isDone())) {
      this.schedule()
    }
    // If job scheduled and the job is not done, place on bus
    if ((this.scheduled != null) && !this.scheduled.isDone()) {
      this.scheduled.place()
      true
    }
    else {
      false
    }
  }

  // Explicitly set control over StreamDriver's flow
  this.ctrl.delay = 0
  this.ctrl.setFactor(1)

  /** Indicates whether there are pending transactions. */
  def isDone(): Boolean = {
    return this.storage.isEmpty && (this.scheduled == null || this.scheduled.isDone())
  }

}


/** 
 * Specialized version of the channel driver serving pending transactions
 * out-of-order (i.e., randomly) amongst pending transactions marked as ready.
 *
 * @constructor Creates an AXI4 channel driver with an AXI4 channel and a clock domain.
 */
class ChannelDriverRandom[T <: Data](stream: Stream[T], cd: ClockDomain) extends ChannelDriver(stream, cd) {

  /** Returns a randomly picked transaction out of the pending ones marked as ready. */
  override def next(): Int = {
    var index = -1
    val candidates = this.storage.getCandidates()
    if (candidates.nonEmpty)
      index = candidates(simRandom.nextInt(candidates.length))
    return index
  }

}


/** 
 * Companion object of [[ultrascaleplus.bus.amba.axi4.sim.ChannelDriverRandom]] class.
 */
object ChannelDriverRandom {
  
  def apply[T <: Data](stream : Stream[T], cd: ClockDomain) = new ChannelDriverRandom[T](stream, cd)

}


/** 
 * Specialized version of the channel driver serving pending transactions
 * in-order (i.e., in the order they arrived).
 *
 * @constructor Creates an AXI4 channel driver with an AXI4 channel and a clock domain.
 */
class ChannelDriverInOrder[T <: Data](stream: Stream[T], cd: ClockDomain) extends ChannelDriver(stream, cd) {

  /** Returns head of the pending transactions' queue once ready for scheduling. */
  override def next(): Int = {
    var index = -1
    if (this.storage(0).ready())
      index = 0
    return index
  }

}


/** 
 * Companion object of [[ultrascaleplus.bus.amba.axi4.sim.ChannelDriverInOrder]] class.
 */
object ChannelDriverInOrder {

  def apply[T <: Data](stream : Stream[T], cd: ClockDomain) = new ChannelDriverInOrder[T](stream, cd)

}
