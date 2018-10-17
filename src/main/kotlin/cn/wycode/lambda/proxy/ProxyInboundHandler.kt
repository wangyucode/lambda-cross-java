package cn.wycode.lambda.proxy

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*

class ProxyInboundHandler : ChannelInboundHandlerAdapter() {

    // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
    // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    var outboundChannel: Channel? = null


    override fun channelActive(ctx: ChannelHandlerContext) {
        val inboundChannel = ctx.channel()
        // Start the connection attempt.
        val outboundClient = Bootstrap()
        outboundClient.group(inboundChannel.eventLoop())
                .channel(inboundChannel.javaClass)
                .handler(ProxyOutBoundHandle(inboundChannel))
                .option(ChannelOption.AUTO_READ, false)

        val f = outboundClient.connect("baidu.com", 80)
        outboundChannel = f.channel()
        f.addListener { future ->
            if (future.isSuccess) {
                // connection complete start to read first data
                inboundChannel.read()
            } else {
                // Close the connection if the connection attempt has failed.
                inboundChannel.close()
            }
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        println("ProxyInboundHandler<<<" + msg.toString())
        if (outboundChannel!!.isActive) {
            outboundChannel!!.writeAndFlush(msg).addListener(object : ChannelFutureListener {
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
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel!!)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        closeOnFlush(ctx.channel())
    }

    companion object {
        fun closeOnFlush(ch: Channel) {
            if (ch.isActive) {
                ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

}
