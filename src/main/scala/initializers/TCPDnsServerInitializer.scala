package initializers

import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.codec.string.StringDecoder
import scalaframes.UDPDnsMessageDecoder
import org.slf4j.LoggerFactory
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import scalaframes.TCPDnsMessageDecoder
import server.dns.TCPDnsHandler

class TCPDnsServerInitializer extends ChannelInitializer[SocketChannel] {

  val logger = LoggerFactory.getLogger("app")

  def initChannel(ch: SocketChannel): Unit = {
      logger.debug("Initializing TCP.........")
      val pipeline = ch.pipeline()

      val frameDecoder = new TCPDnsMessageDecoder
      pipeline.addLast("framer", frameDecoder)
      //pipeline.addLast("decoder", new StringDecoder)
      //pipeline.addLast("encoder", new StringEncoder)
      pipeline.addLast("dns_handler",new TCPDnsHandler)
      pipeline
  }

}
