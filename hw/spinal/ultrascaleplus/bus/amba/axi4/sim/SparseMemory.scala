package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable
import java.nio.file.Paths
import java.nio.file.Files

import scala.collection.mutable.Map


case class SparseMemory() {
  val memory: Map[Int, MemoryPage] = Map()

  def allocPage() : MemoryPage = {
    val page = new MemoryPage(1024*1024)
    page.clear(0xcd.toByte)
    page
  }

  def invalidPage() : MemoryPage = {
    val page = new MemoryPage(1024*1024)
    page.clear(0xef.toByte)
    page
  }

  def getElseAllocPage(index: Int) : MemoryPage = {
    if (!(memory contains index)) { //(memory(index) == null) {
      println(s"Adding page ${index} at 0x${(index << 20).toHexString}")
      memory += (index -> allocPage())
    }
    memory(index)
  }

  def getElseInvalidPage(index: Int) : MemoryPage = {
    if (!(memory contains index)) {
      println(s"Page fault while reading page ${index} (0x${(index << 20).toHexString})")
      invalidPage()
    }
    else
      memory(index)
  }

  def getPageIndex(address: BigInt) : Int = {
    (address >> 20).toInt
  }

  def getOffset(address: BigInt) : Int = {
    val mask = (1 << 20) - 1
    (address & mask).toInt
  }

  def read(address: BigInt) : Byte = {
    getElseInvalidPage(getPageIndex(address)).read(getOffset(address))
  }

  def write(address: BigInt, data: Byte) : Unit = {
    getElseAllocPage(getPageIndex(address)).write(getOffset(address), data)
  }

  def readArray(address: BigInt, len: Long) : Array[Byte] = {
    val startPageIndex = getPageIndex(address)
    val endPageIndex = getPageIndex(address + len - 1)
    var offset = getOffset(address)
    val buffer = new mutable.ArrayBuffer[Byte](0)
    
    for(i <- startPageIndex to endPageIndex) {
      val page = getElseAllocPage(i)
      val readArray = page.readArray(offset, len.toInt - buffer.length)
      buffer.appendAll(readArray)
      offset = 0
    }

    buffer.toArray
  }

  def writeArray(address: BigInt, data: Array[Byte]) : Unit = {
    val startPageIndex = getPageIndex(address)
    val endPageIndex = getPageIndex(address + data.length - 1)
    var offset = getOffset(address)
    
    List.tabulate(endPageIndex - startPageIndex + 1)(_ + startPageIndex).foldLeft(data){
      (writeData,pageIndex) => {
          val page = getElseAllocPage(pageIndex)
          val bytesWritten = page.writeArray(offset, writeData)
          offset = 0
          writeData.drop(bytesWritten)
      }
    }
  }

  /** Reads a BigInt value from the given address.
   * 
   * @param address Read address.
   * @param width Length of the byte array to be read in bytes.
   * @return BigInt read from the given address.
   */
  def readBigInt(address: BigInt, length: Int) : BigInt = {
    val dataArray = readArray(address, length)
    val buffer = dataArray.reverse.toBuffer // revert for Little Endian representation

    // We never want negative numbers
    buffer.prepend(0.toByte)

    BigInt(buffer.toArray)
  }

  /** Writes a BigInt value to the given address.
   * The BigInt will be resized to a byte Array of given width.
   * The data will be trimmed if it is bigger than the given width.
   * If it is smaller, the unused bytes will be filled with '0x00'.
   * 
   * @param address Write address.
   * @param data Data to be written.
   * @param width Width of the byte Array the data is resized to (if necessary).
   */
  def writeBigInt(address: BigInt, data: BigInt, width: Int, strb: BigInt=null) {
    var dataArray = data.toByteArray.reverse
    var length = scala.math.min(width, dataArray.length)
    var result = Array.fill[Byte](width)(0.toByte)

    for(i <- 0 until length)
      result(i) = dataArray(i)

    if(strb != null){
      val strbArray = strb.toByteArray.reverse
      val origin = readArray(address,width)
      // replace with origin data according to strobes
      for(i <- Range(0,width,8)){
        val strb = strbArray.applyOrElse(i>>3,(x:Int)=>0.toByte).toInt
        if(strb != 0xff){
          for(j <- 0 until 8;k = i + j){
            if(k < width && (strb & (1<<j)) == 0){
              result(k) = origin(k)
            }
          }
        }
      }
    }
    
    writeArray(address, result)
  }

  def loadBinary(address: BigInt, file: String) : Unit = {
    val byteArray = Files.readAllBytes(Paths.get(file))
    writeArray(address, byteArray)

    println(s"Loading 0x${byteArray.length.toHexString} bytes from ${file} to 0x${address.toString(16)}")
  }

  def loadDebugSequence(address: BigInt, length: Int, width : Int) : Unit = {
    for(i <- 0 until length) {
      writeBigInt(address+i*width, address+i*width, width)
    }
  }

  def saveBinary(address: BigInt, len: Long, file: String) : Unit = {
    val byteArray = readArray(address, len)
    Files.write(Paths.get(file), byteArray)

    println(s"Saving 0x${len.toHexString} bytes from 0x${address.toString(16)} to ${file}")
  }
}
