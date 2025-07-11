package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable


import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim.{StreamDriver, StreamMonitor}
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
abstract class Axi4Job (
  private val delay: () => Int = () => 0
) {
  
  /** Keep track of the job age. Monotonously increases. */
  private var age: Int = 0

  /** Status marker */
  protected var done: Boolean = false

  /** Status marker */
  protected var placed: Boolean = false

  /** Increase age of job by one step. */
  def makeOlder(): Unit = {
    age += 1
  }

  /**
   *  Indicates whether the job is ready (i.e., mature) with respect to its 
   *  age and the constructor-specified delay.
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
  def isDone(): Boolean = {
    return this.done
  }

  def markAsDone(): Unit = {
    this.done = true
  }

  /**
   *  Abstract method in charge of placing signals on the channel.
   */
  def place(): Unit

  def wasPlaced(): Boolean = {
    return this.placed
  }

}


/** Specialized (but abstract) Axi4 Job class for address phases.
 *
 *  The class provides common features for addres phases such as assertions, 
 *  address generation for burst's beats, and so on.
 *
 *  @constructor Creates a address phase job with
 *  @param channel the associated channel (either [[Axi4ARJob]] and [[Axi4AWJob]])
 *  @param addr the target address 
 *  @param id the transaction ID
 *  @param len the burst length of the transaction
 *  @param size the amount of bytes used within a beat
 *  @param burst the burst type of the transaction (default [[Axi4Sim.burst.INCR]])
 */
abstract class Axi4AXJob(
  channel  : Axi4Ax,
  val addr : BigInt,
  val id   : Int,
  val len  : Int,
  val size : Int,
  val burst: Int = Axi4Sim.burst.INCR
) extends Axi4Job() {

  val cache   = 8
  val lock    = 0
  val prot    = 0
  val qos     = 0
  val region  = 0

  /** Bus width aligned verison of the transaction's (job's) address. */
  val aligned = (addr >> log2Up(channel.config.bytePerWord)) << log2Up(channel.config.bytePerWord)
  /** Total transaction size (i.e., beats*size) */
  private val dataTransactionSize : Int    = (len + 1) << size
  /** Start of the wrap transaction as an aligned bus width address */
  private val lowerWrapBoundary   : BigInt = (aligned/dataTransactionSize)*dataTransactionSize
  /** End of the wrap transaction as an aligned bus width address */
  private val upperWrapBoundary   : BigInt = lowerWrapBoundary+dataTransactionSize

  // check for read/write over 4k boundary
  if (burst == Axi4Sim.burst.INCR) {
    assert(
      assertion = (((len+1)<<size)+(addr%4096)) <= 4096,
      message   = s"Read/write request crossing 4KB boundary (addr=${addr.toString(16)}, len=${len}, size=${size})"
    )
  }

  /** Generates burst beat address for [[Axi4Sim.burst.INCR]] burst type.
   *
   *  @param i The i th desired burst's beat.
   *  @return The address of the i th burst's beat (bus width aligned).
   */
  private def incrAddress(i : BigInt): BigInt = {
    return ((addr >> size) + i) << size
  }
  
  /** Generates burst beat address for [[Axi4Sim.burst.WRAP]] burst type.
   *
   *  @param i The i th desired burst's beat.
   *  @return The address of the i th burst's beat (bus width aligned).
   */
  private def wrapAddress(i : BigInt): BigInt = {
    var ret = incrAddress(i)
    if (ret >= upperWrapBoundary) {
      ret = ret-dataTransactionSize
    }
    return ret
  }

  /** Generates burst beat address for job's burst type.
   *
   *  @param i The i th desired burst's beat.
   *  @return The address of the i th burst's beat (bus width aligned).
   */
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

  /** Generates burst beat address for job's burst type.
   *
   *  @param i The i th desired burst's beat.
   *  @param maxBurstSize The size of the bus width
   *  @return The address of the i th burst's beat (bus width aligned).
   */
  def alignedNextAddress(i : BigInt, maxBurstSize : Int): BigInt = {
    return (nextAddress(i) >> maxBurstSize) << maxBurstSize
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

/** Specialized class of Axi$ Job for write address channel.
 *
 *  The class is offered as an interface to Axi4Job and provide tyoe check on 
 *  the channel provide. **Note:** the size is expressed in bytes and assumed 
 *  to be a power of two.
 *
 *  @constructor Creates a Axi4 job for the write address phase.
 *  @param channel the associated channel.
 *  @param addr the target address 
 *  @param id the transaction ID
 *  @param len the burst length of the transaction
 *  @param size the amount of bytes used within a beat
 *  @param burst the burst type of the transaction (default [[Axi4Sim.burst.INCR]])
 */
class Axi4AWJob(
  channel: Axi4Aw,
  addr   : BigInt,
  id     : Int,
  len    : Int,
  size   : Int,
  burst  : Int = Axi4Sim.burst.INCR
) extends Axi4AXJob(
  channel,
  addr,
  id,
  len,
  size,
  burst
) {

  /**
   *  Constructor made to capture bus state during simulation.
   *  Accordingly, no data can be stored and captured as it would have to be
   *  accumulated. This can be done after facts.
   *
   *  @param channel [[Axi4Aw]] channel we want to capture the content.
   */
  def this(channel: Axi4Aw) = this(
    channel,
    channel.addr.toBigInt,
    channel.id.toInt,
    channel.len.toInt,
    channel.size.toInt,
    channel.burst.toInt
  )

  /**
   *  Constructor made to capture bus state during simulation.
   *  Accordingly, no data can be stored and captured as it would have to be
   *  accumulated. This can be done after facts.
   *
   *  @param axi Full [[spinal.lib.bus.amba4.axi.Axi4]] bus from which the channel we want to capture
   *  the content can be extracted.
   */
  def this(axi: Axi4) = this(axi.aw)

}

/** Specialized class of Axi4 Job for write address channel.
 *
 *  The class is offered as an interface to Axi4Job and provide tyoe check on 
 *  the channel provide. **Note:** the size is expressed in bytes and assumed 
 *  to be a power of two.
 *
 *  @constructor Creates a Axi4 job for the write address phase.
 *  @param channel the associated channel.
 *  @param addr the target address 
 *  @param id the transaction ID
 *  @param len the burst length of the transaction
 *  @param size the amount of bytes used within a beat
 *  @param burst the burst type of the transaction (default [[Axi4Sim.burst.INCR]])
 */
class Axi4ARJob(
  channel: Axi4Ar,
  addr   : BigInt,
  id     : Int,
  len    : Int,
  size   : Int,
  burst  : Int = Axi4Sim.burst.INCR
) extends Axi4AXJob(
  channel,
  addr,
  id,
  len,
  size,
  burst
) {

  /**
   *  Constructor made to capture bus state during simulation.
   *  Accordingly, no data can be stored and captured as it would have to be
   *  accumulated. This can be done after facts.
   *
   *  @param channel [[Axi4Ar]] channel we want to capture the content.
   */
  def this(channel: Axi4Ar) = this(
    channel,
    channel.addr.toBigInt,
    channel.id.toInt,
    channel.len.toInt,
    channel.size.toInt,
    channel.burst.toInt
  )

  /**
   *  Constructor made to capture bus state during simulation.
   *  Accordingly, no data can be stored and captured as it would have to be
   *  accumulated. This can be done after facts.
   *
   *  @param axi Full [[spinal.lib.bus.amba4.axi.Axi4]] bus from which the channel we want to capture
   *  the content can be extracted.
   */
  def this(axi: Axi4) = this(axi.ar)

}

/** Specialized class if the Axi4 job for write data phase.
 *
 *  **NOTE:** constructed job assumes the burst type to be [[Axi4Sim.burst.INCR]].
 *  
 *  @constructor Creates a [[spinal.lib.bus.amba4.axi.Axi4]] job for the write data phase.
 *  @param channel [[spinal.lib.bus.amba4.axi.Axi4]] channel the [[Axi4WJob]] is associated to.
 *  @param data Sequence of the [[BigInt]] representing the data to be written.
 *  @param strb Sequence of [[BigInt]] representing the valid bytes in each beats.
 *  @param parent Reference to the associated AW job.
 */
class Axi4WJob(
  channel   : Axi4W,
  val data  : Seq[BigInt],
  val strb  : Seq[BigInt],
  parent: Axi4AWJob
) extends Axi4Job(
  delay = () => 20+simRandom.nextInt(20)
) {

  assert(
    assertion = (data.length == strb.length),
    message   = f"Data and strb length mismatch!"
  )

  /** Starts at one because var will be compared to data length during the creation of a last signals. */
  private var beat: Int = 0

  override def ready(): Boolean = {
    return parent.isDone() && super.ready()
  }

  override def isDone(): Boolean = {
    return (this.beat == this.data.length)
  }

  override def place(): Unit = {
    this.channel.data #= this.data(this.beat)
    if (this.channel.config.useStrb)
      this.channel.strb #= this.strb(this.beat)
    // Must be at the end to retrieve data correctly
    this.beat += 1
    if (this.channel.config.useLast)
      this.channel.last #= this.isDone()
  }

}

/** Specialized class if the Axi4 job for read data phase.
 *
 *  @constructor Creates a [[spinal.lib.bus.amba4.axi.Axi4]] job for the read data phase.
 *  @param channel [[spinal.lib.bus.amba4.axi.Axi4]] channel the [[Axi4RJob]] is associated to.
 *  @param id transaction ID.
 *  @param resp transaction response status (default [[Axi4Sim.resp.OKAY]]).
 */
class Axi4RJob(
  channel : Axi4R,
  val id  : Int = 0,
  val resp: Int = Axi4Sim.resp.OKAY
) extends Axi4Job(
  delay = () => 20+simRandom.nextInt(20)
) {
  
  /** 
   *  Queue storing data to be read in the form of function returning a 
   *  [[BigInt]]. Function was chosen over straight value such that data placed
   *  on bus in [[Axi4RJob.place]] is evaluated at the last momnent if 
   *  simulated target accept concurrency. The choice a using a queue is not 
   *  inocent; the order of insertion is the order of extraction.
   */
  private val data: mutable.Queue[() => BigInt] = new mutable.Queue[() => BigInt]()

  /** Creates a [[spinal.lib.bus.amba4.axi.Axi4]] job for read data phase by capturing the ID set on a 
   *  [[Axi4Ar]].
   *
   *  @param channel [[spinal.lib.bus.amba4.axi.Axi4]] channel the [[Axi4RJob]] is associated to.
   */
  def this(channel: Axi4R, context: Axi4Ar) = this(channel, context.id.toInt)

  /** Creates a [[spinal.lib.bus.amba4.axi.Axi4]] job for read data phase by capturing the content of 
   *  the [[Axi4R]] channel in simulation.
   *
   *  @param channel [[Axi4R]] channel the [[Axi4RJob]] is associated to.
   */
  def this(channel: Axi4R) = this(channel, channel.id.toInt)

  /**
   *  Enqueue burst beat data as a [[BigInt]] returning function.
   */
  def enqueue(func: () => BigInt): Unit = {
    this.data.enqueue(func)
  }

  /**
   *  Enqueue burst beat data as a fixed [[BigInt]].
   */
  def enqueue(value: BigInt): Unit = {
    this.enqueue(() => value)
  }

  override def isDone(): Boolean = {
    return this.data.isEmpty
  }

  override def place(): Unit = {
    this.channel.data #= (this.data.dequeue())()
    if (this.channel.config.useId)
      this.channel.id #= this.id
    if (this.channel.config.useResp)
      this.channel.resp #= this.resp
    if (this.channel.config.useLast)
      this.channel.last #= this.data.isEmpty
  }
}

/** Specialized class if the Axi4 job for read data phase.
 *
 *  @constructor Creates a [[spinal.lib.bus.amba4.axi.Axi4]] job for the write response phase.
 *  @param channel [[spinal.lib.bus.amba4.axi.Axi4]] channel the [[Axi4BJob]] is associated to.
 *  @param id transaction ID.
 *  @param resp transaction response status (default [[Axi4Sim.resp.OKAY]]).
 */
class Axi4BJob(
  channel : Axi4B,
  val id  : Int,
  val resp: Int
) extends Axi4Job() {

  /** 
   *  Creates a [[spinal.lib.bus.amba4.axi.Axi4]] job for hte write response phase.
   *  TODO: resp can be deduced from the [[Axi4AWJob]]!
   *
   *  @param channel [[spinal.lib.bus.amba4.axi.Axi4]] channel the [[Axi4BJob]] is associated to.
   *  @param job Original [[Axi4AWJob]] address phase to which the created 
   *  [[Axi4BJob]] corresponds.  
   */
  def this(channel: Axi4B, job: Axi4AWJob) = this(channel, job.id, Axi4Sim.resp.OKAY)

  override def place(): Unit = {
    if (this.channel.config.useId)
      this.channel.id #= id
    if (this.channel.config.useResp)
      this.channel.resp #= resp
    placed = true
  }

}


/** Specialized queue that filters out candidate jobs for scheduling and 
 *  updating transactions age.
 *
 *  @constructor Construct self managed queue.
 *  @param cd The associated clock domain.
 */
class Axi4JobQueue(
  cd: ClockDomain
) extends mutable.Queue[Axi4Job]() {

  /** Inidicate whether job candidate for scheduling exists in the queue.
   *
   *  @return status Yes/no.
   */
  def hasCandidate(): Boolean = {
    return super.nonEmpty && this.map(p => p.ready()).reduce(_ || _)
  }

  /** Request candidates for scheduling.
   *
   *  @return indexes List of indexes of job ready for selection.
   */
  def getCandidates(): Seq[Int] = {
    return this.zipWithIndex.filter(_._1.ready()).map(_._2).toSeq
  }

  // Every clock cycle, make job older
  cd.onRisingEdges({
    this.foreach({ p => p.makeOlder() })
  })

}


/** Specialized StreamMonitor that count all handshakes occuring on a given bus.
 *
 *  @constructor Requires a stream and a clock domain.
 *  @param stream Stream to be monitored.
 *  @param clockdomain Clock domain associated with the stream.
 */
class Axi4BeatCounter[T <: Data](stream: Stream[T], clockdomain: ClockDomain) extends StreamMonitor(stream, clockdomain) {

  /** Amount of handshake that occured ont he stream. */
  var count: Int = 0

  this.addCallback({ _ =>
    this.count += 1
  })

}

/** Construct monitoring amount of in flight transaction.
 *
 *  @constructor Create in-fligt monitor.
 *  @param increment Sequence ([[Seq]]) of streams for which the counter is incremented upon handshake.
 *  @param decrement Sequence([[Seq]]) of streams for whicht he counter is decremented upon handshake.
 *  @param clockdomain Clock domain associated with ALL stream (i.e., [[increment]] and [[decrement]])
 */
class Axi4InFlightCounter[T1 <: Data, T2 <: Data](increment: Seq[Stream[T1]], decrement: Seq[Stream[T2]], clockdomain: ClockDomain) {

  /** Amount of on-going (i.e., in-flight) transactions. */
  var count: Int = 0

  for (stream <- increment) {
    StreamMonitor(stream, clockdomain) { _ =>
      this.count += 1
    }
  }

  for (stream <- decrement) {
    StreamMonitor(stream, clockdomain) { _ =>
      this.count -= 1
    }
  }

}
