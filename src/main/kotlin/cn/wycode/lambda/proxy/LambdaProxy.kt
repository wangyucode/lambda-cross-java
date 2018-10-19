package cn.wycode.lambda.proxy

import cn.wycode.lambda.proxy.config.AliyunConfig
import cn.wycode.lambda.proxy.inbound.ProxyInboundInitializer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.lang.RuntimeException

fun main(args: Array<String>) {
    if(args.isEmpty()){
        throw RuntimeException("请提供阿里云函数地址")
    }
    val url = args[0]
    println(url)

    LambdaProxy(1594, AliyunConfig(url)).run()
}


class LambdaProxy(private val port: Int, private val aliyunConfig: AliyunConfig) {

    @Throws(Exception::class)
    fun run() {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        try {
            val inboundServer = ServerBootstrap()
            inboundServer.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(ProxyInboundInitializer(aliyunConfig))
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