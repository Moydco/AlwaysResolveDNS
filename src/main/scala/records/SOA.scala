package records

import io.netty.buffer.ByteBuf
import payload.Name
import org.slf4j.LoggerFactory
import payload.RRData

case class SOA(
  mname: List[Array[Byte]], 
  rname: List[Array[Byte]],
  serial: Long,
  refresh: Long,
  retry: Long,
  expire: Long,
  minimum: Long,
  timetolive: Long //ignorare questo campo !!!!
) extends AbstractRecord {
  
  val description = "SOA"
  
  def isEqualTo(any: Any) = any match {
    case r: SOA => r.mname.toArray.deep == mname.toArray.deep && r.rname.toArray.deep == rname.toArray.deep && r.serial == serial &&
      r.refresh == refresh && r.retry == retry && r.expire == expire && r.minimum == minimum
    case _ => false
  }
    
  def toByteArray = Name.toByteArray(mname) ++ Name.toByteArray(rname) ++ RRData.intToBytes(serial.toInt) ++
    RRData.intToBytes(refresh.toInt) ++ RRData.intToBytes(retry.toInt) ++ RRData.intToBytes(expire.toInt) ++
    RRData.intToBytes(minimum.toInt)
    
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
    val mnameBytes = Name.toCompressedByteArray(mname :+ Array[Byte](), input)
    val rnameBytes = Name.toCompressedByteArray(rname :+ Array[Byte](), mnameBytes)
    
    (rnameBytes._1 ++ RRData.intToBytes(serial.toInt) ++ RRData.intToBytes(refresh.toInt) ++ RRData.intToBytes(retry.toInt) ++ 
    RRData.intToBytes(expire.toInt) ++ RRData.intToBytes(minimum.toInt), rnameBytes._2)
  }
}

object SOA {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    val mname = Name.parse(buf, offset)
    val rname = Name.parse(buf, offset)
    val serial = buf.readUnsignedInt
    val refresh = buf.readUnsignedInt
    val retry = buf.readUnsignedInt
    val expire = buf.readUnsignedInt
    val minimum = buf.readUnsignedInt
    new SOA(mname, rname, serial, refresh, retry, expire, minimum, 60)
  }
}

