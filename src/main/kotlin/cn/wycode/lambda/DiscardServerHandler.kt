package cn.wycode.lambda

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import java.nio.file.Files.isReadable
import io.netty.buffer.ByteBuf


class DiscardServerHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val buf = msg as ByteBuf
        try {
            println(buf.toString(io.netty.util.CharsetUtil.UTF_8))
        } finally {
            ReferenceCountUtil.release(msg)
        }
    }


    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        // Close the connection when an exception is raised.
        cause?.printStackTrace()
        ctx?.close()
    }
}