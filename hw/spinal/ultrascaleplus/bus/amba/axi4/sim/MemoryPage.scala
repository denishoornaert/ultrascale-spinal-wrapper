package ultrascaleplus.bus.amba.axi4.sim


/** Simulated memory block.
 *
 *  @constructor Creates a memory block of `size` bytes.
 *  @param size Size in bytes of the memory block (default: 4096 bytes).`
 */
class MemoryPage(size: Int = 4096) {

    assert(
      assertion = (size <= 4096),
      message   = f"The memory page size requested is higher than allowed (max 4096 but ${size} requested!)."
    )

    /** Actual data buffer. */
    private val data = new Array[Byte](size)

    /** Sets whole buffer to specified value.
     *
     *  @param value The value (a byte!) to populate the page with.
     */
    def clear(value: Byte): Unit = {
      data.transform(x => value)
    }

    /** Reads a single byte from this page.
     * 
     *  @param offset Offset within the memory page.
     *  @return data Data byte read from page at specified offset. 
     */
    def read(offset: Int): Byte = {
      assert(
        assertion = (offset < 4096),
        message   = f"The offset requested is higher than allowed (max 4095 but ${offset} requested!)."
      )
      this.data(offset)
    }

    /** Writes a single byte from this page.
     * 
     *  @param offset Offset within the memory page.
     *  @param data Data byte to write to the page page. 
     */
    def write(offset: Int, data: Byte): Unit = {
      assert(
        assertion = (offset < 4096),
        message   = f"The offset requested is higher than allowed (max 4095 but ${offset} requested!)."
      )
      this.data(offset) = data
    }
    
    /** Reads a byte array from this page.
     * 
     *  @param offset Offset within the memory page.
     *  @param len Length of request byte array.
     *  @return byte Array containing the read bytes. Reads may be limited by 
     *  the page end.
     */
    def readArray(offset: Int, len: Int): Array[Byte] = {
      val length = scala.math.min(len, size-offset)
      val data = new Array[Byte](length)
      for(i <- 0 until length)
        data(i) = this.data(offset+i)
      return data
    }

    /** Writes a byte array to this page.
     * 
     *  @param offset Offset within the memory page.
     *  @param data Byte array to write within the page. 
     *  @return Number of bytes written. Writes may be limited by the page end.
     */
    def writeArray(offset: Int, data: Array[Byte]): Int = {
      val length = scala.math.min(data.length, size - offset)
      for(i <- 0 until length)
        this.data(offset+i) = data(i)
      return length
    }
}
