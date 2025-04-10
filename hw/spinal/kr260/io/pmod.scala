package kr260.io.pmod


import ultrascaleplus.io.pmod._


object PMOD0 {

  /*
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   * H12 -> 0 upper part
   * E10 -> 2 upper part
   * D10 -> 4 upper part
   * C11 -> 6 upper part
   * B10 -> 1 lower part
   * E12 -> 3 lower part
   * D11 -> 5 lower part
   * B11 -> 7 lower part
   */
  val names = Seq[String]("H12", "B10", "E10", "E12", "D10", "D11", "C11", "B11")

}

object PMOD1 {

  /*
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   * J11 -> 0 upper part
   * J10 -> 2 upper part
   * K13 -> 4 upper part
   * K12 -> 6 upper part
   * H11 -> 1 lower part
   * G10 -> 3 lower part
   * F12 -> 5 lower part
   * F11 -> 7 lower part
   */
  val names = Seq[String]("J11", "J10", "K13", "K12", "H11", "G10", "F12", "F11")

}

object PMOD2 {

  /*
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   * AE12 -> 0 upper part
   * AF12 -> 2 upper part
   * AG10 -> 4 upper part
   * AH10 -> 6 upper part
   * AF11 -> 1 lower part
   * AG11 -> 3 lower part
   * AH12 -> 5 lower part
   * AH11 -> 7 lower part
   */
  val names = Seq[String]("AE12", "AF12", "AG10", "AH10", "AF11", "AG11", "AH12", "AH11")

}

object PMOD3 {

  /*
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   * AC12 -> 0 upper part
   * AD12 -> 2 upper part
   * AE10 -> 4 upper part
   * AF10 -> 6 upper part
   * AD11 -> 1 lower part
   * AD10 -> 3 lower part
   * AA11 -> 5 lower part
   * AA10 -> 7 lower part
   */
  val names = Seq[String]("AC12", "AD12", "AE10", "AF10", "AD11", "AD10", "AA11", "AA10")

}

