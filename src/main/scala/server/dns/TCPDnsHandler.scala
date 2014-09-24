package server.dns

import configs.ConfigService
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import org.slf4j.LoggerFactory
import payload.{Message, RRData}

class TCPDnsHandler extends SimpleChannelInboundHandler[Object] {

	val logger = LoggerFactory.getLogger("app")
	val dnsServerIp = ConfigService.config.getString("dnsServerIp")

	override def channelRead0(ctx: ChannelHandlerContext, e: Object) {
		logger.debug("This request is brought to you by TCP")
		val sourceIP = ctx.channel().remoteAddress.toString
		logger.debug("Sender: " + sourceIP)

		/**
		 * Se il messaggio arriva con il sender = indirizzo del server, non fare niente perchè:
		 * 1. E' un pacchetto malefico probabilmente.
		 * 2. Il server lo fa rimbalzare su se stesso facendo un dos (?).
		 */
		logger.debug(ctx.channel().remoteAddress.toString)
		logger.debug(dnsServerIp)
		if (sourceIP == dnsServerIp) {
			logger.error("Sender address equals server address")
			return
		}

		e match {
			case message: Message => {
				val responses = DnsResponseBuilder(message, sourceIP)

				if (responses.length == 1) {
					logger.debug("Compressed response length: {}", responses.head.length.toString)
					logger.debug("Compressed response bytes: {}", responses.head.toList.toString)
				}

				Array.tabulate(responses.length) { i =>
					val response = responses(i)
					if (i < responses.length - 1) {
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
