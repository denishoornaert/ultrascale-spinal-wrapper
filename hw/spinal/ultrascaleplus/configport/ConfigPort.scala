package configport

import scala.collection.mutable.ArrayBuffer

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import scripts._


class ConfigPort(config: Axi4Config) extends Area {
  setPartialName("Configuration port")

  val array = ArrayBuffer[Data]()

  var io = new Bundle {
    val axi = slave(Axi4(config))
  }

  // AXI logic
  val in_flight_read = Reg(Bool) init(False)
  val ar = Reg(Axi4Ar(config))
  val r  = Reg(Axi4R (config))
  val in_flight_write = Reg(Bool) init(False)
  val aw = Reg(Axi4Aw(config))
  val b  = Reg(Axi4B (config))

  // AXI Read

  // When handshake happens, store all metadata
  when (io.axi.ar.fire) {
    ar := io.axi.ar.payload
    in_flight_read := True
  }
  // Not available if a transaction is already being handled
  io.axi.ar.ready := ~in_flight_read

  // When handshake happens, provide register content
  // Assumes the transaction requested a one beat read
  when (io.axi.r.fire & in_flight_read) {
    r.id           := ar.id
    r.data         := 42
    r.last         := True
    r.resp         := 0 // TODO: 0 for success, right?
    in_flight_read := False
  }
  io.axi.r.payload := r
  // Reads can be provided as soon as the 'in_flight_read' is asserted
  io.axi.r.valid := in_flight_read

  // AXI Write
  val data_array = Reg(Bits(config.dataWidth bit)) init(0)

  // When handshake happens, store all write request metadata
  when (io.axi.aw.fire) {
    aw := io.axi.aw
    in_flight_write := False
  }
  // Not available if a write transaction is in progress
  io.axi.aw.ready := ~in_flight_write

  // When handshake happens, insert the data into the register
  when (io.axi.w.fire & in_flight_write) {
    data_array := io.axi.w.payload.data
  }
  // Ready to recieve any write transaction handshake when a transaction is in progress
  io.axi.w.ready := in_flight_write

  // Response phase only occur after the last data handshake
  io.axi.b.valid := (io.axi.w.fire & io.axi.w.last & in_flight_write)
  io.axi.b.resp  := 0 // TODO: 0 for success, right?
  io.axi.b.id    := aw.id

  // When the write response handshake happens, no transaction is being processed anymore
  when (io.axi.b.fire & in_flight_write) {
    in_flight_write := False
  }

  // on-demand register mapping
  
  def addElement(element: Data): Unit = {
    array.append(element)
  }

  val target_address = UInt(64 bits)

  addElement(target_address)

  println(CStructFactory.process(target_address, 0))
}

object ConfigPorts extends Component {
  
  var port:ConfigPort = null

  // TODO: add protection via condition checking if 'port' is already set
  def build(config: Axi4Config): Unit = {
    port = new ConfigPort(config)
  }

}
