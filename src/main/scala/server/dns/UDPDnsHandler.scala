package server.dns

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import payload.Message
import records._
import io.netty.buffer.Unpooled
import configs.ConfigService
import io.netty.channel.DefaultAddressedEnvelope

class UDPDnsHandler extends SimpleChannelInboundHandler[Object] {

  val logger = LoggerFactory.getLogger("app")
  val UdpResponseMaxSize = ConfigService.config.getInt("udpResponseMaxSize")
  val truncateUDP = ConfigService.config.getBoolean("truncateUDP")

  override def channelRead0(ctx: ChannelHandlerContext, e: Object) {
    logger.info("This is UDP.")
    if(ctx == null) logger.debug("ctx null")
    if(DefaultAddressedEnvelope.recipient()==null) logger.debug("ctxchannel null")
    if(ctx.channel().remoteAddress() == null) logger.debug("addrss null")
    val sourceIP = DefaultAddressedEnvelope.recipient().toString
    e match {
      case message: Message => {
        logger.info(message.toString)
        logger.info("Request bytes: " + message.toByteArray.toList.toString)
        
        val responses = truncateUDP match {
          case true => DnsResponseBuilderUDP(message, sourceIP, UdpResponseMaxSize)
          case _ => DnsResponseBuilder(message, sourceIP, UdpResponseMaxSize)
        }
        
        if(responses.length == 1) {
          logger.debug("Compressed response length: " + responses.head.length.toString)
          logger.debug("Compressed response bytes: " + responses.head.toList.toString)
        }
        
        responses.foreach(response => ctx.writeAndFlush(Unpooled.copiedBuffer(response), ctx.newPromise))
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
