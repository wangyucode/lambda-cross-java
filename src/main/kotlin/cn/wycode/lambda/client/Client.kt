package cn.wycode.lambda.client

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.internal.ChannelUtils
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import java.net.InetAddress
import java.net.InetSocketAddress

fun main(args: Array<String>) {
//    Client(1080).run()
    val b = Bootstrap()
    b.group(NioEventLoopGroup())
    b.channel(NioSocketChannel::class.java)
    b.option(ChannelOption.SO_KEEPALIVE,true)
    b.handler(object : ChannelInitializer<SocketChannel>() {
        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(HttpHandle())
        }
    })

    val f = b.connect("127.0.0.1",8080).sync()
    val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/")
    request.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE)
    f.channel().write(request)
    f.channel().flush()
    f.channel().closeFuture().sync()
}

class HttpHandle : ChannelInboundHandlerAdapter(){

    override fun channelActive(ctx: ChannelHandlerContext?) {
        println("channelActive")
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        println("channelReadComplete")
        ctx.flush()
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        println(msg)
        msg as ByteBuf
        println(msg.toString(CharsetUtil.UTF_8))
    }
}

class Client(val port: Int) {
    @Throws(Exception::class)
    fun run() {
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()

        try {
            val b = ServerBootstrap() // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java) // (3)
                    .handler(LoggingHandler(LogLevel.INFO))
                    .childHandler(object : ChannelInitializer<SocketChannel>() { // (4)
                        @Throws(Exception::class)
                        public override fun initChannel(ch: SocketChannel) {
                            ch.pipeline()
                                    .addLast(LoggingHandler(LogLevel.INFO))
                                    .addLast(HttpRequestDecoder())
                                    .addLast(HttpObjectAggregator(65536))
                                    .addLast(HttpResponseEncoder())
                                    .addLast(HttpClientInboundHandle())
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // (6)

            // Bind and start to accept incoming connections.
            val f = b.bind(port).sync() // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}