package records

import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory

case class OPT(timetolive: Long = 0) extends AbstractRecord {

   val description = "OPT"

   lazy val toByteArray = Array[Byte]()

   def isEqualTo(any: Any) = any match {
      case _ => false
   }

   def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = (input._1 ++ toByteArray, input._2)
}

object OPT {

   val logger = LoggerFactory.getLogger("app")

   def apply(buf: ByteBuf, recordclass: Int, size: Int) = {
      new OPT()
   }
}