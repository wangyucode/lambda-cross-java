package cn.wycode.lambda.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpObject

class HttpClientHandler: SimpleChannelInboundHandler<HttpObject>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        println(msg)
    }

}
