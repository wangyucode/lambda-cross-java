package cn.wycode.lambda.proxy.outbound

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpContentDecompressor
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

class ProxyOutboundInitializer(val inboundChannel: Channel) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        val sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
        p.addLast(sslContext.newHandler(ch.alloc()))
//        p.addLast(LoggingHandler(LogLevel.INFO))
        p.addLast(HttpClientCodec())
        // Remove the following line if you don't want automatic content decompression.
//        p.addLast(HttpContentDecompressor())
        // Uncomment the following line if you don't want to handle HttpContents.
        p.addLast(HttpObjectAggregator(2097152))
        p.addLast(ProxyOutboundHandler(inboundChannel))
    }
}