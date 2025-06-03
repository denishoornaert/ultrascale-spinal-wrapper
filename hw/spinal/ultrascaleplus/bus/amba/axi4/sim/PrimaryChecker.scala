package ultrascaleplus.bus.amba.axi4.sim


import scala.math.pow
import scala.collection.mutable
import scala.collection.mutable.Map


import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.sim.{StreamDriver, StreamMonitor, StreamReadyRandomizer}
import spinal.lib.bus.amba4.axi._


/** Class simulating an Axi4 primary actor. It handles: transaction scheduling,
 *  transaction emission, and basic protocol checks.
 *
 *  @constructor Creates an Axi4 primary checker/handler
 *  @param axi The [[spinal.lib.bus.amba4.axi.Axi4]] bus to handle and check.
 *  @param clockDomain The clock domain associated with the bus.
 */
class Axi4CheckerPrimary(axi: Axi4, clockDomain: ClockDomain) {

  /** Maintaining clock count of the simulated system. */
  private var clockCount: BigInt = 0

  // AR
  private val ARDriver = ChannelDriverInOrder(axi.ar, clockDomain)
  // R
  private val RMonitor: Map[Int, Int] = Map()
  // AW
  private val AWDriver = ChannelDriverInOrder(axi.aw, clockDomain)
  // W
  private val WDriver = ChannelDriverInOrder(axi.w, clockDomain) 
  // B
  private val BMonitor: Map[Int, Int] = Map()

  // Keep track of amount of transactions processed (i.e., emitted and served)
  private var readTransactionsCompleted: Int = 0
  private var writeTransactionsCompleted: Int = 0

  // Statistics variables and functions
  private var readStartTimeStamp: BigInt = 0
  private var readEndTimeStamp: BigInt = 0
  private var readByteCount: BigInt = 0
  private var writeStartTimeStamp: BigInt = 0
  private var writeEndTimeStamp: BigInt = 0
  private var writeByteCount: BigInt = 0

  /**
   *  Obtain current read bandwidth in byte per clock cycle (B/cc)
   *
   *  @return bandwidth The read bandwidth experienced by the primary.
   */
  def getReadBandwidth(): Double = {
    return readByteCount.toDouble/(readEndTimeStamp-readStartTimeStamp).toDouble
  }

  /**
   *  Obtain current write bandwidth in byte per clock cycle (B/cc)
   *
   *  @return bandwidth The write bandwidth experienced by the primary.
   */
  def getWriteBandwidth(): Double = {
    return writeByteCount.toDouble/(writeEndTimeStamp-writeStartTimeStamp).toDouble
  }

  /**
   *  Indicate whether there are any pending write transactions.
   *
   *  @return completed `true` if no write transactions are pending.
   */
  def allWritesCompleted(): Boolean = {
    return BMonitor.isEmpty && AWDriver.isDone()
  }

  /**
   *  Indicate whether there are any pending read transactions.
   *
   *  @return completed `true` if no read transactions are pending.
   */
  def allReadsCompleted(): Boolean = {
    return RMonitor.isEmpty && ARDriver.isDone()
  }

  /** Start timer for read bandwidth measurement. */
  def startRead(): Unit = {
    readStartTimeStamp = clockCount
  }

  /** Start timer for write bandwidth measurement. */
  def startWrite(): Unit = {
    writeStartTimeStamp = clockCount
  }

  /** Stop timer for read bandwidth measurement. */
  def stopRead(): Unit = {
    readEndTimeStamp = clockCount
  }

  /** Stop timer for write bandwidth measurement. */
  def stopWrite(): Unit = {
    writeEndTimeStamp = clockCount
  }

  /** Start timer for read and write bandwidth measurements. */
  def start(): Unit = {
    startRead()
    startWrite()
  }

  /** Reset read bandwidth measurements variables */
  def resetRead(): Unit = {
    // Tracking
    readTransactionsCompleted = 0
    // statistics
    readStartTimeStamp = 0
    readEndTimeStamp = 0
    readByteCount = 0
  }

  /** Reset write bandwidth measurements variables */
  def resetWrite(): Unit = {
    // Tracking
    writeTransactionsCompleted = 0
    // statistics
    writeStartTimeStamp = 0
    writeEndTimeStamp = 0
    writeByteCount = 0
  }

  /** Reset read and write bandwidth measurements variables */
  def reset(): Unit = {
    resetRead()
    resetWrite()
  }

  /** 
   *  Enqueue new read job to be placed on the bus and completed.
   *
   *  @param job The [[Axi4ARJob]] describing the read transaction to be 
   *  performed.
   */
  def addRead(job: Axi4ARJob): Unit = {
    ARDriver.storage.enqueue(job)
  }

  /** 
   *  Enqueue new write job to be placed on the bus and completed.
   *
   *  @param addrJob The [[Axi4AWJob]] describing the address phase of the 
   *  write transaction to be performed.
   *  @param dataJob The [[Axi4WJob]] describing the data phase of the write 
   *  transaction to be performed.
   */
  def addWrite(addrJob: Axi4AWJob, dataJob: Axi4WJob): Unit = {
    AWDriver.storage.enqueue(addrJob)
    WDriver.storage.enqueue(dataJob)
  }
  
  // Self defined driver
  clockDomain.onRisingEdges({clockCount += 1})

  /** AXI AR: Increment read variable tracking the transaction count.
   *
   *  Add read transaction ID for monitoring and check of existance during the 
   *  response phase.
   */
  StreamMonitor(axi.ar, clockDomain) { payload =>
    if (!(RMonitor contains payload.id.toInt)) {
      RMonitor += (payload.id.toInt -> 0)
    }
    RMonitor(payload.id.toInt) += 1
  }

  /** AXI R: Random drive for R ready signal. */
  StreamReadyRandomizer(axi.r, clockDomain).setFactor(1.0f)

  /** AXI R: Check whether read data/response ID is in the monitoring list. 
   *
   *  Handles monitoring of data and repsonse read phases. Checks if ID for 
   *  transaction exists and, for last beat of burst, removes the ID entry.
   *  Upon mismatch (i.e., response ID not in ID container), an error is 
   *  thrown.
   */
  StreamMonitor(axi.r, clockDomain) { payload =>
    // Maintain statistics variables
    readByteCount += axi.config.bytePerWord
    // Expectation: there should be a pending transaction for that id
    val job = new Axi4RJob(axi.r)
    assert(
      assertion = RMonitor(job.id) > 0,
      message   = f"No read response expcted for 0x${job.id.toHexString}"
    )
    // When last beat
    if (payload.last.toBoolean) {
      readTransactionsCompleted += 1
      RMonitor(job.id) -= 1
      if (RMonitor(job.id) == 0)
        RMonitor.remove(job.id)
      // Maintain tracking variable
      readTransactionsCompleted += 1
    }
  }

  /** AXI AW: Increment write variable tracking count.
   *
   *  Add write transaction ID for monitoring and check of existance during the 
   *  response phase.
   */
  StreamMonitor(axi.aw, clockDomain) { payload =>
    if (!(BMonitor contains payload.id.toInt)) {
      BMonitor += (payload.id.toInt -> 0)
    }
    BMonitor(payload.id.toInt) += 1
  }

  /** AXI W: Keeps track of amount of bytes written in every burst beats. */
  StreamMonitor(axi.w, clockDomain) { payload =>
    // Maintain statistics variables
    // TODO: should count the strb content!
    writeByteCount += axi.config.bytePerWord
  }

  /** AXI R: Random drive for R ready signal. */
  StreamReadyRandomizer(axi.b, clockDomain).setFactor(1.0f)

  /** AXI B
   *
   *  Handles monitoring of repsonse write phases. Checks if ID for transaction
   *  exists and removes the ID entry. Upon mismatch (i.e., response ID not in 
   *  ID container), an error is thrown.
   */
  StreamMonitor(axi.b, clockDomain) { payload =>
    assert(
      assertion = BMonitor(payload.id.toInt) > 0,
      message   = f"No write response expected for ${payload.id.toInt}"
    )
    BMonitor(payload.id.toInt) -= 1
    if (BMonitor(payload.id.toInt) == 0) {
      BMonitor.remove(payload.id.toInt)
    }
    // Maintain tracking variable
    writeTransactionsCompleted += 1
  }
}
