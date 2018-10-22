package cn.wycode.lambda

import okhttp3.OkHttpClient
import okhttp3.Request

fun main(args: Array<String>) {
    val client = OkHttpClient()
    val request = Request.Builder()
            .url("wycode.cn:443")
            .build()

    val response = client.newCall(request).execute()
    println(response.body().toString())

}