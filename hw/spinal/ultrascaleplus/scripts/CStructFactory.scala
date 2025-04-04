package scripts

import spinal.core._
import spinal.lib._


object CStructFactory {

  trait Element {

    def ctype(): String
    
    def buildString(level: Int, name: String): String

    def buildString(name: String): String = buildString(0, name)
  }

  trait NamedElement extends Element {
    def name(): String

    def buildString(level: Int): String = {
      buildString(level, name)
    }
  }

  case class CStructElement(ctype: String) extends Element {

    def buildString(level: Int, name: String): String = {
      val padding = "\t"*level
      s"${padding}${ctype} ${name};\n"
    }

  }

  case class CStructVectorElement(base: Element, count: Int) extends Element {

    def ctype(): String = base.ctype()

    override def buildString(level: Int, name: String): String = {
      val padding = "\t"*level
      s"${padding}${base.ctype} ${name}[${count}];\n"
    }
  }

  case class CStructNamedElement(base: Element, name: String) extends NamedElement {

    def ctype(): String = base.ctype()

    def buildString(level: Int, name: String): String = base.buildString(level, name)

  }
  
  case class CStructBundleElement(content: Array[NamedElement]) extends Element {

    def ctype(): String = "struct"

    def buildString(level: Int, instanceName: String, structName: String) = {
      val padding = "\t"*level

      val safeLength = (s: String) => if (s == null) 0 else s.length()

      val _structName   = if (safeLength(structName) > 0) structName + ' ' else ""
      val _instanceName = if (safeLength(instanceName) > 0) ' ' + instanceName else ""
      

      s"${padding}struct ${_structName}{\n" + 
          content.map(item => item.buildString(level + 1)).mkString + 
          s"${padding}}${_instanceName};\n"
    }
    
    def buildString(instanceName: String, structName: String): String = buildString(0, instanceName, structName)
    
    override def buildString(level: Int, name: String): String = buildString(level, name, "")
  } 

  def apply(element: Data): Element = {
    
    val result = element.getClass.getName match {
      case "spinal.core.UInt" => CStructFactory(element.asInstanceOf[UInt])
      case "spinal.core.SInt" => CStructFactory(element.asInstanceOf[SInt])
      case "spinal.core.Bits" => CStructFactory(element.asInstanceOf[Bits])
      case "spinal.core.Bool" => CStructFactory(element.asInstanceOf[Bool])
      case "spinal.core.Vec" => CStructFactory(element.asInstanceOf[Vec[Data]])
      case _ => {
        CStructFactory(element.asInstanceOf[Bundle])
      }
    }
    return result
  }

  def apply(element: Bundle): CStructBundleElement = {
    
    val content: Array[NamedElement] = element.elements.map(entry => {
      val (name, item) = entry
      CStructNamedElement(CStructFactory(item), name)
    }).toArray

    return CStructBundleElement(content)
  }

  def apply(element: Vec[Data]): CStructVectorElement = {

    val result = CStructFactory(element(0))

    return CStructVectorElement(result, element.size)
  }


  def apply(element: UInt): CStructElement = {
    assert(List(8, 16, 32, 64).contains(element.getWidth), "UInt should be either 8, 16, 32, or 64.")

    val ctype = s"uint${element.getWidth}_t"

    return new CStructElement(ctype)
  }

  def apply(element: SInt): CStructElement = {
    assert(List(8, 16, 32, 64).contains(element.getWidth), "SInt should be either 8, 16, 32, or 64.")

    val ctype = s"int${element.getWidth}_t"

    return new CStructElement(ctype)
  }

  def apply(element: Bool): CStructElement = {
    assert(false, "Bool datatype not supported in configuration port. Try instead UInt(8 bit).")
    throw new RuntimeException("Bool datatype not supported in configuration port. Try instead UInt(8 bit).")
  }

  def apply(element: Bits): CStructElement = {
    assert(false, "Bits datatype not supported in configuration port. Think about using UInt() instead.")
    throw new RuntimeException("Bits datatype not supported in configuration port. Think about using UInt() instead.")
  }
  
}
