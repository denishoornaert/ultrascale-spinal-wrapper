package ultrascaleplus.configport

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.io._

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.{SingleMapping, BusSlaveFactoryRead, BusSlaveFactoryWrite}
import spinal.lib.bus.amba4.axi._

import scripts._


case class ConfigPort(axi: Axi4, portName: String = null) extends Axi4SlaveFactory(axi) {
  
  if (portName != null) {
    setPartialName(portName)
  }

  val pageSize    =  12
  val template    =  "/configuration_port.h.template"
  val destinationParent = "hw/gen"
  val destination = f"${destinationParent}/${portName}.h"
  val items       = new ArrayBuffer[(BigInt, Data)]()

  override def read[T <: Data](that: T, address: BigInt, bitOffset: Int = 0, documentation: String = null): T = {
    val roundedAddress = (address >> log2Up(axi.config.bytePerWord)) << log2Up(axi.config.bytePerWord)
    val offset: Int = (address%axi.config.bytePerWord).toInt*8
    super.read(that, roundedAddress, offset, documentation)
    items.append((address, that))
    return that
  }

  override def write[T <: Data](that: T, address: BigInt, bitOffset: Int = 0, documentation: String = null): T = {
    val roundedAddress = (address >> log2Up(axi.config.bytePerWord)) << log2Up(axi.config.bytePerWord)
    val offset: Int = (address%axi.config.bytePerWord).toInt*8
    super.write(that, roundedAddress, offset, documentation)
    items.append((address, that))
    return that
  }

  override def readAndWrite(that: Data, address: BigInt, bitOffset: Int = 0, documentation: String = null): Unit = {
    val roundedAddress = (address >> log2Up(axi.config.bytePerWord)) << log2Up(axi.config.bytePerWord)
    val offset: Int = (address%axi.config.bytePerWord).toInt*8
    super.read(that, roundedAddress, offset, documentation)
    super.write(that, roundedAddress, offset, documentation)
    items.append((address, that))
  }

  def generateBundle(): Bundle = {
    val top = new Bundle()
    // Sort elements by address
    val sortedMapping = items.sortBy(_._1)
    // Previous address to compare against is page aligned
    var previous: BigInt = (sortedMapping(0)._1 >> pageSize) << pageSize
    for ((e, i) <- sortedMapping.zipWithIndex) {
      // Inserts reserved area whn needed
      val distance = (e._1-previous).toInt
      if (distance > 0) {
        val reserved = Vec.fill(distance)(UInt(8 bits))
        top.elements.append((f"reserved_${i}", reserved))
      }
      // Inserts actual element
      top.elements.append((e._2.name, e._2))
      // Sets new "previous address" as previous plus the element's size
      previous = previous+(widthOf(e._2)/8) // divide by 8 to convert to byte(s)
    }
    return top
  }

  def generateCStruct(bundle: Bundle): Unit = {
    println(CStructFactory(bundle))
  }

  def generateCHeader(bundle: Bundle): Unit = {
    // TODO: temporary
    // Sort elements by address
    val sortedMapping = items.sortBy(_._1)
    // Previous address to compare against is page aligned
    val aperture: BigInt = (sortedMapping(0)._1 >> pageSize) << pageSize
    
    // Open buffer for read
    val resource_url = getClass.getResource(template)
    val br = Source.fromURL(resource_url)
    // Make string out of buffer read
    var header = br.mkString
    // Close read buffer
    br.close
    // Generate c struct for configuration port
    val struct = CStructFactory(bundle)
    // Insert generate strings in the template
    header = header.replace("${insert_name}", portName)
    header = header.replace("${insert_struct}", struct)
    header = header.replace("${insert_addr}", aperture.toString())
    // Open buffer for writing
    if (!new File(destinationParent).exists()) {
      new File(destinationParent).mkdir()
    }
    val bw = new BufferedWriter(new FileWriter(new File(destination)))
    // Write header in target file
    bw.write(header+"\n")
    // Close FD.
    bw.close
  }
  
  override def build(): Unit = {
    super.build()
    val bundle = generateBundle()
    generateCStruct(bundle)
    generateCHeader(bundle)
  }

}
