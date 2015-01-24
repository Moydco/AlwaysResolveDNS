/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package initializers

import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.channel.epoll.EpollSocketChannel

import org.slf4j.LoggerFactory
import scalaframes.TCPDnsMessageDecoder
import server.dns.TCPDnsHandler

class TCPDnsServerInitializer extends ChannelInitializer[EpollSocketChannel] {

  val logger = LoggerFactory.getLogger("app")

  def initChannel(ch: EpollSocketChannel): Unit = {
      logger.debug("Initializing TCP server.........")
      val pipeline = ch.pipeline()

      val frameDecoder = new TCPDnsMessageDecoder
      pipeline.addLast("framer", frameDecoder)
      //pipeline.addLast("decoder", new StringDecoder)
      //pipeline.addLast("encoder", new StringEncoder)
      pipeline.addLast("dns_handler",new TCPDnsHandler)
      pipeline
  }

}
