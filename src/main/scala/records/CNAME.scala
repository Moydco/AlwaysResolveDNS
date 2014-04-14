package records

import io.netty.buffer.ByteBuf
import payload.Name
import org.slf4j.LoggerFactory

case class CNAME(record: List[Array[Byte]], timetolive: Long /*vedere commento record srv*/) extends AbstractRecord {

  val description = "CNAME"

  def toByteArray = Name.toByteArray(record)
  
  def isEqualTo(any: Any) = any match {
    case r: CNAME => r.record.toArray.deep == record.toArray.deep
    case _ => false
  }
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = Name.toCompressedByteArray(record, input)
  
  override def toString = "CNAME(%s)".format(record.map(new String(_, "UTF-8")).mkString("."))
}

object CNAME {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    val record = recordclass match {
      // IN
      case 1 => Name.parse(buf, offset)
      // *
      case 255 => null // not implemented yet
      case _ => throw new Error("Error: Unknown record class")
    }
    new CNAME(record, 60)
  }
}