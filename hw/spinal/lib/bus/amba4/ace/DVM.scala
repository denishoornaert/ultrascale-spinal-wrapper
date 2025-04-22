package lib.bus.amba4.ace

import spinal.core._
import spinal.lib._

case class DVMConfig(
    physicalAddressWidth: Int,
    virtualAddressWidth: Int,
    canHavePhysicalAddr: Boolean,
    canHaveVirtualAddr: Boolean
) {

    def canHaveSecondPart   = canHavePhysicalAddr || canHaveVirtualAddr
    def largestAddrWidth    = scala.math.max(virtualAddressWidth, physicalAddressWidth)

    require(
        List((32, 32), (40, 32), (40, 41), (44, 49), (48, 57))
            .contains((physicalAddressWidth, virtualAddressWidth)),
        "Invalid physical address width and virtual address width combination," +
          " refer to ARM IHI0022H for valid combinations")

}

class DVMMessage(config: DVMConfig) extends Bundle {
    // val firtPartExtra   = 

    val completion      = Bool()
    val messageType     = Bits(3 bits)
    val exceptionLevel  = Bits(2 bits)
    val security        = Bits(2 bits)
    val range           = Bool()
    val vmidOrVaValid   = Bool()
    val asidOrVaValid   = Bool()
    val leaf            = Bool()
    val stage           = Bits(2 bits)
    val addr            = Bool()
}

trait DVMBus extends IMasterSlave

abstract class DVM(config: DVMConfig) extends DVMBus {

    def hasTwoParts: Bool

    
}