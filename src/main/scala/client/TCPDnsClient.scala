package client

import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.bootstrap.Bootstrap

import scala.concurrent.ExecutionContext.Implicits.global
import java.net.InetSocketAddress
import payload.Message
import initializers.ClientTCPDnsInitializer
import payload.RRData
import scala.concurrent.Future
import org.slf4j.LoggerFactory

class TCPDnsClient {
  
  val logger = LoggerFactory.getLogger("app")

  //def send(address: String, port: Int, message: Message)(callback: ChannelFuture => Unit) = {
    
  
  def send(address: String, port: Int, message: Message)(callback: ChannelFuture => Unit) = {
    val tcpGroup = new EpollEventLoopGroup
    val bootstrap = new Bootstrap()
    bootstrap.group(tcpGroup)
    bootstrap.channel(classOf[EpollSocketChannel])
    bootstrap.handler(new ClientTCPDnsInitializer)
    
    val connectionFuture = bootstrap.connect(new InetSocketAddress(address, port))
    connectionFuture.addListener(new ChannelFutureListener() {
      override def operationComplete(cf: ChannelFuture) = {
        val channel = cf.channel()
        val messageBytes = message.toCompressedByteArray(Array(), Map())._1
        val bufferedMessage = Unpooled.copiedBuffer(RRData.shortToBytes(messageBytes.length.toShort) ++ messageBytes)
        val future = channel.writeAndFlush(bufferedMessage, channel.newPromise())
        future.addListener(new ChannelFutureListener() {
          override def operationComplete(cf: ChannelFuture) = {
            callback(cf)
            channel.closeFuture.addListener(new ChannelFutureListener() {
              override def operationComplete(cf: ChannelFuture) = Future {
                channel.close
                tcpGroup.shutdownGracefully()
              }
            })
          }
        })
      }
    })

    // val messageBytes = message.toCompressedByteArray(Array(), Map())._1
    // val bufferedMessage = Unpooled.copiedBuffer(RRData.shortToBytes(messageBytes.length.toShort) ++ messageBytes)
    // var lastWriteFuture: ChannelFuture = null;
    // val connection = bootstrap.connect(new InetSocketAddress(address, port)).sync.channel
    // lastWriteFuture = connection.writeAndFlush(bufferedMessage, connection.newPromise())
    // connection.closeFuture.sync

    // if (lastWriteFuture != null) {
    //     lastWriteFuture.sync();
    // }
    // connection.close
    // tcpGroup.shutdownGracefully()
  }
}