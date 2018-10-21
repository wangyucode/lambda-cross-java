package cn.wycode.lambda.proxy

data class Request(val method: String,
                   val uri: String,
                   val protocolVersion: String,
                   val headers: Map<String, String>,
                   val body:String ="你abc好")


data class Response(
        val code: Int = 0,
        val error: String? = null,
        val headers: Map<String, String> = HashMap(),
        val content: String = "")