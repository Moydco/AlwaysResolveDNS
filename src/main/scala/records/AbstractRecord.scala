package records

// Togliere, non serve
import io.netty.buffer.ByteBuf

abstract class AbstractRecord {
  
  val description: String

  val timetolive: Long
  
  def toByteArray: Array[Byte]
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])): (Array[Byte], Map[String, Int])
  
  def isEqualTo(any: Any): Boolean
}

object AbstractRecord {
  lazy val MAX_STRING_LENGTH = 255
}