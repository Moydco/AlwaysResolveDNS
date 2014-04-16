package initializers

import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.codec.string.StringDecoder
import scalaframes.UDPDnsMessageDecoder
import org.slf4j.LoggerFactory
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.nio.NioDatagramChannel
import server.dns.UDPDnsHandler

class UDPDnsServerInitializer extends ChannelInitializer[NioDatagramChannel] {

  val logger = LoggerFactory.getLogger("app")
  
  def initChannel(ch: NioDatagramChannel): Unit = {
    logger.debug("Initializing UDP server.........")
    // Create a default pipeline implementation.
    val pipeline = ch.pipeline()

    // Add the text line codec combination first,
    val frameDecoder = new UDPDnsMessageDecoder
    pipeline.addLast("framer", frameDecoder)
    //pipeline.addLast("decoder", new StringDecoder)
    //pipeline.addLast("encoder", new StringEncoder)
    pipeline.addLast("dns_handler",new UDPDnsHandler)

    pipeline
  }


}
