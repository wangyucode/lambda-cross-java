package cn.wycode.lambda.client

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil

fun main(args: Array<String>) {
    Client("baidu.com", 80).run()
}

class Client(val host: String, val port: Int) {

    @Throws(Exception::class)
    fun run() {
        val workerGroup = NioEventLoopGroup()
        try {
            val b = Bootstrap()
            b.group(workerGroup)
                    .channel(NioSocketChannel::class.java)
                    .handler(ClientInitializer())
            // Make the connection attempt.
            val ch = b.connect(host, port).sync().channel()

            // Prepare the HTTP request.
            val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
            request.headers().set(HttpHeaderNames.HOST, host)
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)

            // Send the HTTP request.
            ch.writeAndFlush(request)

            // Wait for the server to close the connection.
            ch.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}