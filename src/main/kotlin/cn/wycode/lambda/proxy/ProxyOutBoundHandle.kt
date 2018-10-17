package cn.wycode.lambda.proxy

import io.netty.channel.*

class ProxyOutBoundHandle(val inboundChannel: Channel) : ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("ProxyOutBoundHandle==")
        ctx.read()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        println("ProxyOutBoundHandle<<<" + msg.toString())
        inboundChannel.writeAndFlush(msg).addListener(object : ChannelFutureListener {
            override fun operationComplete(future: ChannelFuture) {
                if (future.isSuccess) {
                    // was able to flush out data, start to read the next chunk
                    ctx.channel().read()
                } else {
                    future.channel().close()
                }
            }
        })
    }


    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("ProxyOutBoundHandle!=")
        ProxyInboundHandler.closeOnFlush(inboundChannel)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ProxyInboundHandler.closeOnFlush(ctx.channel())
    }
}
