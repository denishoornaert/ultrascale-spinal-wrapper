package zcu102.io.ethernet


import ultrascaleplus.io.ethernet._


object SFP0 {

  /**
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  val TX = Seq[String]("B6", "B5", "B13")

  /**
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   */
  val RX = Seq[String]("B2", "B1")

}
