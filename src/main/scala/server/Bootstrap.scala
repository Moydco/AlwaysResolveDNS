package server

import io.netty.bootstrap.ServerBootstrap
import java.util.concurrent.Executors
import java.net.InetSocketAddress
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import java.net.InetAddress
import initializers.UDPDnsServerInitializer
import initializers.TCPDnsServerInitializer
import configs.ConfigService
import messaging.Rabbit
import org.slf4j.LoggerFactory
import scala.Boolean

object BootstrapDNS {
  val logger = LoggerFactory.getLogger("app")  
  // Per il messaging
  val messagingThread = new Thread(new Rabbit())
  
  // val executorTCPBoss = Executors.newCachedThreadPool//Executors.newFixedThreadPool(cores)
  // val executorTCPWorker = Executors.newCachedThreadPool//Executors.newFixedThreadPool(cores)
  // val executorUDP = Executors.newCachedThreadPool//Executors.newFixedThreadPool(cores)

  val tcpBossGroup = new NioEventLoopGroup()
  val tcpWorkerGroup = new NioEventLoopGroup()
  val udpGroup = new NioEventLoopGroup()
  
  
  // ### TCP
  // bootstraps so they can be closed down gracefully
  //val tcpFactory = new NioServerSocketChannelFactory(executorTCPBoss, executorTCPWorker)//new NioServerSocketChannelFactory(executorTCP, executorTCP)
  val tcpBootstrap = new ServerBootstrap()
  
  // ### UDP
  val udpBootstrap = new Bootstrap()
  
  val httpServerAddress = ConfigService.config.getString("httpServerAddress")
  val httpServerPort = ConfigService.config.getInt("httpServerPort")

  val dnsServerIp = ConfigService.config.getString("dnsServerIp")
  
  // Starts both services
  def start() {
    startTCP
    startUDP
    startRabbit
  } 
  
  def stop() {
    stopTCP
    stopUDP
    stopRabbit
  }
  
  private def startRabbit() {
    messagingThread.start()
  }
  
  private def startTCP() {
    tcpBootstrap.group(tcpBossGroup, tcpWorkerGroup)
    tcpBootstrap.channel(classOf[NioServerSocketChannel])
    tcpBootstrap.childHandler(new TCPDnsServerInitializer())
    // Bind and start to accept incoming connections.
    // we need to refactor this to set it up via config
    tcpBootstrap.localAddress(new InetSocketAddress(dnsServerIp, 53))

    // Questa libreria fa cagare al cazzo... https://gist.github.com/fbettag/3876463
    tcpBootstrap.childOption(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
    tcpBootstrap.childOption(ChannelOption.SO_RCVBUF.asInstanceOf[ChannelOption[Any]], 1048576)
    // Non sono sicuro che servano tutte quelle chiamate alla fine, al limite fermarsi al primo sync
    tcpBootstrap.bind(new InetSocketAddress(dnsServerIp, 53)).sync().channel().closeFuture().sync()
  }
  
  private def startUDP() {
    
    // bind the server to an address and port
    // we need to refactor this to set it up via config
    //bootstrap.bind(new InetSocketAddress(InetAddress.getByName("192.168.1.100"), 8080));
    //udpBootstrap.setOption("localAddress", new InetSocketAddress(InetAddress.getByName(dnsServerIp), 53));
    udpBootstrap.group(udpGroup)
    udpBootstrap.channel(classOf[NioDatagramChannel])
    udpBootstrap.handler(new UDPDnsServerInitializer())

    // queste options secondo me non servono
    udpBootstrap.option(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
 	  udpBootstrap.option(ChannelOption.SO_RCVBUF.asInstanceOf[ChannelOption[Any]], 1048576)
    
    udpBootstrap.bind(new InetSocketAddress(dnsServerIp, 53)).sync().channel().closeFuture().await()
  }
  
  private def stopTCP() {
    logger.debug("Chiamato STOP")
    //tcpBootstrap.releaseExternalResources()
  }
  
  private def stopUDP() {
    logger.debug("Chiamato STOP")
    //udpBootstrap.releaseExternalResources()
  }

  private def stopRabbit() {
    messagingThread.stop
  }
  
}