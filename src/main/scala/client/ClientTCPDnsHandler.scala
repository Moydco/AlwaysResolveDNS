package client

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelFutureListener
import io.netty.buffer.Unpooled

import org.slf4j.LoggerFactory
import payload.Message
import payload.RRData
import records._

class ClientTCPDnsHandler extends SimpleChannelInboundHandler[Object] {

  val logger = LoggerFactory.getLogger("app")

  override def channelRead0(ctx: ChannelHandlerContext, e: Object) {
    e match {
      case message: Message => {
        logger.debug("Response received")
        DNSClient.processResponse(message)
      }
      case _ => logger.debug("Error, error, error")
    }
  }

  override def exceptionCaught( ctx: ChannelHandlerContext,  cause: Throwable) {
    logger.debug("Exception caught")
    logger.error(cause.getMessage)
    logger.error(cause.getStackTraceString)
  }

}
