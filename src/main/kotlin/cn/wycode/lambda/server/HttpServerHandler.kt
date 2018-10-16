package cn.wycode.lambda.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.CharsetUtil

class HttpServerHandler : SimpleChannelInboundHandler<Any>() {

    val sb = StringBuilder()

    lateinit var request: HttpRequest

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is HttpRequest) {
            if (HttpUtil.is100ContinueExpected(msg)) {
                ctx.write(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
            }
            request = msg


            sb.setLength(0)

            sb.append("Welcome\r\n")
            sb.append("=====================\r\n")
            sb.append("VERSION: ").append(msg.protocolVersion()).append("\r\n")
            sb.append("HOSTNAME: ").append(msg.headers().get(HttpHeaderNames.HOST, "unknown")).append("\r\n")
            sb.append("REQUEST_URI: ").append(msg.uri()).append("\r\n\r\n")

            val headers = msg.headers()
            if (!headers.isEmpty) {
                for (h in headers) {
                    sb.append("HEADER: ").append(h.key).append(" = ").append(h.value).append("\r\n")
                }
                sb.append("\r\n")
            }

            val queryStringDecoder = QueryStringDecoder(msg.uri())
            val params = queryStringDecoder.parameters()
            if (!params.isEmpty()) {
                for (p in params) {
                    val vals = p.value
                    for (value in vals) {
                        sb.append("PARAM: ").append(p.key).append(" = ").append(value).append("\r\n")
                    }
                }
                sb.append("\r\n")
            }

            appendDecoderResult(sb, msg)
        }

        if (msg is HttpContent) {
            val content = msg.content()

            if (content.isReadable) {
                sb.append("CONTENT: ")
                sb.append(content.toString(CharsetUtil.UTF_8))
                sb.append("\r\n")
                appendDecoderResult(sb, msg)
            }

            if (msg is LastHttpContent) {
                sb.append("END OF CONTENT\r\n")

                if (!msg.trailingHeaders().isEmpty) {
                    sb.append("\r\n")
                    for (name in msg.trailingHeaders().names()) {
                        for (value in msg.trailingHeaders().getAll(name)) {
                            sb.append("TRAILING HEADER: ")
                            sb.append(name).append(" = ").append(value).append("\r\n")
                        }
                    }
                    sb.append("\r\n")
                }

                // Decide whether to close the connection or not.
                if (writeResponse(msg, ctx)) {
                    // If keep-alive is off, close the connection once the content is fully written.
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
                }
            }
        }

    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    private fun writeResponse(currentObj: HttpObject, ctx: ChannelHandlerContext): Boolean {
        val keepAlive = HttpUtil.isKeepAlive(request)
        // Build the response object.
        val status = if (currentObj.decoderResult().isSuccess) {
            HttpResponseStatus.OK
        } else {
            HttpResponseStatus.BAD_REQUEST
        }
        val response = DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8))

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }
        // Encode the cookie.
        val cookieString = request.headers().get(HttpHeaderNames.COOKIE)
        if (cookieString != null) {
            val cookies = ServerCookieDecoder.STRICT.decode(cookieString)
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (cookie in cookies) {
                    response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie))

                }

            }

        } else {
            // Browser sent no cookie.  Add some.
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));

        }

        // Write the response.
        ctx.write(response)

        return keepAlive
    }


    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    companion object {
        fun appendDecoderResult(sb: StringBuilder, httpObj: HttpObject) {
            val result = httpObj.decoderResult()
            if (result.isSuccess) {
                return
            }

            sb.append(".. WITH DECODER FAILURE: ")
            sb.append(result.cause())
            sb.append("\r\n")
        }
    }
}
