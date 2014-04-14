package records

import io.netty.buffer.ByteBuf
import scala.collection.mutable.ArrayBuffer
import org.slf4j.LoggerFactory
import scala.annotation.tailrec

case class TXT(strings: Array[Array[Byte]], timetolive: Long = 60) extends AbstractRecord {

  def this(strings: Array[String]) = this(strings.map(_.getBytes))
  
  val logger = LoggerFactory.getLogger("app")

  val description = "TXT"
  
  lazy val getStrings = strings.map(new String(_, "UTF-8"))
    
  def toByteArrayTemp = strings.foldRight(Array[Byte]()) {case (bytes, total) => bytes ++ total}

  def toByteArray = Array(toByteArrayTemp.length.toByte) ++ toByteArrayTemp
  
  def isEqualTo(any: Any) = any match {
    case r: TXT => r.strings.deep == strings.deep
    case _ => false
  }
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = (input._1 ++ toByteArray, input._2)
}

object TXT {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int) = {
    val part = buf.readSlice(size)
    val strings = readPart(part, Array[Array[Byte]]())
    new TXT(strings)
  }

  @tailrec
  private def readPart(buf: ByteBuf, strings: Array[Array[Byte]]): Array[Array[Byte]] =
    if (!buf.isReadable) strings
    else {
      val length = buf.readUnsignedByte
      // throw exception string must be MAX 255
      if (AbstractRecord.MAX_STRING_LENGTH < length)
        throw new Error("String length exceeds 255")
      val marray = new Array[Byte](length)
      buf.readBytes(marray);
      readPart(buf, strings :+ marray)
    }
}
