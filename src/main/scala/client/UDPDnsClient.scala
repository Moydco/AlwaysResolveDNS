package client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption

import initializers.ClientUDPDnsInitializer
import java.net.InetSocketAddress
import payload.Message

class UDPDnsClient {
  val udpGroup = new NioEventLoopGroup()
  val bootstrap = new Bootstrap()
  bootstrap.group(udpGroup)
  bootstrap.channel(classOf[NioDatagramChannel])
  bootstrap.handler(new ClientUDPDnsInitializer)
  bootstrap.option(ChannelOption.SO_RCVBUF.asInstanceOf[ChannelOption[Any]], 65536)
  bootstrap.option(ChannelOption.SO_SNDBUF.asInstanceOf[ChannelOption[Any]], 65536)
  bootstrap.option(ChannelOption.SO_REUSEADDR.asInstanceOf[ChannelOption[Any]], true)
  bootstrap.option(ChannelOption.SO_BROADCAST.asInstanceOf[ChannelOption[Any]], false)
                
  val channel = bootstrap.bind(new InetSocketAddress(0))
  
  /*def send(address: String, port: Int, message: Message) = {
    val bufferedMessage = ChannelBuffers.copiedBuffer(message.toCompressedByteArray(Array(), Map())._1)
    val future = channel.write(bufferedMessage, new InetSocketAddress(address, port))
  }*/
  
  def send(address: String, port: Int, message: Message)(callback: ChannelFuture => Unit) = {
    val bufferedMessage = Unpooled.copiedBuffer(message.toCompressedByteArray(Array(), Map())._1)
    val future = channel.channel().writeAndFlush(bufferedMessage, channel.channel().newPromise())
    future.addListener(new ChannelFutureListener() {
      override def operationComplete(cf: ChannelFuture) = callback(cf)
    })
  }
  
  def stop = {
    channel.channel().close
    udpGroup.shutdownGracefully()
  }
  
}