package server.dns

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import payload.Message
import payload.RRData
import records._
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import scala.Array.canBuildFrom

class TCPDnsHandler extends SimpleChannelInboundHandler[Object] {

  val logger = LoggerFactory.getLogger("app")

  override def channelRead0(ctx: ChannelHandlerContext, e: Object) {
    logger.info("This request is brought to you by TCP")
    val sourceIP = ctx.channel().remoteAddress.toString
    e match {
      case message: Message => {
        val responses = DnsResponseBuilder(message, sourceIP)

        if(responses.length == 1) {
          logger.debug("Compressed response length: " + responses.head.length.toString)
          logger.debug("Compressed response bytes: " + responses.head.toList.toString)
        }
        
        Array.tabulate(responses.length) {i =>
          val response = responses(i)
          if(i < responses.length - 1) {
            ctx.writeAndFlush(Unpooled.copiedBuffer(RRData.shortToBytes(response.length.toShort) ++ response))
          } else {
            ctx.writeAndFlush(Unpooled.copiedBuffer(RRData.shortToBytes(response.length.toShort) ++ response))
          .addListener(ChannelFutureListener.CLOSE)
          }
        }
      }
      case _ => {
        logger.error("Unsupported message type")
      }
    }
  }
}
