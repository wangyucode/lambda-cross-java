package cn.wycode.lambda.proxy.config

import java.net.URI

data class AliyunConfig(private val url: String) {

    val port: Int = 443
    val path: String
    val host: String

    init {
        val uri = URI(url)
        host = uri.host
        path = uri.rawPath
    }
}