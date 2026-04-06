package ultrascaleplus.bus.amba.ace

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


case class AceAc(config: AceConfig) extends Bundle with IMasterSlave {

  val addr  = in(UInt(config.addressWidth bits))
  val snoop = in(UInt(4 bits))
  val prot  = in(Bits(3 bits))

  override def clone: this.type = new AceAc(config).asInstanceOf[this.type]

  override def asMaster(): Unit = {
    out(addr, snoop, prot)
  }

}


case class AceCr(config: AceConfig) extends Bundle with IMasterSlave {

  val resp = in(UInt(5 bits))

  override def clone: this.type = new AceCr(config).asInstanceOf[this.type]

  override def asMaster(): Unit = {
    out(resp)
  }

}


case class AceCd(config: AceConfig) extends Bundle with IMasterSlave {

  val data = in(Bits(config.dataWidth bits))

  override def clone: this.type = new AceCd(config).asInstanceOf[this.type]

  override def asMaster(): Unit = {
    out(data)
  }

}


class AceAx(config: AceConfig, val userWidth: Int, readOnly: Boolean) extends Bundle with IMasterSlave {

  val addr    = in(UInt(config.addressWidth bits))
  val id      = (config.useId)     generate in(UInt(config.idWidth bits))
  val region  = (config.useRegion) generate in(Bits(4 bits))
  val len     = (config.useLen)    generate in(UInt(config.lenWidth bits))
  val size    = (config.useSize)   generate in(UInt(config.sizeWidth bits))
  val burst   = (config.useBurst)  generate in(Bits(2 bits))
  val lock    = (config.useLock)   generate in(Bits(config.lockWidth bits))
  val cache   = (config.useCache)  generate in(Bits(4 bits))
  val qos     = (config.useQos)    generate in(Bits(4 bits))
  val user    = (userWidth >= 0)   generate in(Bits(userWidth bits))
  val prot    = (config.useProt)   generate in(Bits(3 bits))

  val domain  = (config.useDomain) generate in(Bits(2 bits))
	val snoop   = (config.useSnoop)  generate in(Bits(3+readOnly.toInt bits))
	val bar     = (config.useBar)    generate in(Bits(2 bits))

  import Axi4.burst._

  def setBurstFIXED(): Unit = {assert(config.useBurst); burst := FIXED}
  def setBurstWRAP() : Unit = {assert(config.useBurst); burst := WRAP}
  def setBurstINCR() : Unit = {assert(config.useBurst); burst := INCR}

  def isINCR() = if(config.useBurst) burst === INCR else True
  def isFIXED() = if(config.useBurst) burst === FIXED else False

  def setSize(sizeBurst :UInt) : Unit = if(config.useBurst) size := sizeBurst
  def setFullSize() : Unit = this.setSize(log2Up(config.dataWidth/8))
  def setLock(lockType :Bits) : Unit = if(config.useLock) lock := lockType
  def setCache(cacheType : Bits) : Unit = if (config.useCache ) cache := cacheType
  def setQos(qosType : Bits) : Unit = if (config.useQos) qos := qosType
  def setProt(protType : Bits) : Unit = if (config.useProt) prot := protType

	override def clone: this.type = new AceAx(config, userWidth, readOnly).asInstanceOf[this.type]
  
  def getLenOnDataWidth(dataWidth : Int): UInt ={
    assert(dataWidth > config.dataWidth)
    val byteCount = (len << size).resize(8+log2Up(config.bytePerWord) bits)
    val incrLen = ((U"0" @@ byteCount) + addr(log2Up(dataWidth/8)-1 downto 0))(byteCount.high + 1 downto log2Up(dataWidth/8))
    incrLen
  }

  def getAddrSizeMaskedLow(): UInt = {
    val width = log2Up(config.bytePerWord)
    return addr.resize(width bits) & ((U(1) << size)-1).resize(width bits)
  }

  def getFirstBeatBytesMinusOne(): UInt = {
    return ((U(1) << size) - 1 - getAddrSizeMaskedLow()).resize(log2Up(config.bytePerWord))
  }

  def getBurstBytesMinusOne(boundaryWidth : Int = Axi4.boundaryWidth): UInt = {
    return (len << size).resize(boundaryWidth) + getFirstBeatBytesMinusOne()
  }

  def getLenAlignedAddr() : UInt = {
    addr & size.muxList(
      (default -> U(config.addressWidth bits, default -> true)) ::
      (1 to log2Up(config.bytePerWord)).map(l =>
        l -> U(config.addressWidth bits, default -> true, (l-1 downto 0) -> false)
      ).toList
    )
  }

  override def asMaster(): Unit = {
    out(addr, id, region, len, size, burst, lock, cache, qos, user,prot, domain, snoop, bar)
  }

}


class AceAw(config: AceConfig) extends AceAx(config, config.awUserWidth, false) {

  val unique  = (config.useUnique) generate in(Bool())

  override def clone: this.type = new AceAw(config).asInstanceOf[this.type]

  override def asMaster(): Unit = {
    super.asMaster()
    out(unique)
  }

}

object AceAw {

  def apply(config: AceConfig) = new AceAw(config)

}


class AceAr(config: AceConfig) extends AceAx(config, config.arUserWidth, true) {

  override def clone: this.type = new AceAr(config).asInstanceOf[this.type]

}

object AceAr {

  def apply(config: AceConfig) = new AceAr(config)

}


class AceW(config: AceConfig) extends Bundle with IMasterSlave {

  val data = in(Bits(config.dataWidth bits))
  val id   = in(UInt(config.idWidth bits))
  val strb = (config.useStrb)  generate  in(Bits(config.bytePerWord bits))
  val user = (config.useWUser) generate  in(Bits(config.wUserWidth bits))
  val last = (config.useLast)  generate  in(Bool())
  val ack  = (config.useAck)   generate out(Bool())

  def setStrb(): Unit = {
    if(config.useStrb) strb := (1 << widthOf(strb))-1
  }
  def setStrb(bytesLane: Bits): Unit = {
    if(config.useStrb) strb := bytesLane
  }

  override def asMaster(): Unit = {
    out(data, strb, user, last, id)
    in(ack)
  }

}

object AceW {

  def apply(config: AceConfig) = new AceW(config)

}


class AceB(config: AceConfig) extends Bundle with IMasterSlave {

  val id   = (config.useId)    generate in(UInt(config.idWidth bits))
  val resp = (config.useResp)  generate in(Bits(2 bits))
  val user = (config.useBUser) generate in(Bits(config.bUserWidth bits))

  import Axi4.resp._

  def setOKAY()   : Unit = resp := OKAY
  def setEXOKAY() : Unit = resp := EXOKAY
  def setSLVERR() : Unit = resp := SLVERR
  def setDECERR() : Unit = resp := DECERR
  def isOKAY()   : Bool = resp === OKAY
  def isEXOKAY() : Bool = resp === EXOKAY
  def isSLVERR() : Bool = resp === SLVERR
  def isDECERR() : Bool = resp === DECERR

  override def asMaster(): Unit = {
    out(id, resp, user)
  }

}

object AceB {

  def apply(config: AceConfig) = new AceB(config)

}


class AceR(config: AceConfig) extends Bundle with IMasterSlave {

  val data = in(Bits(config.dataWidth bits))
  val id   = (config.useId)    generate  in(UInt(config.idWidth bits))
  val resp = (config.useResp)  generate  in(Bits(2 bits))
  val last = (config.useLast)  generate  in(Bool())
  val user = (config.useRUser) generate  in(Bits(config.rUserWidth bits))
  val ack  = (config.useAck)   generate out(Bool())

  import Axi4.resp._

  def setOKAY()   : Unit = resp := OKAY
  def setEXOKAY() : Unit = resp := EXOKAY
  def setSLVERR() : Unit = resp := SLVERR
  def setDECERR() : Unit = resp := DECERR
  def isOKAY()   : Bool = resp === OKAY
  def isEXOKAY() : Bool = resp === EXOKAY
  def isSLVERR() : Bool = resp === SLVERR
  def isDECERR() : Bool = resp === DECERR

  override def asMaster(): Unit = {
    out(data, id, resp, last, user)
    in(ack)
  }

}

object AceR {

  def apply(config: AceConfig) = new AceR(config)

}
