package ultrascaleplus.signal.irq


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils.{TCL, Util}
import ultrascaleplus.scripts.TCLFactory


case class IRQ(amount: Int = 8) extends Bits() with TCL {

  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    val index = this.getPartialName().takeRight(1)
    var tcl = ""
    tcl += TCLFactory.netConnection(f"${moduleName}_${this.getName()}", Seq(f"${moduleName}/${this.getName()}", f"processing_system/ps_pl_irq${index}"))
    tcl += "\n"
    return tcl
  }

  assert(
    assertion = (amount <= 8),
    message   = f"${amount} interrupt lines requested but only up to 8 supported!"
  )

  // This actually sets the bitwidth! Constructor does not take that parameter.
  // It it the companion objects that builds and sets it. This replicates that
  // process.
  this.setWidth(amount)

}
