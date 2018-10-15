package cn.wycode.lambda.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest


class HttpClientInboundHandle : ChannelInboundHandlerAdapter() {


    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        println(msg)
        val inboundChannel = ctx.channel()
        val b = Bootstrap()

        b.group(inboundChannel.eventLoop())
                .channel(inboundChannel.javaClass)
                .handler(HttpClientOutboundHandle(inboundChannel))
        msg as FullHttpRequest
        val f = b.connect("baidu.com", 80)

        val outboundChannel = f.channel()
        outboundChannel.writeAndFlush(msg)
    }
}