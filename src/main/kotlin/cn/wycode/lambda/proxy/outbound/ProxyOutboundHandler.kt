package cn.wycode.lambda.proxy.outbound

import cn.wycode.lambda.proxy.inbound.ProxyInboundHandler
import io.netty.channel.*
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.DefaultLastHttpContent
import io.netty.handler.codec.http.HttpObject
import io.netty.util.CharsetUtil

class ProxyOutboundHandler(private val inboundChannel: Channel) : SimpleChannelInboundHandler<HttpObject>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("ProxyOutBoundHandle==")
        ctx.read()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        if (msg is DefaultHttpContent){
            println("ProxyOutBoundHandle<<<" + msg.content().toString(CharsetUtil.UTF_8))
            msg.retain()
        }else{
            println("ProxyOutBoundHandle<<<" + msg.toString())
        }
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
