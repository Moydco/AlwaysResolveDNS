package client

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultAddressedEnvelope
import io.netty.channel.socket.DatagramPacket
import io.netty.buffer.Unpooled

import org.slf4j.LoggerFactory
import payload.Message
import records._
import configs.ConfigService
import java.net.SocketAddress
import java.net.InetAddress
import java.net.InetSocketAddress


class ClientUDPDnsHandler extends SimpleChannelInboundHandler[DefaultAddressedEnvelope[payload.Message, InetSocketAddress]] {

  val logger = LoggerFactory.getLogger("app")

  override def channelRead0(ctx: ChannelHandlerContext, e: DefaultAddressedEnvelope[payload.Message, InetSocketAddress]) {
    logger.debug("This is UDP.")
    
    e.content match {
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
