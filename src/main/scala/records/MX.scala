package records

import io.netty.buffer.ByteBuf
import payload.Name
import org.slf4j.LoggerFactory
import payload.RRData

case class MX(preference: Int, record: List[Array[Byte]], timetolive: Long /*vedere commento record srv*/) extends AbstractRecord {

  val description = "MX"
  
  def toByteArray = RRData.shortToBytes(preference.toShort) ++ Name.toByteArray(record)
  
  def isEqualTo(any: Any) = any match {
    case r: MX => r.preference == preference && r.record.toArray.deep == record.toArray.deep
    case _ => false
  }
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = 
    Name.toCompressedByteArray(record :+ Array[Byte](), (input._1 ++ RRData.shortToBytes(preference.toShort), input._2))
}

object MX {
  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    val preference = buf.readUnsignedShort
    val record = Name.parse(buf, offset)
    new MX(preference, record, 60)
  }
}