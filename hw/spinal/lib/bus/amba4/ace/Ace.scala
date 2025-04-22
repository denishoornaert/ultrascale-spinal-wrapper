package spinal.lib.bus.amba4.ace

import spinal.core._
import spinal.lib._

import spinal.lib.bus.amba4.axi._
import spinal.lib.tools.StreamToElements

case class AceConfig(axi4config: Axi4Config,
                      aceAddressWidth : Int,
                      aceDataWidth: Int,
                      hasAWUnique: Boolean,
                      useDomain: Boolean = true,
                      useSnoop: Boolean = true,
                      useBar: Boolean = true,
                      portName: String = "ace") {

    
    require(List(8, 16, 32, 64, 128, 256, 512, 1024) contains aceDataWidth,
        "Valid data width: 8, 16, 32, 64, 128, 256, 512 or 1024 bit")

    def aceAddressType = UInt(aceAddressWidth bits)
    def aceDataType = Bits(aceDataWidth bits)

}

trait AceBus extends IMasterSlave

case class Ace(config: AceConfig) extends Bundle with AceBus {

    val ac      = Stream(new AceAc(config))
    val cr      = Stream(new AceCr(config))
    val cd      = Stream(new AceCd(config))

    val aw      = Stream(new AceAw(config))
    val w       = Stream(new Axi4W(config.axi4config))
    val b       = Stream(new Axi4B(config.axi4config))
    val ar      = Stream(new AceAr(config))
    val r       = Stream(new AceR(config))

    val rack    = Bool()
    val wack    = Bool()

    
    setName(config.portName)
    
    def channels: Vector[Stream[_ <: Bundle]] = Vector(ac, cr, cd, aw, w, b, ar, r)

    def acks: Vector[Bool] = Vector(rack, wack)

    def asMaster(): Unit = {
        master(cr, cd, aw, w, ar)
        slave(ac,r,b)
        out(rack, wack)
    }

    def << (that: Axi4ReadOnly): Ace = {
        require(config.axi4config == that.config)

        that.ar.payload.assignSomeByName(ar.payload)
        that.ar.arbitrationFrom(ar)
        r.payload.assignSomeByName(that.r.payload)
        r.arbitrationFrom(that.r)

        this
    }
    def >> (that: Axi4ReadOnly): Ace = this << that

    def setIdle(filter: Stream[_]*) = channels.foreach(
        that => if (!filter.contains(that)) if (that.isMasterInterface) that.setIdle() else that.setBlocked())

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

object Ace {

    def apply(config: AceConfig): Ace = new Ace(config)

    object master {
        def apply(config: AceConfig): Ace = AceWithAttributes(spinal.lib.master(new Ace(config)))
    }

    object slave {
        def apply(config: AceConfig): Ace = AceWithAttributes(spinal.lib.slave(new Ace(config)))
    }
}

object AceWithAttributes {

    val x_interface_info = "X_INTERFACE_INFO"

    def port[T <: Data](target: T, interfaceName: String, prefix: String = ""): Unit =
        target.addAttribute(x_interface_info, f"xilinx.com:interface:acemm:1.0 ${interfaceName} ${prefix}${target.getPartialName().toUpperCase()}")

    def stream[T <: Stream[_ <: Data]](target: T, interfaceName: String): Unit =
        StreamToElements(target).foreach({
            case (name, _target) => port(_target, interfaceName, target.getPartialName().toUpperCase())
        })

    def apply(target: Ace): Ace = {
        target.addAttribute(x_interface_info, f"XIL_INTERFACENAME ${target.getName()}, PROTOCOL ACE, MODE ${if (target.isMasterInterface) "Master" else "Slave"}")
        target.channels.foreach(stream(_, target.getName()))
        target.acks.foreach(port(_, target.getName()))
        target
    }
}
