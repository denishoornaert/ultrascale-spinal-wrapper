package ultrascaleplus.configport

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.io._

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.{SingleMapping, BusSlaveFactoryRead, BusSlaveFactoryWrite}
import spinal.lib.bus.amba4.axi._

import scripts._
import ultrascaleplus.interface.axi.AbstractSecondaryAxi4

case class ConfigPort(axi: Axi4, portName: String, baseAddress: BigInt = 0) extends Axi4SlaveFactory(axi) {
  
  if (portName != null) {
    setPartialName(portName)
  }

  val pageSize    =  12
  val template    =  "hw/ext/configuration_port.h.template"
  val destination = f"hw/gen/${portName}.h"
  val items       = new ArrayBuffer[(BigInt, Data)]()

  var lastByte    = 0  

  def this(axi: Axi4) = this(axi, axi.getPartialName(), 0)

  def this(axi: Axi4, portName: String) = this(axi, portName, 0)

  def this(axi: Axi4, baseAddress: BigInt) = this(axi, axi.getPartialName(), baseAddress)


  def adjustAddress(address: BigInt, bitsWidth: Int = 0): (BigInt, Int) = {
    
    val valAddress = 
      if ((address % axi.config.bytePerWord) + (bitsWidth/8) > axi.config.bytePerWord) {
        ((address >> log2Up(axi.config.bytePerWord)) << log2Up(axi.config.bytePerWord)) + axi.config.bytePerWord
      } else {
        address
      }
      
    val roundedAddress = (valAddress >> log2Up(axi.config.bytePerWord)) << log2Up(axi.config.bytePerWord)
    val offset: Int = (valAddress%axi.config.bytePerWord).toInt*8

    return (roundedAddress, offset)
  }

  override def read[T <: Data](that: T, address: BigInt, bitOffset: Int = 0, documentation: String = null): T = {
    if (bitOffset != 0)
      throw new RuntimeException("BitOffset in function `read` will be ignore, set it to 0!")
    add((that, R), address, documentation)
    return that
  }

  override def write[T <: Data](that: T, address: BigInt, bitOffset: Int = 0, documentation: String = null): T = {
    if (bitOffset != 0)
      throw new RuntimeException("BitOffset in function `write` will be ignore, set it to 0!")
    add((that, W), address, documentation)
    return that
  }

  override def readAndWrite(that: Data, address: BigInt, bitOffset: Int = 0, documentation: String = null): Unit = {
    if (bitOffset != 0)
      throw new RuntimeException("BitOffset in function `readAndWrite` will be ignore, set it to 0!")
    add((that, RW), address, documentation)
  }

  private def safeAdd[T <: Data](target : T, mode: AccessMode, address: BigInt, offset: Int = 0, documentation: String = null): T = {
    if (mode.canRead)
      super.read(target, address, offset, documentation)
    if (mode.canWrite)
      super.write(target, address, offset, documentation)
    items.append((address + (offset / 8), target))
    target
  }

  def add[T <: Data](target : (T, AccessMode), address: BigInt, documentation: String = null) = {
    val (that, mode) = target
    val (roundedAddress, offset) = adjustAddress(baseAddress + address, that.getBitsWidth)
    safeAdd(that, mode, roundedAddress, offset, documentation)
  }

  def addAll[T <: Data](target: Array[(T, AccessMode)], address: BigInt = 0): BigInt = {
    var lastEnd = baseAddress + address
    for ((that, mode) <- target) {
      val (_addr, _offset) = adjustAddress(lastEnd, that.getBitsWidth)
      safeAdd(that, mode, _addr, _offset)
      lastEnd = _addr + ((_offset + that.getBitsWidth) / 8)
    }
    return lastEnd
  }

  def generateBundle(): Bundle = {
    val top = new Bundle()
    // Sort elements by address
    val sortedMapping = items.sortBy(_._1)
    // Previous address to compare against is page aligned
    var previous: BigInt = (sortedMapping(0)._1 >> pageSize) << pageSize
    for (((addr, that), i) <- sortedMapping.zipWithIndex) {
      // Inserts reserved area whn needed
      val distance = (addr-previous).toInt
      if (distance > 0) {
        val reserved = Vec.fill(distance)(UInt(8 bits))
        top.elements.append((f"reserved_${i}", reserved))
      }
      // Inserts actual element
      top.elements.append((that.name, that))
      // Sets new "previous address" as addr plus the element's size
      previous = addr+(widthOf(that)/8) // divide by 8 to convert to byte(s)
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
    val br = Source.fromFile(template)
    // Make string out of buffer read
    var header = br.mkString
    // Close read buffer
    br.close
    // Generate c struct for configuration port
    val struct_name = s"configuration_${portName}_s"
    val struct = CStructFactory(bundle).buildString("", struct_name)
    // Insert generate strings in the template
    header = header.replace("${insert_name}", portName)
    header = header.replace("${insert_struct}", struct)
    header = header.replace("${insert_addr}", aperture.toString())
    // Open buffer for writing
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
