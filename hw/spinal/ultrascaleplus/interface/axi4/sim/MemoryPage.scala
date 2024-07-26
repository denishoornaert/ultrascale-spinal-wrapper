package ultrascaleplus.interface.axi.sim


class MemoryPage(size : Int) {
    val data = new Array[Byte](size)

    def clear(value : Byte) : Unit = {
      data.transform(x => value)
    }

    def read(offset : Int) : Byte = {
        this.data(offset)
    }

    def write(offset : Int, data : Byte) : Unit = {
        this.data(offset) = data
    }
    
    /** Reads an array from this page.
     * 
     * @param offset Offset into page
     * @return Byte array containing the read bytes. Reads may be limited by the page end.
     */
    def readArray(offset : Int, len : Int) : Array[Byte] = {
        var length = scala.math.min(len, size - offset)
        var data = new Array[Byte](length)

        for(i <- 0 until length) {
          data(i) = this.data(offset + i)
        }

        data
    }

    /** Writes an array to this page.
     * 
     * @param offset Offset into page.
     * @param data The byte array.
     * @return Number of bytes written. Writes may be limited by the page end.
     */
    def writeArray(offset : Int, data : Array[Byte]) : Int = {
        var length = scala.math.min(data.length, size - offset)

        for(i <- 0 until length) {
          this.data(offset + i) = data(i)
        }

        length
    }
}
