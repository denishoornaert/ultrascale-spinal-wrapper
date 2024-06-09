package scripts

import spinal.core._
import spinal.lib._


object CStructFactory {

  // implicit topmodule
  def apply(element: Bundle): String = {
    val name = element.getPartialName()

    var result = ""
    result += "struct configuration_s {\n"
    for ((name, elem) <- element.elements)
      result += CStructFactory(elem, 1)
    result += "};\n"

    return result
  }

  def apply(element: Data, level: Int): String = {
    val result = element.getClass.getName match {
      case "spinal.core.UInt" => CStructFactory(element.asInstanceOf[UInt], level)
      case "spinal.core.SInt" => CStructFactory(element.asInstanceOf[SInt], level)
      case "spinal.core.Bits" => CStructFactory(element.asInstanceOf[Bits], level)
      case "spinal.core.Bool" => CStructFactory(element.asInstanceOf[Bool], level)
      case "spinal.core.Vec" => CStructFactory(element.asInstanceOf[Vec[Data]], level)
      // Default catches Bundle definitions
      case _ => CStructFactory(element.asInstanceOf[Bundle], level)
    }
    return result
  }

  def apply(element: Bundle, level: Int): String = {
    val padding = "\t"*level
    val name = element.getPartialName()

    var result = ""
    result += s"${padding}struct {\n"
    for ((name, elem) <- element.elements)
      result += CStructFactory(elem, level+1)
    result += s"${padding}} ${name};\n"

    return result
  }

  def apply(element: Vec[Data], level: Int): String = {
    val padding = "\t"*level
    val name = element.getPartialName()
    
    var result = CStructFactory(element(0), level)
    // '-3' is because 1 (';'), 1 ('0' or the first element of the array), and 1 because...
    result = result.patch(result.size-3, s"${name}[${element.size}]", 1)

    return result
  }

  def apply(element: UInt, level: Int): String = {
    assert(List(8, 16, 32, 64).contains(element.getWidth), "UInt should be either 8, 16, 32, or 64.")

    val ctype = s"uint${element.getWidth}_t"
    val padding = "\t"*level
    val name = element.getPartialName()

    var result = s"${padding}${ctype} ${name};\n"

    return result
  }

  def apply(element: SInt, level: Int): String = {
    assert(List(8, 16, 32, 64).contains(element.getWidth), "SInt should be either 8, 16, 32, or 64.")

    val ctype = s"int${element.getWidth}_t"
    val padding = "\t"*level
    val name = element.getPartialName()

    var result = s"${padding}${ctype} ${name};\n"

    return result
  }

  def apply(element: Bool, level: Int): String = {
    assert(false, "Bool datatype not supported in configuration port. Try instead UInt(8 bit).")
    return ""
  }

  def apply(element: Bits, level: Int): String = {
    assert(false, "Bits datatype not supported in configuration port. Think about using UInt() instead.")
    return ""
  }
  
}
