package ultrascaleplus.bus.amba.axi4.sim


import scala.math.pow
import scala.collection.mutable
import scala.collection.mutable.Map


import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.sim.{StreamDriver, StreamMonitor, StreamReadyRandomizer}
import spinal.lib.bus.amba4.axi._


class Axi4CheckerPrimary(axi: Axi4, clockDomain: ClockDomain) {

  // Maintaining clock count
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

  def getReadBandwidth(): Double = {
    return readByteCount.toDouble/(readEndTimeStamp-readStartTimeStamp).toDouble
  }

  def getWriteBandwidth(): Double = {
    return writeByteCount.toDouble/(writeEndTimeStamp-writeStartTimeStamp).toDouble
  }

  def allWritesCompleted(): Boolean = {
    return BMonitor.isEmpty && AWDriver.isDone()
  }

  def allReadsCompleted(): Boolean = {
    return RMonitor.isEmpty && ARDriver.isDone()
  }

  // Management methods

  def startRead(): Unit = {
    readStartTimeStamp = clockCount
  }

  def startWrite(): Unit = {
    writeStartTimeStamp = clockCount
  }

  def stopRead(): Unit = {
    readEndTimeStamp = clockCount
  }

  def stopWrite(): Unit = {
    writeEndTimeStamp = clockCount
  }

  def start(): Unit = {
    startRead()
    startWrite()
  }

  def resetRead(): Unit = {
    // Tracking
    readTransactionsCompleted = 0
    // statistics
    readStartTimeStamp = 0
    readEndTimeStamp = 0
    readByteCount = 0
  }

  def resetWrite(): Unit = {
    // Tracking
    writeTransactionsCompleted = 0
    // statistics
    writeStartTimeStamp = 0
    writeEndTimeStamp = 0
    writeByteCount = 0
  }

  def reset(): Unit = {
    resetRead()
    resetWrite()
  }

  // Transaction generation

  def genReadCmd(): Unit = {}
  
  def addRead(job: Axi4ARJob): Unit = {
    ARDriver.storage.enqueue(job)
  }

  def addWrite(addrJob: Axi4AWJob, dataJob: Axi4WJob): Unit = {
    AWDriver.storage.enqueue(addrJob)
    WDriver.storage.enqueue(dataJob)
  }
  
  // Self defined driver
  clockDomain.onRisingEdges({clockCount += 1})

  /** AXI AR
   * Increment read variable tracking count
   */
  StreamMonitor(axi.ar, clockDomain) { payload =>
    if (!(RMonitor contains payload.id.toInt)) {
      RMonitor += (payload.id.toInt -> 0)
    }
    RMonitor(payload.id.toInt) += 1
  }

  /** AXI R
   * Random drive for R ready signal
   */
  StreamReadyRandomizer(axi.r, clockDomain).setFactor(1.0f)

  /** AXI R
   *
   */
  StreamMonitor(axi.r, clockDomain) { payload =>
    // Maintain statistics variables
    readByteCount += axi.config.bytePerWord
    // When last beat
    if (axi.r.last.toBoolean) {
      readTransactionsCompleted += 1
      val job = new Axi4RJob(axi.r)
      // Expectation: there should be a pending transaction for that id
      assert(
        assertion = RMonitor(job.id) > 0,
        message   = f"No read response expcted for 0x${job.id.toHexString}"
      )
      RMonitor(job.id) -= 1
      if (RMonitor(job.id) == 0)
        RMonitor.remove(job.id)
      // Maintain tracking variable
      readTransactionsCompleted += 1
    }
  }

  /** AXI W
   *
   */
  
  /** AXI AW
   * Increment write variable tracking count
   */
  StreamMonitor(axi.aw, clockDomain) { payload =>
    if (!(BMonitor contains payload.id.toInt)) {
      BMonitor += (payload.id.toInt -> 0)
    }
    BMonitor(payload.id.toInt) += 1
  }

  StreamMonitor(axi.w, clockDomain) { payload =>
    // Maintain statistics variables
    // TODO: should count the strb content!
    writeByteCount += axi.config.bytePerWord
  }

  /** AXI B
   *
   */
  StreamReadyRandomizer(axi.b, clockDomain).setFactor(1.0f)

  /** AXI B
   *
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
