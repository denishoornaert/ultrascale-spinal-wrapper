package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable
import java.lang.Math.min
import java.nio.file.Paths
import java.nio.file.Files
import scala.collection.mutable.Map

import spinal.core._
import spinal.lib._


/** Class simulating target memory.
 *
 *  Allocates pages dynamically on-demand (i.e., on reads and writes).
 *  
 *  @constructor Creates a sparce and dynamic simulated memory target.
 *  @param pageSize Size of the page to allocate (default; 4096 byte).
 */
case class SparseMemory(pageSize: Int = 4096) {

  /** Page offset. */
  private val pageOffset = log2Up(pageSize)

  /** Mapping of physical address to memory block. */
  private val memory: Map[Int, MemoryPage] = Map()

  /** Method allocating and initializing a new page/memblock.
   *  
   *  @return page The newly allocated page.
   */
  private def allocPage(): MemoryPage = {
    val page = new MemoryPage(pageSize)
    page.clear(0xcd.toByte)
    return page
  }

  /** Method to safely access a page/memblock **(Recomanded!)**.
   *
   *  @param pfn Page frame number (PFN) of the target page.
   *  @return Target page/memblock.
   */
  private def getElseAllocPage(pfn: Int): MemoryPage = {
    if (!(memory contains pfn)) {
      println(f"Adding page ${pfn} at 0x${(pfn << this.pageOffset).toHexString}")
      memory += (pfn -> allocPage())
    }
    return memory(pfn)
  }

  /** Return the page frame number (PFN) of the target page/memblock.
   *  
   *  @param address The target address.
   *  @return The target page PFN.
   */
  private def getPFN(address: BigInt): Int = {
    return (address >> this.pageOffset).toInt
  }

  /** Return the page's offset within it PFN.
   *
   *  @param address The target address.
   *  @return The target page's offset.
   */
  private def getOffset(address: BigInt): Int = {
    val mask = (1 << this.pageOffset)-1
    return (address & mask).toInt
  }

  /** Read byte of data at specified address.
   *
   *  @param address The address where to find data.
   *  @return The data located at the specified address.
   */
  def read(address: BigInt): Byte = {
    val page = getElseAllocPage(getPFN(address))
    return page.read(getOffset(address))
  }

  /** Write byte of data at specified address.
   *
   *  @param address The address where to write the data.
   *  @param data The data (byte) to be written at the specified address.
   */
  def write(address: BigInt, data: Byte): Unit = {
    val page = getElseAllocPage(getPFN(address))
    page.write(getOffset(address), data)
  }

  /** Read (sequential) an array of byte from page/memblock.
   *
   *  @param address The address where the data is loacted.
   *  @param len The length of the array to read.
   *  @return The array of bytes to be read.
   */
  def readArray(address: BigInt, len: Long): Array[Byte] = {
    // Compute start and end PFNs for cases where PFNs would be crossed.
    val startPFN = getPFN(address)
    val endPFN = getPFN(address+len-1)
    // Offset within first page/memblock
    var offset = getOffset(address)
    // Result read buffer
    val buffer = new mutable.ArrayBuffer[Byte](0)
    // For each page we access
    for(i <- startPFN to endPFN) {
      val page = getElseAllocPage(i)
      // Calculate end byte within page
      // (max array to retrieve = the page/memblock size)
      val lenInPage = min(pageSize, len.toInt-buffer.length)
      buffer.appendAll(page.readArray(offset, lenInPage))
      // Access in the next page is guaranteed to start at 0
      offset = 0
    }
    return buffer.toArray
  }

  /** Write (sequential) an array of byte to page/memblock.
   *
   *  @param address The address where the data is written to.
   *  @param data Sequence of data (as bytes) to be written.
   */
  def writeArray(address: BigInt, data: Array[Byte]): Unit = {
    // Compute start and end PFNs for cases where PFNs would be crossed.
    val startPFN = getPFN(address)
    val endPFN = getPFN(address+data.length-1)
    // Offset within first page/memblock
    var offset = getOffset(address) 
    // For each page we access
    List.tabulate(endPFN-startPFN+1)(_+startPFN).foldLeft(data){
      (writeData, pfn) => {
          val page = getElseAllocPage(pfn)
          val bytesWritten = page.writeArray(offset, writeData)
          // Access in the next page is guaranteed to start at 0
          offset = 0
          writeData.drop(bytesWritten)
      }
    }
  }

  /** Reads a BigInt value from the given address.
   * 
   *  @param address Read address.
   *  @param width Length of the byte array to be read in bytes.
   *  @return BigInt read from the given address.
   */
  def readBigInt(address: BigInt, length: Int): BigInt = {
    val dataArray = readArray(address, length)
    val buffer = dataArray.reverse.toBuffer // revert for Little Endian representation

    // We never want negative numbers
    buffer.prepend(0.toByte)

    BigInt(buffer.toArray)
  }

  /** Writes a BigInt value to the given address.
   * 
   *  The BigInt will be resized to a byte Array of given width.
   *  The data will be trimmed if it is bigger than the given width.
   *  If it is smaller, the unused bytes will be filled with '0x00'.
   * 
   *  @param address Write address.
   *  @param data Data to be written.
   *  @param width Width of the byte Array the data is resized to (if necessary).
   */
  def writeBigInt(address: BigInt, data: BigInt, width: Int, strb: BigInt=null): Unit = {
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

  /** Populate memory with content of binary flie/blob.
   *
   *  @param address Location where to start dumping file/blob.
   *  @param file Name of the file containing the binry blob.
   */
  def loadBinary(address: BigInt, file: String): Unit = {
    val byteArray = Files.readAllBytes(Paths.get(file))
    writeArray(address, byteArray)
    println(s"Loading 0x${byteArray.length.toHexString} bytes from ${file} to 0x${address.toString(16)}")
  }

  /** Capture state of the memory and dump it in file.
   *
   *  @param address Address where to start the capture. 
   *  @param len Amount of bytes to capture.
   *  @param file Target file in system to dump the binary blob in.
   */
  def saveBinary(address: BigInt, len: Long, file: String) : Unit = {
    val byteArray = readArray(address, len)
    Files.write(Paths.get(file), byteArray)
    println(s"Saving 0x${len.toHexString} bytes from 0x${address.toString(16)} to ${file}")
  }
}
