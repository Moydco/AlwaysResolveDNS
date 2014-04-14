package records

import io.netty.buffer.ByteBuf
import payload.Name
import org.slf4j.LoggerFactory
import payload.RRData

case class SRV(
  priority: Long,
  weight: Long,
  port: Long,
  target: List[Array[Byte]],
  timetolive: Long //niente valore di default perchè scala rompe il cazzo. Impostato a mano sotto
) extends AbstractRecord {
  
  val description = "SRV"
  
  def isEqualTo(any: Any) = any match {
    case r: SRV => r.priority == priority && r.weight == weight && r.port == port && r.target == target
    case _ => false
  }
    
  def toByteArray = RRData.shortToBytes(priority.toShort) ++ RRData.shortToBytes(weight.toShort) ++ RRData.shortToBytes(port.toShort) ++ Name.toByteArray(target)
    
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
    Name.toCompressedByteArray(target :+ Array[Byte](), (input._1 ++ RRData.shortToBytes(priority.toShort) ++ RRData.shortToBytes(weight.toShort) ++ RRData.shortToBytes(port.toShort), input._2))
    // val targetBytes = Name.toCompressedByteArray(target :+ Array[Byte](), input)
    // (  RRData.shortToBytes(priority.toShort) ++ RRData.shortToBytes(weight.toShort) ++ RRData.shortToBytes(port.toShort) ++ targetBytes._1 , targetBytes._2 )
  }
}

object SRV {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    val priority = buf.readUnsignedShort
    val weight = buf.readUnsignedShort
    val port = buf.readUnsignedShort
    val target = Name.parse(buf, offset)

    new SRV(priority, weight, port, target, 60)
  }
}

