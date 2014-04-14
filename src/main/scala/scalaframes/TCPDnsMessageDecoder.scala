package scalaframes

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import payload.Header
import scala.collection.immutable.BitSet
import payload.Question
import payload.Message
import org.slf4j.LoggerFactory
import scala.annotation.tailrec
import java.util.List

class TCPDnsMessageDecoder extends ByteToMessageDecoder {
  val logger = LoggerFactory.getLogger("app")

  //@Override
  override def decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: List[Object]): Unit = {
    logger.debug("Reading message delivered by TCP")
    // 14 bytes: 2(length of the data) + 12(the minimum length in bytes of the header)
    if (buf.readableBytes() < 14) null
    else {
      // The length field was not received yet - return null.
      // This method will be invoked again when more packets are
      // received and appended to the buffer.

      // The length field is in the buffer.

      // Mark the current buffer position before reading the length field
      // because the whole frame might not be in the buffer yet.
      // We will reset the buffer position to the marked position if
      // there's not enough bytes in the buffer.
      //buf.markReaderIndex();

      // Read the length field.
      //val length = buf.readUnsigned
      //println(buf.readableBytes())
      //logger.debug(buf.readableBytes.toString)
      val length = buf.readUnsignedShort
      //logger.debug(buf.slice().readableBytes.toString)
      out.add(payload.Message(buf, 2))
    }

  }

  /*def fromBytesToString(buf: ChannelBuffer, length: Int) = {
    val marray = new Array[Byte](length)
    buf.readBytes(marray)
    new String(marray, "UTF-8")
  }*/

  /*def toBitArracy(byte: Int, size: Short): Array[Short] =
    Array.tabulate(size) { i => ((byte >> (size - i - 1)) % 2).toShort }*/

  // there is probably a better way via bit operations to calculate this.
  // not a tail recursion
  //def toInt(bits: Array[Short]): Int =
  //  if (bits.isEmpty) 0 else (bits.head * (scala.math.pow(2, bits.tail.length))).toInt + toInt(bits.tail)

}
