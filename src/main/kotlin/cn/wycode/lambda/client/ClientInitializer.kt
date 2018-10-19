package cn.wycode.lambda.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpContentDecompressor
import io.netty.handler.ssl.SslContext


class ClientInitializer(private val sslContext: SslContext?) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        if (sslContext != null) {
            p.addLast(sslContext.newHandler(ch.alloc()))
        }
        p.addLast(HttpClientCodec())
        // Remove the following line if you don't want automatic content decompression.
        p.addLast(HttpContentDecompressor())
        // Uncomment the following line if you don't want to handle HttpContents.
        //p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(HttpClientHandler())
    }
}