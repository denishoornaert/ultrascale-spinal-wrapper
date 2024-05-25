package scripts

import java.io._

import spinal.core._
import spinal.core.sim._


object Constraints {

  val target = "hw/gen/constraints.xdc"
  var alreadyOpened = false

  def add(line: String): Unit = {
    // If already opened before, append to file. Otherwise, overwrite content.
    val bw = new BufferedWriter(new FileWriter(new File(this.target), this.alreadyOpened))
    // From now on, the logger will only append to the file.
    this.alreadyOpened = true
    // Write in buffer.
    bw.write(line+"\n")
    // Close FD.
    bw.close
  }

}
