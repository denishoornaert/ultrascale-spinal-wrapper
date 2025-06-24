package zcu102.io.ethernet


import ultrascaleplus.ip.{Ethernet}
import ultrascaleplus.io.gt._


object GT0 extends GTMappedTemplate() {

  /**
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  override val TX = Seq[String]("B6", "B5")

  /**``
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  override val SFP = Seq[String]("B13")

  /**
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  override val RX = Seq[String]("B2", "B1")

}

object Ethernet0 extends Ethernet.Config(
  group = "Quad_X1Y3",
  lane  = "X1Y14"
)
