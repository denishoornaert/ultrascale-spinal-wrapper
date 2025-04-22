package spinal.lib.bus.amba4.ace


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

class AceAc(val config: AceConfig) extends Bundle {
    val addr    = UInt(config.aceAddressWidth bits)
    val snoop   = Bits(4 bits)
    val prot    = Bits(3 bits)
}

class AceCr(val config: AceConfig) extends Bundle {
    val crresp  = Bits(5 bits)
}

class AceCd(val config: AceConfig) extends Bundle {
    val cddata = Bits(config.aceDataWidth bits)
    val cdlast = Bool()
}

class AceAx(config: AceConfig, snoopWidth: Int, userWidth: Int, readOnly: Boolean) extends Axi4Ax(config.axi4config, userWidth, readOnly) {
    val domain  = if (config.useDomain) Bits(2 bits)            else null
    val snoop   = if (config.useSnoop)  Bits(snoopWidth bits)   else null
    val bar     = if (config.useBar)    Bits(2 bits)            else null
}

object AcePriv {

    def driveAx[T <: AceAx](stream: Stream[T], sink: Stream[T]): Unit = {
        Axi4Priv.driveAx(stream, sink)

        Axi4Priv.driveWeak(stream,sink,stream.domain,sink.domain,() => B"00",false,true)
        Axi4Priv.driveWeak(stream,sink,stream.snoop,sink.snoop,() => B"00",false,true)
        Axi4Priv.driveWeak(stream,sink,stream.bar,sink.bar,() => B"00",false,true)
        Axi4Priv.driveWeak(stream,sink,stream.domain,sink.domain,() => B(sink.domain.range -> false),false,false)
    }

//   def driveAx[T <: Axi4Ax](stream: Stream[T],sink: Stream[T]): Unit = {
//     sink.arbitrationFrom(stream)
//     assert(stream.config.idWidth <= sink.config.idWidth, s"Expect $stream idWidth=${stream.config.idWidth} <= $sink idWidth=${sink.config.idWidth}")
//     assert(stream.config.addressWidth >= sink.config.addressWidth, s"Expect $stream addressWidth=${stream.config.addressWidth} >= $sink addressWidth=${sink.config.addressWidth}")

//     sink.addr := stream.addr.resized
//     driveWeak(stream,sink,stream.id,sink.id,() => U(sink.id.range -> false),true,false)
//     driveWeak(stream,sink,stream.region,sink.region,() => B(sink.region.range -> false),false,true)
//     driveWeak(stream,sink,stream.len,sink.len,() => U(sink.len.range -> false),false,false)
//     driveWeak(stream,sink,stream.size,sink.size,() => U(log2Up(sink.config.dataWidth/8)),false,false)
//     driveWeak(stream,sink,stream.burst,sink.burst,() => Axi4.burst.INCR,false,false)
//     driveWeak(stream,sink,stream.lock,sink.lock,() => Axi4.lock.NORMAL,false,true)
//     driveWeak(stream,sink,stream.cache,sink.cache,() => B"0000",false,true)
//     driveWeak(stream,sink,stream.qos,sink.qos,() => B"0000",false,true)
//     driveWeak(stream,sink,stream.user,sink.user,() => B(sink.user.range -> false),true,true)
//     driveWeak(stream,sink,stream.prot,sink.prot,() => B"010",false,true)
//     driveWeak(stream,sink,stream.allStrb,sink.allStrb,() => False,false,true)
//   }

}

class AceAw(config: AceConfig) extends AceAx(config, 3, config.axi4config.awUserWidth, false) {

    val unique  = if (config.hasAWUnique) Bool() else null

    override def clone: this.type = new AceAw(config).asInstanceOf[this.type]
}

class AceAr(config: AceConfig) extends AceAx(config, 4, config.axi4config.arUserWidth, true) {

    override def clone: this.type = new AceAr(config).asInstanceOf[this.type]
}

class AceR(config: AceConfig) extends Axi4R(config.axi4config) {

    override val resp = Bits(4 bits)

    import Axi4.resp._

    override def setOKAY()   : Unit = resp(1 downto 0) := OKAY
    override def setEXOKAY() : Unit = resp(1 downto 0) := EXOKAY
    override def setSLVERR() : Unit = resp(1 downto 0) := SLVERR
    override def setDECERR() : Unit = resp(1 downto 0) := DECERR
    override def isOKAY()    : Bool = resp(1 downto 0) === OKAY
    override def isEXOKAY()  : Bool = resp(1 downto 0) === EXOKAY
    override def isSLVERR()  : Bool = resp(1 downto 0) === SLVERR
    override def isDECERR()  : Bool = resp(1 downto 0) === DECERR

    def setPassDirty(passDirty: Bool) : Unit = resp(2) := passDirty
    def setIsShared(isShared: Bool) : Unit = resp(3) := isShared

    def isPassDirty() : Bool = resp(2)
    def isIsShared() : Bool = resp(3)
}