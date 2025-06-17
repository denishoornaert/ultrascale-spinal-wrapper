package zcu102.io.ethernet


import ultrascaleplus.io.ethernet._


object GT0 extends GTInstanceTemplate {

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

  override val group = "Quad_X1Y3"

  override val lane = "X1Y14"

}
