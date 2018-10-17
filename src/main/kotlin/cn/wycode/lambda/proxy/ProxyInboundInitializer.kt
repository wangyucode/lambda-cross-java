package cn.wycode.lambda.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class ProxyInboundInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        p.addLast(LoggingHandler(LogLevel.INFO))
//        p.addLast(HttpRequestDecoder())
        // Uncomment the following line if you don't want to handle HttpChunks.
        //p.addLast(HttpObjectAggregator(1048576))
//        p.addLast(HttpResponseEncoder())
        // Remove the following line if you don't want automatic content compression.
        //p.addLast(new HttpContentCompressor());
        p.addLast(ProxyInboundHandler())
    }

}
