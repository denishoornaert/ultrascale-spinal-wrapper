package scripts

import spinal.core._
import spinal.lib._


object CStructFactory {

  def processs(element: Bundle, level: Int): String = {
    val padding = "\t"*level
    val name = element.getPartialName()

    var result = ""
    result += s"${padding}struct ${name} {\n"
    for ((name, elem) <- element.elements)
      result += ""//process(elem, level+1)
    result += "};\n"

    return result
  }

  def process(element: Vec[Data], level: Int): String = {
    val padding = "\t"*level
    val name = element.getPartialName()
    
    var result = ""
    // can use result.patch(result.size, element.size, 0)

    return result
  }

  def process(element: UInt, level: Int): String = {
    assert(List(8, 16, 32, 64).contains(element.getWidth), "UInt should be either 8, 16, 32, or 64.")

    val ctype = s"uint${element.getWidth}_t"
    val padding = "\t"*level
    val name = element.getPartialName()

    var result = s"${padding}${ctype} ${name};\n"

    return result
  }

  def process(element: SInt, level: Int): String = {
    assert(List(8, 16, 32, 64).contains(element.getWidth), "SInt should be either 8, 16, 32, or 64.")

    val ctype = s"sint${element.getWidth}_t"
    val padding = "\t"*level
    val name = element.getPartialName()

    var result = s"${padding}${ctype} ${name};\n"

    return result
  }

  def process(element: Bool, level: Int): String = {
    assert(false, "Bool datatype not supported in configuration port. Try instead UInt(8 bit).")
    return ""
  }

  def process(element: Bits, level: Int): String = {
    assert(false, "Bits datatype not supported in configuration port. Think about using UInt() instead.")
    return ""
  }
  
}
