package cn.wycode.lambda.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.net.URI

fun main(args: Array<String>) {
    Client("").run()
}

class Client(url: String) {

    private val uri: URI = URI(url)

    @Throws(Exception::class)
    fun run() {
        if (uri.host == null) {
            return
        }
        val host = uri.host
        val scheme = if (uri.scheme == null) "http" else uri.scheme
        val port = if (uri.port == -1) {
            when {
                "http".equals(scheme, true) -> 80
                "https".equals(scheme, true) -> 443
                else -> return
            }
        } else {
            uri.port
        }
        // Configure SSL context if necessary.
        val sslCtx: SslContext? =
                if ("https".equals(scheme, true)) {
                    SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
                } else {
                    null
                }

        val workerGroup = NioEventLoopGroup()
        try {
            val b = Bootstrap()
            b.group(workerGroup)
                    .channel(NioSocketChannel::class.java)
                    .handler(ClientInitializer(sslCtx))
            // Make the connection attempt.
            val ch = b.connect(host, port).sync().channel()

            // Prepare the HTTP request.
            val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.rawPath)
            request.headers().set(HttpHeaderNames.HOST, host)
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
            val msg = "Hello!".toByteArray()
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, msg.size)
            request.content().writeBytes(msg)

            // Send the HTTP request.
            ch.writeAndFlush(request)

            // Wait for the server to close the connection.
            ch.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}