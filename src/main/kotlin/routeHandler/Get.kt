package routeHandler

import java.io.File

class Get {

    private val statusMap = mapOf(
        200 to "Found",
        400 to "Bad Request",
        401 to "Unauthorized"
    )

    fun handleGetRequest(request: String): String {
        val path = getPath(request)
        return when (path) {
            "/" -> serveFile("/index.html")
            else -> serveFile(path)
        }
    }

    private fun serveFile(path: String): String {
        val responseBody = readFileContent(path)
        val contentLength = responseBody.length
        val endOfHeader = "\r\n\r\n"
        return getHttpHead(200) + """Content-Type: text/html; charset=utf-8
            |Content-Length: $contentLength""".trimMargin() + endOfHeader + responseBody
    }

    private fun readFileContent(fileName: String): String {
        val path = System.getProperty("user.dir")
        var file = File("$path/src/main/public$fileName")
        if (!file.exists()) {
            file = File("$path/src/main/public/404.html")
        }
        return file.readText(Charsets.UTF_8)
    }

    private fun getPath(request: String): String {
        return request.split("\r\n")[0].split(" ")[1].substringBefore("?")
    }

    private fun getHttpHead(statusCode: Int): String {
        val content = statusMap[statusCode]
        return "HTTP/1.1 $statusCode $content\n"
    }

}