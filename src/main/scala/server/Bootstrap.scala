package server

import io.netty.bootstrap.ServerBootstrap
import io.netty.bootstrap.Bootstrap
import io.netty.channel.epoll.EpollDatagramChannel
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.epoll.EpollChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.ChannelOption

import java.util.concurrent.Executors
import java.util.Timer
import java.util.Date
import java.net.InetSocketAddress
import initializers.UDPDnsServerInitializer
import initializers.TCPDnsServerInitializer
import configs.ConfigService
import httpSync._
import messaging.Rabbit
import org.slf4j.LoggerFactory

object BootstrapDNS {
  val logger = LoggerFactory.getLogger("app")  
  // Per il messaging
  val messagingThread = new Thread(new Rabbit())
  val timerNotifyQuery = new Timer()

  val tcpBossGroup = new EpollEventLoopGroup()
  val tcpWorkerGroup = new EpollEventLoopGroup()
  //val udpGroup = new NioEventLoopGroup()
  
  
  // ### TCP
  // bootstraps so they can be closed down gracefully
  val tcpBootstrap = new ServerBootstrap()
  
  // ### UDP
  val udpBootstrap = new Bootstrap()

  val dnsServerIp = ConfigService.config.getString("dnsServerIp")
  
  // Starts both services
  def start() {
    startTCP
    startUDP
    startRabbit
    startTimer
  }
  
  def stop() {
    stopTCP
    stopUDP
    stopRabbit
    //stopTimer
  }
  
  private def startRabbit() {
    messagingThread.start()
  }

  private def startTimer() {
    val TIMER = ConfigService.config.getLong("queryCountTimer")
    logger.debug("Timer started")
    timerNotifyQuery.scheduleAtFixedRate(new QueryCountNotifier(), new Date(), TIMER)
  }
  
  private def startTCP() {
    tcpBootstrap.group(tcpBossGroup, tcpWorkerGroup)
    tcpBootstrap.channel(classOf[EpollServerSocketChannel])
    tcpBootstrap.childHandler(new TCPDnsServerInitializer())
    // Bind and start to accept incoming connections.
    // we need to refactor this to set it up via config
    tcpBootstrap.localAddress(new InetSocketAddress(dnsServerIp, 53))

    // Questa libreria fa cagare al cazzo... https://gist.github.com/fbettag/3876463
    // https://github.com/kxbmap/netty4-example-scala/blob/master/src/main/scala/com/github/kxbmap/netty/example/echo/EchoServer.scala
    tcpBootstrap.option(EpollChannelOption.SO_REUSEPORT, Boolean.box(true))
    tcpBootstrap.childOption(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
    tcpBootstrap.childOption(ChannelOption.SO_RCVBUF.asInstanceOf[ChannelOption[Any]], 1048576)
    // Non sono sicuro che servano tutte quelle chiamate alla fine, al limite fermarsi al primo sync
    tcpBootstrap.bind(new InetSocketAddress(dnsServerIp, 53)).sync()
  }
  
  private def startUDP() {
    
    // bind the server to an address and port
    // we need to refactor this to set it up via config
    //bootstrap.bind(new InetSocketAddress(InetAddress.getByName("192.168.1.100"), 8080));
    //udpBootstrap.setOption("localAddress", new InetSocketAddress(InetAddress.getByName(dnsServerIp), 53));
    //udpBootstrap.group(udpGroup)
    udpBootstrap.group(tcpBossGroup)
    udpBootstrap.channel(classOf[EpollDatagramChannel])
    udpBootstrap.handler(new UDPDnsServerInitializer())

    // queste options secondo me non servono
    //udpBootstrap.option(EpollChannelOption.TCP_NODELAY.asInstanceOf[EpollChannelOption[Any]], true)
 	  udpBootstrap.option(ChannelOption.SO_RCVBUF.asInstanceOf[ChannelOption[Any]], 1048576)
    udpBootstrap.option(EpollChannelOption.SO_REUSEPORT, Boolean.box(true))
    //try{
      udpBootstrap.bind(new InetSocketAddress(dnsServerIp, 53)).sync()
    // }
    // finally
    // {
    //   udpGroup.shutdownGracefully()
    // }
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