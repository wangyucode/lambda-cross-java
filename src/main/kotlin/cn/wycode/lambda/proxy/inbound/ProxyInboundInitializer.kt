package cn.wycode.lambda.proxy.inbound

import cn.wycode.lambda.proxy.config.AliyunConfig
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class ProxyInboundInitializer(val aliyunConfig: AliyunConfig) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
//        p.addLast(LoggingHandler(LogLevel.INFO))
        p.addLast(HttpRequestDecoder())
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(HttpObjectAggregator(2097152))
        p.addLast(HttpResponseEncoder())
        // Remove the following line if you don't want automatic content compression.
//        p.addLast(new HttpContentCompressor());
        p.addLast(ProxyInboundHandler(aliyunConfig))
    }

}
