package routeHandler

import metaData.ConfigFileReaderWriter
import routeHandler.get.FileGetter
import routeHandler.get.GetRouteHandler
import routeHandler.post.PostRouteHandler
import java.io.InputStream

class RouteHandler(
    configFileReaderWriter: ConfigFileReaderWriter
) {
    private val getRouteHandler = GetRouteHandler()
    private val postRouteHandler = PostRouteHandler()
    private val deleteRouteHandler = DeleteRouteHandler(configFileReaderWriter)
    private val fileGetter = FileGetter()

    fun handleRequest(request: String, inputStream: InputStream): String {
        return when (getRequestType(request)) {
            "GET" -> getRouteHandler.handleGetRequest(request)
            "POST" -> postRouteHandler.handlePostRequest(request, inputStream)
            "DELETE" -> deleteRouteHandler.handleDeleteRequest(request)
            else -> fileGetter.getFileNotFound()
        }
    }

    private fun getRequestType(request: String): String {
        return request.substringBefore(" ")
    }
}