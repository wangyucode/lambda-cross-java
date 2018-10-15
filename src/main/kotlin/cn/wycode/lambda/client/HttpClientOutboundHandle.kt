package cn.wycode.lambda.client

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class HttpClientOutboundHandle(val inboundChannel: Channel) :ChannelInboundHandlerAdapter(){

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        println(msg)
        inboundChannel.writeAndFlush(msg)
    }
}