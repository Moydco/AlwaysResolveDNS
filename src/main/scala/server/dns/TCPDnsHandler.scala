/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
 * This file incorporates work covered by the following copyright and permission notice:
 *
 * Copyright 2012 silenteh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("Unexpected exception from downstream in tcp response: " + cause)
    logger.error(cause.getMessage)
    logger.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(cause))

    // Non serve chiudere il canale con l'udp!
    // https://github.com/netty/netty/blob/master/example/src/main/java/io/netty/example/qotm/QuoteOfTheMomentServerHandler.java
  }
}
