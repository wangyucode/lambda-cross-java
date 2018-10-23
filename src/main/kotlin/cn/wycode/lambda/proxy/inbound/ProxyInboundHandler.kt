package cn.wycode.lambda.proxy.inbound

import cn.wycode.lambda.proxy.Request
import cn.wycode.lambda.proxy.config.AliyunConfig
import cn.wycode.lambda.proxy.outbound.ProxyOutboundInitializer
import com.alibaba.fastjson.JSON
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil

class ProxyInboundHandler(val aliyunConfig: AliyunConfig) : SimpleChannelInboundHandler<ByteBuf>() {


    // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
    // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    private var outboundChannel: Channel? = null

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        println("ProxyInboundHandler<<<" + msg.toString(CharsetUtil.UTF_8))
//        val headers = msg.headers()
//        val headerMap = HashMap<String, String>(headers.size())
//        headers.forEach { headerMap[it.key] = it.value }
//        val outboundBody = Request(msg.method().name(), msg.uri(), msg.protocolVersion().text(), headerMap)
//        val outboundJson = JSON.toJSONString(outboundBody)
        val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, aliyunConfig.path)
        request.headers().set(HttpHeaderNames.HOST, aliyunConfig.host)
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, msg.writerIndex())
//        request.content().writeCharSequence(outboundJson, CharsetUtil.UTF_8)
        request.content().writeBytes(msg)
//
        if (outboundChannel!!.isActive) {
            println("ProxyInboundHandler>>>${request.content().toString(CharsetUtil.UTF_8)}")
            outboundChannel!!.writeAndFlush(request).addListener(object : ChannelFutureListener {
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


    override fun channelActive(ctx: ChannelHandlerContext) {
        val inboundChannel = ctx.channel()
        // Start the connection attempt.
        val outboundClient = Bootstrap()
        outboundClient.group(inboundChannel.eventLoop())
                .channel(inboundChannel.javaClass)
                .handler(ProxyOutboundInitializer(inboundChannel))
                .option(ChannelOption.AUTO_READ, false)

        val f = outboundClient.connect(aliyunConfig.host, aliyunConfig.port)
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
