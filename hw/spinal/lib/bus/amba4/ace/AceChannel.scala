/**
 * Author:    Francesco Ciraolo
 * Created:   13.10.2025
 * 
 * Placeholder for AceChannel.scala as a interface with an ad-hoc WIP library
 * 
 * Note: some API could change
 * 
 **/

package spinal.lib.bus.amba4.ace

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

class AceAc(val config: AceConfig) extends Bundle {
    val addr    = UInt(config.aceAddressWidth bits)
    val snoop   = Bits(4 bits)
    val prot    = Bits(3 bits)

    def setReadOnce()           : Unit = snoop := 0b0000
    def setReadShared()         : Unit = snoop := 0b0001
    def setReadClean()          : Unit = snoop := 0b0010
    def setReadNotSharedDirty() : Unit = snoop := 0b0011
    def setReadUnique()         : Unit = snoop := 0b0111
    def setCleanShared()        : Unit = snoop := 0b1000
    def setCleanInvalid()       : Unit = snoop := 0b1001
    def setMakeInvalid()        : Unit = snoop := 0b1101
    def setDVMComplete()        : Unit = snoop := 0b1110
    def setDVMMessage()         : Unit = snoop := 0b1111

    def isReadOnce()            : Bool = snoop === 0b0000
    def isReadShared()          : Bool = snoop === 0b0001
    def isReadClean()           : Bool = snoop === 0b0010
    def isReadNotSharedDirty()  : Bool = snoop === 0b0011
    def isReadUnique()          : Bool = snoop === 0b0111
    def isCleanShared()         : Bool = snoop === 0b1000
    def isCleanInvalid()        : Bool = snoop === 0b1001
    def isMakeInvalid()         : Bool = snoop === 0b1101
    def isDVMComplete()         : Bool = snoop === 0b1110
    def isDVMMessage()          : Bool = snoop === 0b1111

    /* Manual IHI0022H pages D13-327,D13-328 */
    def dvmRequiresCompletion() : Bool = addr(15)
    def dvmHasTwoParts()        : Bool = addr(0)

    def isRead()                : Bool = snoop.msb === False
    def isCacheMaintenance()    : Bool = snoop.msb === True && snoop(1) === False
    def isDVM()                 : Bool = isDVMComplete() || isDVMMessage()

    override def clone: this.type = new AceAc(config).asInstanceOf[this.type]
}

class AceCr(val config: AceConfig) extends Bundle {
    val resp  = Bits(5 bits)

    def setDataTransfer(dataTransfer: Bool) : Unit = resp(0) := dataTransfer
    def setError(error: Bool)               : Unit = resp(1) := error
    def setPassDirty(passDirty: Bool)       : Unit = resp(2) := passDirty
    def setIsShared(isShared: Bool)         : Unit = resp(3) := isShared
    def setWasUnique(wasUnique: Bool)       : Unit = resp(4) := wasUnique
    def setMiss()                           : Unit = resp := 0b00000
    def setError()                          : Unit = resp := 0b00010
    def setHit(passDirty: Bool = False, 
                isShared: Bool = False, 
                wasUnique: Bool = False)    : Unit = resp := wasUnique ## isShared ## passDirty ## 0b01

    def isDataTransfer()                    : Bool = resp(0)
    def isError()                           : Bool = resp(1)
    def isPassDirty()                       : Bool = resp(2)
    def isIsShared()                        : Bool = resp(3)
    def isWasUnique()                       : Bool = resp(4)
    

    override def clone: this.type = new AceCr(config).asInstanceOf[this.type]
    
}

class AceCd(val config: AceConfig) extends Bundle {
    val data = Bits(config.dataWidth bits)
    val last = Bool()


    override def clone: this.type = new AceCd(config).asInstanceOf[this.type]
}

/**
 * Definition of the Write/Read address channel
 * @param config Axi4 configuration class
 */
class AceAx(val config: AceConfig, val userWidth : Int, isRead : Boolean) extends Bundle {
    val addr   = UInt(config.addressWidth bits)
    val id     = if(config.useId)     UInt(config.idWidth bits)   else null
    val region = if(config.useRegion) Bits(4 bits)                else null
    val len    = if(config.useLen)    UInt(config.lenWidth bits) else null
    val size   = if(config.useSize)   UInt(config.sizeWidth bits)                else null
    val burst  = if(config.useBurst)  Bits(2 bits)                else null
    val lock   = if(config.useLock)   Bits(config.lockWidth bits)                else null
    val cache  = if(config.useCache)  Bits(4 bits)                else null
    val qos    = if(config.useQos)    Bits(4 bits)                else null
    val user   = if(userWidth >= 0)   Bits(userWidth bits)        else null
    val prot   = if(config.useProt)   Bits(3 bits)                else null
    val allStrb = if(config.useAllStrb && !isRead) Bool()       else null
    
    val domain  = if (config.useDomain) Bits(2 bits)            else null
    val snoop   = if (config.useSnoop)  Bits((if (!isRead) 3 else 4) bits)   else null
    val bar     = if (config.useBar)    Bits(2 bits)            else null

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

  override def clone: this.type = new AceAx(config, userWidth, isRead).asInstanceOf[this.type]

  def getLenOnDataWidth(dataWidth : Int ): UInt ={
    assert(dataWidth > config.dataWidth)
    val byteCount = (len << size).resize(8+log2Up(config.bytePerWord) bits)
    val incrLen = ((U"0" @@ byteCount) + addr(log2Up(dataWidth/8)-1 downto 0))(byteCount.high + 1 downto log2Up(dataWidth/8))
    incrLen
  }
  def getLenAlignedAddr() : UInt = {
    addr & size.muxList(
      (default -> U(config.addressWidth bits, default -> true)) ::
      (1 to log2Up(config.bytePerWord)).map(l =>
        l -> U(config.addressWidth bits, default -> true, (l-1 downto 0) -> false)
      ).toList
    )
  }
}

class AceAw(config: AceConfig) extends AceAx(config, config.awUserWidth, false) {

    val unique  = if (config.hasAWUnique) Bool() else null

    override def clone: this.type = new AceAw(config).asInstanceOf[this.type]
    
}

object AceAw {
    def apply(config: AceConfig) = new AceAw(config)

    implicit class StreamPimper(source: Stream[AceAw]) {

        def drive(sink: Stream[AceAw]): Unit = AcePriv.driveAx(source, sink)
    }
}

class AceAr(config: AceConfig) extends AceAx(config, config.arUserWidth, true) {

    override def clone: this.type = new AceAr(config).asInstanceOf[this.type]

}

object AceAr {
    def apply(config: AceConfig) = new AceAr(config)

    implicit class StreamPimper(source: Stream[AceAr]) {

        def drive(sink: Stream[AceAr]): Unit = AcePriv.driveAx(source, sink)
    }
}

case class AceW(val config: AceConfig) extends Bundle {
  val data = Bits(config.dataWidth bits)
  val strb = if(config.useStrb)  Bits(config.bytePerWord bits) else null
  val user = if(config.useWUser) Bits(config.wUserWidth bits)  else null
  val last = if(config.useLast)  Bool()                        else null

  def setStrb() : Unit = if(config.useStrb) strb := (1 << widthOf(strb))-1
  def setStrb(bytesLane : Bits) : Unit = if(config.useStrb) strb := bytesLane
}

case class AceB(val config: AceConfig) extends Bundle {
  val id   = if(config.useId)    UInt(config.idWidth bits)    else null
  val resp = if(config.useResp)  Bits(2 bits)                 else null
  val user = if(config.useBUser) Bits(config.bUserWidth bits) else null

  import Axi4.resp._

  def setOKAY()   : Unit = resp := OKAY
  def setEXOKAY() : Unit = resp := EXOKAY
  def setSLVERR() : Unit = resp := SLVERR
  def setDECERR() : Unit = resp := DECERR
  def isOKAY()   : Bool = resp === OKAY
  def isEXOKAY() : Bool = resp === EXOKAY
  def isSLVERR() : Bool = resp === SLVERR
  def isDECERR() : Bool = resp === DECERR

}

object AceW {
  implicit class StreamPimper(stream : Stream[AceW]) {
    def drive(sink: Stream[AceW]): Unit = {
      sink.arbitrationFrom(stream)
      sink.data := stream.data
      AcePriv.driveWeak(stream,sink,stream.strb,sink.strb,() => B(sink.strb.range -> true),false,false)
      AcePriv.driveWeak(stream,sink,stream.user,sink.user,() => B(sink.user.range -> false),false,true)
      AcePriv.driveWeak(stream,sink,stream.last,sink.last,null,false,true)
    }
  }
}

object AceB {
  implicit class StreamPimper(stream : Stream[AceB]) {
    def drive(sink: Stream[AceB]): Unit = {
      assert(stream.config.idWidth >= sink.config.idWidth, s"Expect $stream idWidth=${stream.config.idWidth} >= $sink idWidth=${sink.config.idWidth}")
      sink.arbitrationFrom(stream)

      AcePriv.driveWeak(stream,sink,stream.id,sink.id,null,true,true)
      AcePriv.driveWeak(stream,sink,stream.resp,sink.resp,() => Axi4.resp.OKAY,false,true)
      AcePriv.driveWeak(stream,sink,stream.user,sink.user,() => B(sink.user.range -> false),false,true)
    }
  }
}


case class AceR(val config: AceConfig) extends Bundle {

    val data = Bits(config.dataWidth bits)
    val id   = if(config.useId)    UInt(config.idWidth bits)    else null
    val resp = if(config.useResp)  Bits(2 bits)                 else null
    val last = if(config.useLast)  Bool()                       else null
    val user = if(config.useRUser) Bits(config.rUserWidth bits) else null

    import Axi4.resp._

    def setOKAY()   : Unit = resp := OKAY
    def setEXOKAY() : Unit = resp := EXOKAY
    def setSLVERR() : Unit = resp := SLVERR
    def setDECERR() : Unit = resp := DECERR
    def isOKAY()   : Bool = resp === OKAY
    def isEXOKAY() : Bool = resp === EXOKAY
    def isSLVERR() : Bool = resp === SLVERR
    def isDECERR() : Bool = resp === DECERR

    def setPassDirty(passDirty: Bool) : Unit = resp(2) := passDirty
    def setIsShared(isShared: Bool) : Unit = resp(3) := isShared

    def isPassDirty() : Bool = resp(2)
    def isIsShared() : Bool = resp(3)

}

object AceR {

  def apply(config: AceConfig): AceR = new AceR(config)

  implicit class StreamPimper(stream : Stream[AceR]) {
    def drive(sink: Stream[AceR]): Unit = {
      assert(stream.config.idWidth >= sink.config.idWidth, s"Expect $stream idWidth=${stream.config.idWidth} >= $sink idWidth=${sink.config.idWidth}")

      sink.arbitrationFrom(stream)
      sink.data := stream.data
      AcePriv.driveWeak(stream,sink,stream.last,sink.last,null,false,true)
      AcePriv.driveWeak(stream,sink,stream.id,sink.id,null,true,true)
      AcePriv.driveWeak(stream,sink,stream.resp,sink.resp,() => Axi4.resp.OKAY,false,true)
      AcePriv.driveWeak(stream,sink,stream.user,sink.user,() => B(sink.user.range -> false),false,true)
    }
  }
}

object AcePriv {
    
    def driveWeak[T <: Data](
        source: Bundle,
        sink: Bundle,
        by: T,
        to: T,
        defaultValue: () => T,
        allowResize: Boolean,
        allowDrop: Boolean): Unit = {
            (by != null, to != null) match {
                case (false, false) =>
                case (false, true) => if (defaultValue != null) to := defaultValue() else LocatedPendingError(s"$source can't drive $to because this first doesn't has the corresponding pin")
                case (true, false) => if (!allowDrop) LocatedPendingError(s"$by can't drive $sink because this last one doesn't has the corresponding pin")
                case (true, true) => to := (if (allowResize) by.resized else by)
            }
    }

    def driveAx[T <: AceAx](source: Stream[T], sink: Stream[T]): Unit = {
        sink.arbitrationFrom(source)
        assert(source.config.idWidth <= sink.config.idWidth, s"Expect $source idWidth=${source.config.idWidth} <= $sink idWidth=${sink.config.idWidth}")
        assert(source.config.addressWidth >= sink.config.addressWidth, s"Expect $source addressWidth=${source.config.addressWidth} >= $sink addressWidth=${sink.config.addressWidth}")

        sink.addr := source.addr.resized
        driveWeak(source,sink,source.id,sink.id,() => U(sink.id.range -> false),true,false)
        driveWeak(source,sink,source.region,sink.region,() => B(sink.region.range -> false),false,true)
        driveWeak(source,sink,source.len,sink.len,() => U(sink.len.range -> false),false,false)
        driveWeak(source,sink,source.size,sink.size,() => U(log2Up(sink.config.dataWidth/8)),false,false)
        driveWeak(source,sink,source.burst,sink.burst,() => Axi4.burst.INCR,false,false)
        driveWeak(source,sink,source.lock,sink.lock,() => Axi4.lock.NORMAL,false,true)
        driveWeak(source,sink,source.cache,sink.cache,() => B"0000",false,true)
        driveWeak(source,sink,source.qos,sink.qos,() => B"0000",false,true)
        driveWeak(source,sink,source.user,sink.user,() => B(sink.user.range -> false),true,true)
        driveWeak(source,sink,source.prot,sink.prot,() => B"010",false,true)
        driveWeak(source,sink,source.allStrb,sink.allStrb,() => False,false,true)

        driveWeak(source, sink, source.domain, sink.domain, () => B("00"), false, false)
        driveWeak(source, sink, source.snoop, sink.snoop, () => B(sink.snoop.range -> false), false, false)
        driveWeak(source, sink, source.bar, sink.bar, () => B("00"), false, false)

    }
}