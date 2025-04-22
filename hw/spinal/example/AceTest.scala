package example


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.ace.Ace
import spinal.lib.bus.amba4.ace.AceConfig

import spinal.lib.bus.amba4.ace.AceWithAttributes
import spinal.lib.bus.amba4.ace.AceAutoAck
import spinal.lib.bus.amba4.ace.AceR
import spinal.lib.bus.amba4.axi.Axi4ReadOnly
import spinal.lib.bus.amba4.axi.Axi4
import spinal.lib.bus.amba4.axilite.AxiLite4Utils._
import spinal.lib.bus.amba4.axi.Axi4Config

import kv260._
import kv260.interface.axi._
import ultrascaleplus.scripts._
import ultrascaleplus.configport._

object KV260Ace extends AceConfig(FPD_HPM0.config, 40, 128, true, portName = "ace_master") 

case class AceTest() extends Component {
  val io = new Bundle {
    val ace = Ace master(KV260Ace)
    val flow = slave(Axi4ReadOnly(KV260Ace.axi4config))
  }

  AceAutoAck(io.ace)

  // val data: Data = io.ace

  // data match {
  //   case x @ (_:Ace) => println("It's ace")
  //   case _ => println("Nope")
  // }

  // println(data.getClass(), data.asInstanceOf[io.ace.type].getClass())

  io.ace.setIdle(
    // io.ace.ar, io.ace.r
    )

  // io.outFlow <-< io.ace.r.toFlow

  // val f = Axi4ReadOnly(KV260Ace.axi4config)
  // f.setBlocked

  // // io.flow << f
  io.flow.setBlocked()

  // io.ace << f

  val s0 = Axi4(Axi4Config(40, 64, 6))
  s0.setIdle()

  val s1 = s0.toLite()
  val s2 = s1.toAxi()
  
  s2.setBlocked()
}

object AceTestVerilog extends App {
  Config.spinal.generateVerilog(AceTest())
}
