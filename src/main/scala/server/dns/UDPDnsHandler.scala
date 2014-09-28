package server.dns

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

class UDPDnsHandler extends SimpleChannelInboundHandler[DefaultAddressedEnvelope[payload.Message, InetSocketAddress]] {

	val logger = LoggerFactory.getLogger("app")
	val UdpResponseMaxSize = ConfigService.config.getInt("udpResponseMaxSize")
	val truncateUDP = ConfigService.config.getBoolean("truncateUDP")
	val dnsServerIp = ConfigService.config.getString("dnsServerIp")

	override def channelRead0(ctx: ChannelHandlerContext, e: DefaultAddressedEnvelope[payload.Message, InetSocketAddress]) {
		logger.debug("This is UDP.")

		val sourceIP = e.sender.toString
		logger.debug("Sender: " + sourceIP)

		/**
		 * Se il messaggio arriva con il sender = indirizzo del server, non fare niente perchè:
		 * 1. E' un pacchetto malefico probabilmente.
		 * 2. Il server lo fa rimbalzare su se stesso facendo un dos (?).
		 */
		logger.debug(e.sender().getAddress.getHostAddress)
		logger.debug(dnsServerIp)
		if(e.sender().getAddress.getHostAddress == dnsServerIp) {
			logger.error("Sender address equals server address")
			return
		}

		e.content match {
			case message: Message => {
				logger.debug(message.toString)
				logger.debug("Request bytes: " + message.toByteArray.toList.toString)

				val responses = truncateUDP match {
					case true => DnsResponseBuilderUDP(message, sourceIP, UdpResponseMaxSize)
					case _ => DnsResponseBuilder(message, sourceIP, UdpResponseMaxSize)
				}

				if (responses.length == 1) {
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

	override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		logger.error("Unexpected exception from downstream." + cause)
		logger.error(cause.getMessage)
		logger.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(cause))

		// Non serve chiudere il canale con l'udp!
		// https://github.com/netty/netty/blob/master/example/src/main/java/io/netty/example/qotm/QuoteOfTheMomentServerHandler.java
	}
}
