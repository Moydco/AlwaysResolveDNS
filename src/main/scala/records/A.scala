package records

import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory
import payload.RRData

case class A(record: Long, timetolive: Long = 60) extends AbstractRecord {

  val description = "A"
  lazy val address = RRData.intToBytes(record.toInt).map(b => if (b < 0) b + 256 else b).reverse.mkString(".")

  def toByteArray = RRData.intToBytes(record.toInt)
  def addressToByteArray = Array[Byte]()
  
  def isEqualTo(any: Any) = any match {
    case r: A => r.record == record
    case _ => false
  }
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = (input._1 ++ toByteArray, input._2)

}

object A {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int) = {
    val record = recordclass match {
      // IN
      case 1 => buf.readUnsignedInt() //return a 32 bit Internet Address
      // *
      case 255 => 0L // not implemented yet
      case _ => throw new Error("Error: Unknown address format")
    }
    new A(record)
  }
  
}
