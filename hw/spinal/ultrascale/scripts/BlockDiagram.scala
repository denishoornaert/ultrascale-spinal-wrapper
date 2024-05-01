package scripts

import java.io._

import spinal.core._
import spinal.core.sim._


/*
 * Object/singleton used to print out the block diagram of an architecture 
 * using the Mermaid format.
 */
object BlockDiagram {

  val target = "hw/gen/blockdiagram.md"

  def draw(component: Component, level: Int): String = {
    val padding = "\t"*level
    val id = component.getName()
    val name = component.getPartialName()
    
    var result = ""
    if (!component.children.isEmpty) {
      result += s"${padding}subgrap ${id}[${name}]\n"
      for (child <- component.children) {
        result += draw(child, level+1)
      }
      result += s"${padding}end\n"
    }
    else {
      result += s"${padding}${id}[${name}]\n"
    }

    return result
  } 

  def draw(toplevel: Component): Unit = {
    var result = ""
    result += "flowchart TB\n"
    result += "\tsubgraph KriaPlatform[Kria Platform]\n"
    result += "\t\tdirection LR\n"
    result += "\t\tPS[PS]\n"
    result += "\t\tsubgraph PL[PL]\n"
    result += "\t\t\tdirection TB\n"
    result += draw(toplevel, 3)
    result += "\t\tend\n"
    result += "\t\tPS <--> toplevel\n"
    result += "\tend\n"

    // Write out the mermaid block diagram
    val bw = new BufferedWriter(new FileWriter(new File(this.target), false))
    // Write in buffer.
    bw.write(result)
    // Close FD.
    bw.close
  }
}
