package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable


import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim.StreamDriver
import spinal.lib.bus.amba4.axi._


/**
 *  Object containing all encodings hard-defined in the protocol. 
 *  [[https://developer.arm.com/documentation/ihi0022/latest/ See the official documentation for further details.]]
 */
object Axi4Sim {
  
  /**
   *  Burst type encodings.
   *  [[https://developer.arm.com/documentation/ihi0022/latest/ See the official documentation for further details.]]
   */
  object burst {
    /** Each beat of the burst targets the same address given during the address phase */
    val FIXED    = 0
    /** Each beat of the burst targets the next address, starting from the address given during the address phase. */
    val INCR     = 1
    /** Same as [[Axi4Sim.burst.INCR]] but when address given during the address phase is not aligned with the burst length */
    val WRAP     = 2
    /** RESERVED do not use! */
    val RESERVED = 3
  }

  /**
   *  Response type encodings.
   *  [[https://developer.arm.com/documentation/ihi0022/latest/ See the official documentation for further details.]]
   */
  object resp {
    /** Successful non-exclusive transaction */
    val OKAY   = 0
    /** Successful exclusive transaction */
    val EXOKAY = 1
    /** Error with the transaction itself (not with the target). Can be due to size, burst type, ... */
    val SLVERR = 2
    /** Error with target. For instance, target could not be found (i.e., error decoding address). */
    val DECERR = 3
  }

}


/** Abstract class representing and encapsulating the base logic of an Axi4Job.
 *
 *  Class keeps track of the job age and define basic methods to be defined by child classes.
 *  It applies to all AXI4 channel jobs (i.e., [[Axi4ARJob]], [[Axi4AWJob]], [[Axi4RJob]], [[Axi4WJob]], [[Axi4BJob]]).
 *
 *  @constructor Creates a job with a delay as a no-parameter function returning an Int (default returns 0).
 */
abstract class Axi4Job (private val delay: () => Int = () => 0) {
  
  /** Keep track of the job age. Monotonously increases. */
  private var age: Int = 0

  /** Increase age of job by one step. */
  def makeOlder(): Unit = {
    age += 1
  }

  /**
   *  Indicates whether the job is ready (i.e., mature) with respect to its age and the constructor-specified delay.
   *
   *  @return ready Returns a boolean indicating whether the joib is mature.
   */
  def ready(): Boolean = {
    return age >= this.delay()
  }

  /**
   *  Indicates if the job has been completed.
   *
   *  @return `true` if the job as completed.
   */
  def isDone(): Boolean

  /**
   *  Abstract method in charge of placing signals on the channel.
   */
  def place(): Unit

}


class Axi4AXJob(channel: Axi4Ax, val addr: BigInt, val id: Int, val len: Int, val size: Int, val burst: Int = Axi4Sim.burst.INCR) extends Axi4Job() {

  // Typical AXI info
  val aligned = (addr >> log2Up(channel.config.bytePerWord)) << log2Up(channel.config.bytePerWord)
  val cache   = 8
  val lock    = 0
  val prot    = 0
  val qos     = 0
  val region  = 0

  val dataTransactionSize : Int    = (1+len)<<size
  val lowerWrapBoundary   : BigInt = (aligned/dataTransactionSize)*dataTransactionSize 
  val upperWrapBoundary   : BigInt = lowerWrapBoundary+dataTransactionSize

  private var placed: Boolean = false

  // check for read/write over 4k boundary
  if (burst == 1) {
    assert(
      assertion = (((len+1)<<size)+(addr%4096)) <= 4096,
      message   = s"Read/write request crossing 4KB boundary (addr=${addr.toString(16)}, len=${len}, size=${size})"
    )
  }

  private def incrAddress(i : BigInt): BigInt = {
    return ((addr >> size) + i) << size
  }
  
  private def wrapAddress(i : BigInt): BigInt = {
    var ret = incrAddress(i)
    if (ret >= upperWrapBoundary) {
      ret = ret-dataTransactionSize
    }
    return ret
  }

  private def nextAddress(i : BigInt): BigInt = {
    var ret: BigInt = 0
    if (burst == Axi4Sim.burst.FIXED)
      ret = addr
    else if (burst == Axi4Sim.burst.INCR)
      ret = incrAddress(i)
    else if (burst == Axi4Sim.burst.WRAP)
      ret = wrapAddress(i)
    else
      throw new Exception("The AxBurst encoding B\"11\" is reserved and cannot be used!")
    return ret
  }

  def alignedNextAddress(i : BigInt, maxBurstSize : Int): BigInt = {
    return (nextAddress(i) >> maxBurstSize) << maxBurstSize
  }

  override def isDone(): Boolean = {
    return this.placed
  }

  override def place(): Unit = {
    this.channel.addr     #= this.addr
    if (this.channel.config.useId)
      this.channel.id     #= this.id
    if (this.channel.config.useBurst)
      this.channel.burst  #= this.burst
    if (this.channel.config.useLen)
      this.channel.len    #= this.len
    if (this.channel.config.useSize)
      this.channel.size   #= this.size
    if (this.channel.config.useCache)
      this.channel.cache  #= this.cache
    if (this.channel.config.useLock)
      this.channel.lock   #= this.lock
    if (this.channel.config.useProt)
      this.channel.prot   #= this.prot
    if (this.channel.config.useQos)
      this.channel.qos    #= this.qos
    if (this.channel.config.useRegion)
      this.channel.region #= this.region
    // Set as placed
    this.placed = true
  }

}

/**
 * The class is offered as an interface to Axi4Job and provide tyoe check on the channel provide.
 * Note: the size is expressed in bytes and assumed to be a power of two.
 */
class Axi4AWJob(channel: Axi4Aw, addr: BigInt, id: Int, len: Int, size: Int, burst: Int = Axi4Sim.burst.INCR) extends Axi4AXJob(channel, addr, id, len, size, burst) {

  /**
   * Constructor made to capture bus state during simulation.
   * Accordingly, no data canbe stored and captured as it would have to be accumulated.
   * This can be done after facts.
   */
  def this(channel: Axi4Aw) = this(
    channel,
    channel.addr.toBigInt,
    channel.id.toInt,
    channel.len.toInt,
    channel.size.toInt,
    channel.burst.toInt
  )

  def this(axi: Axi4) = this(axi.aw)

}

/**
 * The class is offered as an interface to Axi4Job and provide tyoe check on the channel provide.
 * Note: the size is expressed in bytes and assumed to be a power of two.
 */
class Axi4ARJob(channel: Axi4Ar, addr: BigInt, id: Int, len: Int, size: Int, burst: Int = Axi4Sim.burst.INCR) extends Axi4AXJob(channel, addr, id, len, size, burst) {

  /**
   * Constructor made to capture bus state during simulation.
   * Accordingly, no data canbe stored and captured as it would have to be accumulated.
   * This can be done after facts.
   */
  def this(channel: Axi4Ar) = this(
    channel,
    channel.addr.toBigInt,
    channel.id.toInt,
    channel.len.toInt,
    channel.size.toInt,
    channel.burst.toInt
  )

  def this(axi: Axi4) = this(axi.ar)

}

class Axi4WJob(channel: Axi4W, val data: Seq[BigInt], val strb: Seq[BigInt]) extends Axi4Job() {

  assert(
    assertion = (data.length == strb.length),
    message   = f"Data and strb length mismatch!"
  )

  // Starts at one because var will be compared to data length during the creation of a last signals
  private var beat: Int = 0

  override def isDone(): Boolean = {
    return (this.beat == this.data.length)
  }

  override def place(): Unit = {
    this.channel.data #= this.data(this.beat)
    if (this.channel.config.useStrb)
      this.channel.strb #= this.strb(this.beat)
    if (this.channel.config.useLast)
      this.channel.last #= (this.beat == this.data.length-1)
    // Must be at the end to retrieve data correctly
    this.beat += 1
  }

}

class Axi4RJob(channel: Axi4R, val id: Int = 0, val resp: Int = Axi4Sim.resp.OKAY) extends Axi4Job() {
  
  private val data: mutable.Queue[() => BigInt] = new mutable.Queue[() => BigInt]()

  def this(channel: Axi4R, context: Axi4Ar) = this(channel, context.id.toInt)

  def this(channel: Axi4R) = this(channel, channel.id.toInt)

  def enqueue(func: () => BigInt): Unit = {
    this.data.enqueue(func)
  }

  def enqueue(value: BigInt): Unit = {
    this.enqueue(() => value)
  }

  override def isDone(): Boolean = {
    return this.data.isEmpty
  }

  override def place(): Unit = {
    this.channel.data #= this.data.front()
    if (this.channel.config.useId)
      this.channel.id #= this.id
    if (this.channel.config.useResp)
      this.channel.resp #= this.resp
    if (this.channel.config.useLast)
      this.channel.last #= (this.data.length == 1)
    this.data.dequeue
  }
}

class Axi4BJob(channel: Axi4B, val id: Int, val resp: Int) extends Axi4Job() {

  private var placed: Boolean = false

  // TODO: resp can be deduced from the Axi4AWJob!
  def this(channel: Axi4B, job: Axi4AWJob) = this(channel, job.id, Axi4Sim.resp.OKAY)

  override def isDone(): Boolean = {
    return placed
  }

  override def place(): Unit = {
    if (this.channel.config.useId)
      this.channel.id #= id
    if (this.channel.config.useResp)
      this.channel.resp #= resp
    placed = true
  }

}


class Axi4JobQueue(cd: ClockDomain) extends mutable.Queue[Axi4Job]() {

  def hasCandidate(): Boolean = {
    return super.nonEmpty && this.map(p => p.ready()).reduce(_ || _)
  }

  def getCandidates(): Seq[Int] = {
    return this.zipWithIndex.filter(_._1.ready()).map(_._2).toSeq
  }

  // Every clock cycle, make job older
  cd.onRisingEdges({
    this.foreach({ p => p.makeOlder() })
  })

}
