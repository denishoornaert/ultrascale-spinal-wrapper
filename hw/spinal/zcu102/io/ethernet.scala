package zcu102.io.ethernet


import ultrascaleplus.io.ethernet._


object GT0 {

  /**
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  val TX = Seq[String]("B6", "B5")

  /**``
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  val SFP = Seq[String]("B13")

  /**
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  val RX = Seq[String]("B2", "B1")

}
