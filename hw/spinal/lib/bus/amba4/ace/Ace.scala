/**
 * Author:    Francesco Ciraolo
 * Created:   13.10.2025
 * 
 * Placeholder for Ace.scala as a interface with an ad-hoc WIP library
 * 
 * Note: some API could change
 * 
 **/
package spinal.lib.bus.amba4.ace

import spinal.core._
import spinal.lib._

import spinal.lib.bus.amba4.axi._

object Test extends AceConfig(
    addressWidth = 40,
    dataWidth    = 128,
    aceAddressWidth = 40,
    idWidth      = 8
)

case class AceConfig(addressWidth : Int,
                      dataWidth    : Int,
                      aceAddressWidth : Int,
                      idWidth      : Int = -1,
                    //   useId        : Boolean = true,
                      useRegion    : Boolean = true,
                      useBurst     : Boolean = true,
                      useLock      : Boolean = true,
                      useCache     : Boolean = true,
                      useSize      : Boolean = true,
                      useQos       : Boolean = true,
                      useLen       : Boolean = true,
                      useLast      : Boolean = true,
                      useResp      : Boolean = true,
                      useProt      : Boolean = true,
                      useStrb      : Boolean = true,
                      hasAWUnique: Boolean = false,
                      useDomain: Boolean = true,
                      useSnoop: Boolean = true,
                      useBar: Boolean = true,
                      useAllStrb   : Boolean = false,
                      arUserWidth  : Int = -1,
                      awUserWidth  : Int = -1,
                      rUserWidth   : Int = -1,
                      wUserWidth   : Int = -1,
                      bUserWidth   : Int = -1,

                      readIssuingCapability     : Int = -1,
                      writeIssuingCapability    : Int = -1,
                      combinedIssuingCapability : Int = -1,
                      readDataReorderingDepth   : Int = -1) {

    require(List(8, 16, 32, 64, 128, 256, 512, 1024) contains dataWidth,
        "Valid data width: 8, 16, 32, 64, 128, 256, 512 or 1024 bit")

    def aceAddressType = UInt(aceAddressWidth bits)
    def aceDataType = Bits(dataWidth bits)

    def useId = idWidth >= 0
    def useArUser = arUserWidth >= 0
    def useAwUser = awUserWidth >= 0
    def useRUser = rUserWidth >= 0
    def useWUser = wUserWidth >= 0
    def useBUser = bUserWidth >= 0
    def useArwUser = arwUserWidth >= 0 //Shared AR/AW channel
    def arwUserWidth = Math.max(arUserWidth, awUserWidth)
    def sizeWidth = 3
    def lenWidth = 8
    def lockWidth = 1

    if(useId)
        require(idWidth >= 0,"You need to set idWidth")

    require(combinedIssuingCapability >= scala.math.max(readIssuingCapability, writeIssuingCapability),
        "Inconsistent combined issuing capability")
    require(readDataReorderingDepth <= readIssuingCapability,
        "Inconsistent read data reordering depth")

    require(List(8, 16, 32, 64, 128, 256, 512, 1024) contains dataWidth,
        "Valid data width: 8, 16, 32, 64, 128, 256, 512 or 1024 bit")

    def addressType = UInt(addressWidth bits)
    def dataType = Bits(dataWidth bits)
    def idType = UInt(idWidth bits)
    def lenType = UInt(8 bits)
    def bytePerWord = dataWidth/8
    def symbolRange = log2Up(bytePerWord)-1 downto 0
    def wordRange    = addressWidth-1 downto log2Up(bytePerWord)
}

trait AceBus extends IMasterSlave

case class Ace(config: AceConfig) extends Bundle with AceBus {

    val ac      = Stream(new AceAc(config))
    val cr      = Stream(new AceCr(config))
    val cd      = Stream(new AceCd(config))

    val aw      = Stream(new AceAw(config))
    val w       = Stream(new AceW(config))
    val b       = Stream(new AceB(config))
    val ar      = Stream(new AceAr(config))
    val r       = Stream(new AceR(config))

    val rack    = Bool()
    val wack    = Bool()

    def channels: Vector[Stream[_ <: Bundle]] = Vector(ac, cr, cd, aw, w, b, ar, r)

    def acks: Vector[Bool] = Vector(rack, wack)

    def asMaster(): Unit = {
        master(cr, cd, aw, w, ar)
        slave(ac,r,b)
        out(rack, wack)
    }

    def >> (that: Ace): Unit = {
        that.ac >> this.ac
        this.cr >> that.cr
        this.cd >> that.cd

        this.ar drive that.ar
        that.r drive this.r

        this.aw drive that.aw
        this.w drive that.w
        that.b drive this.b

        that.rack := this.rack
        that.wack := this.wack
    }

    def << (that: Ace): Unit = that >> this

    def setIdle(): Unit = {
        ar.setIdle()
        aw.setIdle()
        w.setIdle()
        b.setBlocked()
        r.setBlocked()
        ac.setBlocked()
        cr.setIdle()
        cd.setIdle()
        rack := False
        wack := False
    }

    def setBlocked(): Unit = {
        ar.setBlocked()
        aw.setBlocked()
        w.setBlocked()
        b.setIdle()
        r.setIdle()
        ac.setIdle()
        cr.setBlocked()
        cd.setBlocked()
    }

}

case class AceMonitor(config: AceConfig) extends Bundle with IMonitor {

    val ac      = MonitorStream(new AceAc(config))
    val cr      = MonitorStream(new AceCr(config))
    val cd      = MonitorStream(new AceCd(config))

    val aw      = MonitorStream(new AceAw(config))
    val w       = MonitorStream(new AceW(config))
    val b       = MonitorStream(new AceB(config))
    val ar      = MonitorStream(new AceAr(config))
    val r       = MonitorStream(new AceR(config))

    val rack    = in(Bool())
    val wack    = in(Bool())

    def channels: Vector[MonitorStream[_ <: Bundle]] = Vector(ac, cr, cd, aw, w, b, ar, r)

    def acks: Vector[Bool] = Vector(rack, wack)

}

object AceAutoAck {

    def apply(port: Ace) = {
        val _rack   = Reg(Bool())
        val _wack   = Reg(Bool())

        _rack       := port.r.fire //valid && port.r.ready
        _wack       := port.b.fire //.valid && port.b.ready

        port.rack   := _rack
        port.wack   := _wack
    }
}
