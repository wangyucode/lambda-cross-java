package cn.wycode.lambda.proxy.outbound

import cn.wycode.lambda.proxy.Response
import cn.wycode.lambda.proxy.inbound.ProxyInboundHandler
import com.alibaba.fastjson.JSON
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.DeflaterInputStream
import java.util.zip.GZIPInputStream

class ProxyOutboundHandler(private val inboundChannel: Channel) : SimpleChannelInboundHandler<FullHttpResponse>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("ProxyOutBoundHandle==")
        ctx.read()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) {
        println("ProxyOutBoundHandle<<<$msg")
        val body = msg.content().toString(CharsetUtil.UTF_8)
        println("ProxyOutBoundHandle body<<<$body")
        val proxyResponse = try {
            JSON.parseObject(body, Response::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            msg.retain()
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
            return
        }
        val headers = CombinedHttpHeaders(true)
        var outBytes:ByteArray? = null
        if (proxyResponse.error == null) {
            for (header in proxyResponse.headers!!) {
                headers[header.key] = header.value
            }
            outBytes = Base64.getDecoder().decode(proxyResponse.content)
        }else{
            outBytes = proxyResponse.error.toByteArray()
        }
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(proxyResponse.code),
                Unpooled.wrappedBuffer(outBytes),
                headers, DefaultHttpHeaders())

        inboundChannel.writeAndFlush(response).addListener(object : ChannelFutureListener {
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
