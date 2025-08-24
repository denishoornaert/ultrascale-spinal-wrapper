package ultrascaleplus.bus.amba.axi4.sim


import scala.collection.mutable


case class DiscreteHistogram() {

  private val map: mutable.Map[Int, Int] = mutable.Map[Int, Int]()

  def add(value: Int): Unit = {
    if (!(this.map contains value)) {
      this.map += (value -> 0)
    }
    this.map(value) += 1
  }

  def toSeq(): Seq[Int] = {
    val upperBound: Int = this.map.keysIterator.max+1
    return Seq.tabulate(upperBound)(
      e => if (this.map contains e) this.map(e) else 0
    )
  }

  def distribution(): Seq[Double] = {
    val serialized = this.toSeq
    val sum = serialized.sum.toDouble
    return serialized.map(e => e/sum).toSeq
  }

}
