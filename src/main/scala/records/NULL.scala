package records

import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory

case class NULL(record: Array[Byte], timetolive: Long = 60) extends AbstractRecord {

  val description = "NULL"
  
  lazy val toByteArray = record

  def isEqualTo(any: Any) = any match {
    case r: NULL => r.record.deep == record.deep
    case _ => false
  }
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = (input._1 ++ toByteArray, input._2)
}

object NULL {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int) = {
    val record = recordclass match {
      // IN
      case 1 => {
        val marray = new Array[Byte](size) // A 128 bit IPv6 address = network byte order (high-order byte first).
        buf.readBytes(marray);
        marray
      }
      // *
      case 255 => null // not implemented yet
      case _ => throw new Error("Unknown record type")
    }
    new NULL(record)
  }
}
