package cn.wycode.lambda.proxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

fun main(args: Array<String>) {
    LambdaProxy(1594).run()
}


class LambdaProxy(val port:Int){

    @Throws(Exception::class)
    fun run(){
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        try {
            val inboundServer = ServerBootstrap()
            inboundServer.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .handler(LoggingHandler(LogLevel.INFO))
                    .childHandler(ProxyInboundInitializer())
                    .childOption(ChannelOption.AUTO_READ, false)

            val ch = inboundServer.bind(port).sync().channel()
            println("proxy start on $port")
            ch.closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}