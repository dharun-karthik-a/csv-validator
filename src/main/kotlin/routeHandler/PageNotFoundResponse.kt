package routeHandler

import response.Response

class PageNotFoundResponse(private val response: Response = Response()) {

    fun handleUnknownRequest(): String {
        return response.getHttpHead(400) + "\r\n\r\n"
    }

}