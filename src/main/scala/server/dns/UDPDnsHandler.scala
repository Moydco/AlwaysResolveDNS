package server.dns

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import payload.Message
import records._
import io.netty.buffer.Unpooled
import configs.ConfigService
import java.net.SocketAddress
import java.net.InetAddress
import java.net.InetSocketAddress
import io.netty.channel.DefaultAddressedEnvelope
import io.netty.channel.socket.DatagramPacket

class UDPDnsHandler extends SimpleChannelInboundHandler[DefaultAddressedEnvelope[payload.Message, InetSocketAddress]] {

  val logger = LoggerFactory.getLogger("app")
  val UdpResponseMaxSize = ConfigService.config.getInt("udpResponseMaxSize")
  val truncateUDP = ConfigService.config.getBoolean("truncateUDP")

  override def channelRead0(ctx: ChannelHandlerContext, e: DefaultAddressedEnvelope[payload.Message, InetSocketAddress]) {
    logger.debug("This is UDP.")
    
    val sourceIP = e.sender.toString
    e.content match {
      case message: Message => {
        logger.debug(message.toString)
        logger.debug("Request bytes: " + message.toByteArray.toList.toString)
        
        val responses = truncateUDP match {
          case true => DnsResponseBuilderUDP(message, sourceIP, UdpResponseMaxSize)
          case _ => DnsResponseBuilder(message, sourceIP, UdpResponseMaxSize)
        }
        
        if(responses.length == 1) {
          logger.debug("Compressed response length: {}", responses.head.length.toString)
          logger.debug("Compressed response bytes: {}", responses.head.toList.toString)
        }

        responses.foreach(response => ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response), e.sender)))
      }
      case _ => {
        logger.error("Unsupported message type")
      }
    }
  }

  override def exceptionCaught( ctx: ChannelHandlerContext,  cause: Throwable) {
    logger.debug("Unexpected exception from downstream."+cause)
    ctx.close()
  }
}
