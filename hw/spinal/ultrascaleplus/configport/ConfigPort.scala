package ultrascaleplus.configport

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.io._

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import scripts._


case class ConfigPort(axi: Axi4, aperture: BigInt) extends Area {
  setPartialName("Configuration_port")

  // Buffer size in bits
  val buffer_size = 512
  val buffer_rows = buffer_size/axi.config.dataWidth

  // Buffer; Wired to zero by default.
  val buffer: Array[Array[Bits]] = Array.fill(buffer_rows)(Array.fill(axi.config.dataWidth/8)(Reg(B(0, 8 bits))))

  // AXI logic
  val in_flight_read = Reg(Bool) init(False)
  val r  = Reg(Axi4R (axi.config))
  val in_flight_write = Reg(Bool) init(False)
  val awaited_write_data_handshakes = Reg(UInt(log2Up(256) bits)) init(0) // 256 is arbitrary
  val aw = Reg(Axi4Aw(axi.config))
  val b  = Reg(Axi4B (axi.config))

  // AXI Read

  // When handshake happens, store all metadata
  when (axi.ar.fire) {
    in_flight_read := True
  }
  .elsewhen (axi.r.fire & axi.r.last) {
    in_flight_read := False
  }

  // Not available if a transaction is already being handled
  axi.ar.ready := ~in_flight_read

  // targeted data
  val selected_row_index = axi.ar.addr(log2Up(buffer_size/8)-1 downto log2Up(axi.config.bytePerWord))
  val selected_row = (Cat(Seq.tabulate(buffer_rows)(b => Cat(buffer(b))))).subdivideIn(axi.config.dataWidth bits)(selected_row_index)

  // When handshake happens, provide register content
  // Assumes the transaction requested a one beat read
  when (axi.ar.fire) {
    r.id           := axi.ar.id
    r.data         := selected_row
    r.last         := True
    r.resp         := 0 // TODO: 0 for success, right?
  }
  // Reads can be provided as soon as the 'in_flight_read' is asserted
  axi.r.valid   := in_flight_read
  axi.r.payload := r

  // When handshake happens, store all write request metadata
  when (axi.aw.fire) {
    aw := axi.aw
    awaited_write_data_handshakes := axi.aw.len+1
    in_flight_write := True
  }
  // Not available if a write transaction is in progress
  axi.aw.ready := ~in_flight_write

  // When handshake happens, insert the data into the register
  val write_selected_row_index = aw.addr(log2Up(buffer_size/8)-1 downto log2Up(axi.config.bytePerWord))
  when (axi.w.fire & in_flight_write) {
    // Decrease counter
    awaited_write_data_handshakes := awaited_write_data_handshakes-1
    for (i <- 0 until buffer_rows) {
      when (i === write_selected_row_index) {
        for (b <- 0 until axi.config.bytePerWord) {
          when (axi.w.strb(b)) {
            buffer(i)(b) := axi.w.payload.data(b*8, 8 bits)
          }
        }
      }
    }
  }
  // Ready to recieve any write transaction handshake when a transaction is in progress
  axi.w.ready := (awaited_write_data_handshakes =/= 0)

  // Response phase only occur after the last data handshake
  val b_resp_valid = Reg(Bool()) init(False)
  when (axi.b.fire) {
    b_resp_valid := False
  }
  .otherwise {
    b_resp_valid := (axi.w.fire & axi.w.last & in_flight_write)
  }

  axi.b.valid := b_resp_valid
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
    if (in_row_pointer+element_size > axi.config.bytePerWord) {
      in_row_pointer = 0
      row_pointer += 1
    }
    // Add element to bundle for c struct generation
    top.elements.append((element.name, element))
    // Connect part of the buffer to the element
    element.assignFromBits(Cat(List.tabulate(element_size)(x => buffer(row_pointer)(in_row_pointer+x))))
    // Maintain pointers
    in_row_pointer += element_size
  }

  def generateCStruct(): Unit = {
    println(CStructFactory(top))
  }

  def generateCHeader(template: String = "hw/ext/configuration_port.h.template", destination: String = "hw/gen/configuration_port.h"): Unit = {
    // Open buffer for read
    val br = Source.fromFile(template)
    // Make string out of buffer read
    var header = br.mkString
    // Close read buffer
    br.close
    // Generate c struct for configuration port
    val struct = CStructFactory(top)
    // Insert generate strings in the template
    header = header.replace("${insert_struct}", struct)
    header = header.replace("${insert_addr}", aperture.toString())
    // Open buffer for writing
    val bw = new BufferedWriter(new FileWriter(new File(destination)))
    // Write header in target file
    bw.write(header+"\n")
    // Close FD.
    bw.close
  }
}
