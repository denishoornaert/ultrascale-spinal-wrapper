package ultrascaleplus.configport

import scala.collection.mutable.ArrayBuffer

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import scripts._


class ConfigPort(axi: Axi4) extends Area {
  setPartialName("Configuration_port")

  // Buffer size in bits
  val buffer_size = 512
  val buffer_rows = buffer_size/axi.config.dataWidth

  // Buffer
  val buffer: Array[Array[Bits]] = Array.fill(buffer_rows)(Array.fill(axi.config.dataWidth/8)(Bits(8 bits)))

  // AXI logic
  val in_flight_read = Reg(Bool) init(False)
  val ar = Reg(Axi4Ar(axi.config))
  val r  = Reg(Axi4R (axi.config))
  val in_flight_write = Reg(Bool) init(False)
  val aw = Reg(Axi4Aw(axi.config))
  val b  = Reg(Axi4B (axi.config))

  // AXI Read

  // When handshake happens, store all metadata
  when (axi.ar.fire) {
    ar := axi.ar.payload
    in_flight_read := True
  }
  // Not available if a transaction is already being handled
  axi.ar.ready := ~in_flight_read

  // When handshake happens, provide register content
  // Assumes the transaction requested a one beat read
  when (axi.r.fire & in_flight_read) {
    r.id           := ar.id
    r.data         := 42
    r.last         := True
    r.resp         := 0 // TODO: 0 for success, right?
    in_flight_read := False
  }
  axi.r.payload := r
  // Reads can be provided as soon as the 'in_flight_read' is asserted
  axi.r.valid := in_flight_read

  // AXI Write
  val data_array = Reg(Bits(axi.config.dataWidth bit)) init(0)

  // When handshake happens, store all write request metadata
  when (axi.aw.fire) {
    aw := axi.aw
    in_flight_write := False
  }
  // Not available if a write transaction is in progress
  axi.aw.ready := ~in_flight_write

  // When handshake happens, insert the data into the register
  when (axi.w.fire & in_flight_write) {
    data_array := axi.w.payload.data
  }
  // Ready to recieve any write transaction handshake when a transaction is in progress
  axi.w.ready := in_flight_write

  // Response phase only occur after the last data handshake
  axi.b.valid := (axi.w.fire & axi.w.last & in_flight_write)
  axi.b.resp  := 0 // TODO: 0 for success, right?
  axi.b.id    := aw.id

  // When the write response handshake happens, no transaction is being processed anymore
  when (axi.b.fire & in_flight_write) {
    in_flight_write := False
  }

  // on-demand register mapping
  val top = new Bundle()
  var row_pointer = 0
  var in_row_pointer = 0

  def addElement(element: Data): Unit = {
    val element_size = element.getBitsWidth/8
    // Check if padding is required
    if (in_row_pointer+element_size > axi.config.dataWidth/8) {
      in_row_pointer = 0
      row_pointer += 1
    }
    // Add element to bundle for c struct generation
    top.elements.append((element.name, element))
    // Connect part of the buffer to the element
    element.assignFromBits(Cat((List.tabulate(element_size)(x => buffer(row_pointer)(in_row_pointer+x))).reverse))
    // Maintain pointers
    in_row_pointer += element_size
  }

  def generateCStruct(): Unit = {
    println(CStructFactory(top, 0))
  }
}
