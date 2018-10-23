package cn.wycode.lambda

import java.io.ByteArrayInputStream
import java.net.Socket
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    val requst = "GET http://wycode.cn/ HTTP/1.1\r\n" +
            "cache-control: no-cache\r\n" +
            "Accept: */*\r\n" +
            "Host: wycode.cn\r\n" +
            "Connection: keep-alive\r\n\r\n"
    val input = ByteArrayInputStream(requst.toByteArray())
    val output = System.out

    val sb = StringBuilder()
    val buffer = ByteArray(1024)
    var n: Int = input.read(buffer)
    while (n != -1) {
        sb.append(String(buffer, 0, n, StandardCharsets.UTF_8))
        n = input.read(buffer)
    }
    println("handleRequest>>>" + sb.toString())

    val s = Socket("wycode.cn", 80)
    val httpOutputStream = s.getOutputStream()

    httpOutputStream.write(sb.toString().toByteArray(StandardCharsets.UTF_8))

    val httpInputStream = s.getInputStream()
    sb.setLength(0)
    n = httpInputStream.read(buffer)
    while (n != -1) {

        output.write(buffer, 0, n)
        sb.append(String(buffer, 0, n, StandardCharsets.UTF_8))
        n = httpInputStream.read(buffer)
    }
    httpInputStream.close()
    httpOutputStream.close()
    s.close()
    input.close()
    output.close()
    println("handleRequest<<<" + sb.toString())

}