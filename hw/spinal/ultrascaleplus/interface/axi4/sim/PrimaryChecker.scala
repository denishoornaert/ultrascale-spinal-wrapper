package ultrascaleplus.interface.axi.sim


import scala.math.pow

import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.bus.amba4.axi._
import scala.collection.mutable
import scala.collection.mutable.Map


class Axi4CheckerPrimary(axi: Axi4, clockDomain: ClockDomain) {

  // Self defined driver
  clockDomain.onRisingEdges({this.check()})

  // Define default values for signals
  // Read - Address phase
  axi.ar.valid  #= false
  axi.ar.addr   #= 0
  axi.ar.id     #= 0
  axi.ar.burst  #= 0
  axi.ar.len    #= 0
  axi.ar.size   #= 0
  axi.ar.cache  #= 0
  axi.ar.lock   #= 0
  axi.ar.prot   #= 0
  axi.ar.qos    #= 0
  axi.ar.region #= 0
  // Read - Data phase
  axi.r.ready   #= false
  // Write - Address phase
  axi.aw.valid  #= false
  axi.aw.addr   #= 0
  axi.aw.id     #= 0
  axi.aw.burst  #= 0
  axi.aw.len    #= 0
  axi.aw.size   #= 0
  axi.aw.cache  #= 0
  axi.aw.lock   #= 0
  axi.aw.prot   #= 0
  axi.aw.qos    #= 0
  axi.aw.region #= 0
  // Write - Data phase
  axi.w.valid   #= false
  axi.w.data    #= 0
  axi.w.strb    #= 0
  axi.w.last    #= false

  // Maintaining clock count
  var clockCount: BigInt = 0

  // AR
  val maxReads = 8
  var readCount = 0
  val ARQueue = mutable.Queue[Axi4RJob]()
  // R
  val RQueue = mutable.Queue[Int]()
  val RMonitor: Map[Int, Int] = Map()
  var RespData = List[BigInt]() // for storing responses
  def readDataAssertionFunction(id: Int, data: List[BigInt]): Unit = {}
  // AW
  val AWQueue = new mutable.Queue[Axi4WJob]()
  // W
  val WQueue = new mutable.Queue[Axi4WJob]()
  // B
  val BMonitor: Map[Int, Int] = Map()

  var wBeatCount = 0

  // Keep track of amount of transactions processed (i.e., emitted and served)
  var readTransactionsCompleted: Int = 0
  val readTransactionsCap: Int = 0
  var writeTransactionsCompleted: Int = 0
  val writeTransactionsCap: Int = 0

  // Statistics variables and functions
  var readStartTimeStamp: BigInt = 0
  var readEndTimeStamp: BigInt = 0
  var readByteCount: BigInt = 0
  var writeStartTimeStamp: BigInt = 0
  var writeEndTimeStamp: BigInt = 0
  var writeByteCount: BigInt = 0

  def getReadBandwidth(): Double = {
    return readByteCount.toDouble/(readEndTimeStamp-readStartTimeStamp).toDouble
  }

  def getWriteBandwidth(): Double = {
    return writeByteCount.toDouble/(writeEndTimeStamp-writeStartTimeStamp).toDouble
  }

  // Management methods
  var readInProgress: Boolean = false
  var writeInProgress: Boolean = false

  def startRead(): Unit = {
    readInProgress = true
    readStartTimeStamp = clockCount
  }

  def startWrite(): Unit = {
    writeInProgress = true
    writeStartTimeStamp = clockCount
  }

  def start(): Unit = {
    startRead()
    startWrite()
  }

  def resetRead(): Unit = {
    // Tracking
    readInProgress = false
    readTransactionsCompleted = 0
    // statistics
    readStartTimeStamp = 0
    readEndTimeStamp = 0
    readByteCount = 0
  }

  def resetWrite(): Unit = {
    // Tracking
    writeInProgress = false
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

  def waitForAllReadCompleted(): Unit = {
    clockDomain.waitRisingEdgeWhere(readTransactionsCompleted == readTransactionsCap)
    readEndTimeStamp = clockCount
  }

  def waitForAllWriteCompleted(): Unit = {
    clockDomain.waitRisingEdgeWhere(writeTransactionsCompleted == writeTransactionsCap)
    writeEndTimeStamp = clockCount
  }

  // Transaction generation

  def genReadCmd(): Unit = {}
  
  def addRead(job: Axi4RJob): Unit = {
    ARQueue += job
  }

  def genWriteCmd(): Unit = {}

  def addWrite(job: Axi4WJob): Unit = {
    AWQueue += job
    WQueue += job
  }
  
  def updateAR(): Unit = {
    if (ARQueue.nonEmpty) {
      val context = ARQueue.front
      axi.ar.valid  #= true
      axi.ar.addr   #= context.addr
      axi.ar.id     #= context.id
      axi.ar.burst  #= context.burst
      axi.ar.len    #= context.len
      axi.ar.size   #= context.size
      axi.ar.cache  #= context.cache
      axi.ar.lock   #= context.lock
      axi.ar.prot   #= context.prot
      axi.ar.qos    #= context.qos
      axi.ar.region #= context.region
    }
    else {
      axi.ar.valid  #= false
      axi.ar.addr   #= 0
      axi.ar.id     #= 0
      axi.ar.burst  #= 0
      axi.ar.len    #= 0
      axi.ar.size   #= 0
      axi.ar.cache  #= 0
      axi.ar.lock   #= 0
      axi.ar.prot   #= 0
      axi.ar.qos    #= 0
      axi.ar.region #= 0
    }
  }

  def updateR(): Unit = {
    axi.r.ready #= (if (axi.r.valid.toBoolean & axi.r.ready.toBoolean & axi.r.last.toBoolean) false else true)
  }
  
  def updateAW(): Unit = {
    if (AWQueue.nonEmpty) {
      val context = AWQueue.front
      axi.aw.valid  #= true
      axi.aw.addr   #= context.addr
      axi.aw.id     #= context.id
      axi.aw.burst  #= context.burst
      axi.aw.len    #= context.len
      axi.aw.size   #= context.size
      axi.aw.cache  #= context.cache
      axi.aw.lock   #= context.lock
      axi.aw.prot   #= context.prot
      axi.aw.qos    #= context.qos
      axi.aw.region #= context.region
    }
    else {
      axi.aw.valid  #= false
      axi.aw.addr   #= 0
      axi.aw.id     #= 0
      axi.aw.burst  #= 0
      axi.aw.len    #= 0
      axi.aw.size   #= 0
      axi.aw.cache  #= 0
      axi.aw.lock   #= 0
      axi.aw.prot   #= 0
      axi.aw.qos    #= 0
      axi.aw.region #= 0
    }
  }

  def updateW(): Unit = {
    if (WQueue.nonEmpty) {
      val context = WQueue.front
      val beatOffset = (context.actual_addr%axi.config.bytePerWord).toInt
      axi.w.valid #= true
      axi.w.data  #= context.data(wBeatCount) << (beatOffset*8) // 8 is 1 byte
      axi.w.strb  #= (if (context.size > 0) ((pow(2, context.size).toInt-1)) else 1) << beatOffset
      if (wBeatCount == context.len) {
        axi.w.last #= true
      }
      else {
        axi.w.last #= false
      }
    }
    else {
      axi.w.valid #= false
      axi.w.data  #= 0
      axi.w.strb  #= 0
      axi.w.last  #= false
    }
  }

  def updateB(): Unit = {
    axi.b.ready #= true
  }
  
  def onARHandshake(): Unit = {
    ARQueue.dequeue
    RQueue += axi.ar.len.toInt
    if (!(RMonitor contains axi.ar.id.toInt)) {
      RMonitor += (axi.ar.id.toInt -> 0)
    }
    RMonitor(axi.ar.id.toInt) += 1
  }

  def onRHandshake(): Unit = {
    // Add beat data to temporary structure
    RespData = RespData :+ axi.r.data.toBigInt
    // Maintain statistics variables
    readByteCount += axi.config.bytePerWord
    // When last beat
    if (axi.r.last.toBoolean) {
      // Expectation: there should be a pending transaction for that id
      assert(
        assertion = RMonitor(axi.r.id.toInt) > 0,
        message   = f"No read response expcted for 0x${axi.r.id.toInt.toHexString}"
      )
      RMonitor(axi.r.id.toInt) -= 1
      // Call data check function on reversed list
      readDataAssertionFunction(axi.r.id.toInt, RespData)
      // Clear resp data
      RespData = List[BigInt]() 
      // Maintain tracking variable
      readTransactionsCompleted += 1
    }
  }

  def onAWHandshake(): Unit = {
    //WQueue += 
    AWQueue.dequeue
    if (!(BMonitor contains axi.aw.id.toInt)) {
      BMonitor += (axi.aw.id.toInt -> 0)
    }
    BMonitor(axi.aw.id.toInt) += 1
  }

  def onWHandshake(): Unit = {
    if (axi.w.last.toBoolean) {
      WQueue.dequeue()
      wBeatCount = 0
    }
    else {
      wBeatCount += 1
    }
    // Maintain statistics variables
    writeByteCount += axi.config.bytePerWord
  }

  def onBHandshake(): Unit = {
    assert(
      assertion = BMonitor(axi.b.id.toInt) > 0,
      message   = f"No write response expected for ${axi.b.id.toInt}"
    )
    BMonitor(axi.b.id.toInt) -= 1
    // Maintain tracking variable
    writeTransactionsCompleted += 1
  }

  def check(): Unit = {
    // Maintain 
    clockCount += 1
    if (readInProgress)
      genReadCmd()
    if(writeInProgress)
      genWriteCmd()
    // Check channels
    if (ARQueue.nonEmpty & axi.ar.valid.toBoolean & axi.ar.ready.toBoolean)
      onARHandshake()
    updateAR()
    if (axi.r.valid.toBoolean & axi.r.ready.toBoolean)
      onRHandshake()
    updateR()
    if (AWQueue.nonEmpty & axi.aw.valid.toBoolean & axi.aw.ready.toBoolean)
      onAWHandshake()
    updateAW()
    if (WQueue.nonEmpty & axi.w.valid.toBoolean & axi.w.ready.toBoolean)
      onWHandshake()
    updateW()
    if (axi.b.valid.toBoolean & axi.b.ready.toBoolean)
      onBHandshake()
    updateB()
  }

}
