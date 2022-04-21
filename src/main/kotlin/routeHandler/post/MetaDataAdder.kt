package routeHandler.post

import metaData.ConfigReaderWriter
import org.json.JSONObject
import request.RequestHandler
import response.ContentType
import response.Response
import java.io.BufferedReader

class MetaDataAdder(val configReaderWriter: ConfigReaderWriter) {
    private val response = Response()
    private val requestHandler = RequestHandler()

    fun handleAddCsvMetaData(request: String, inputStream: BufferedReader): String {
        val bodySize = requestHandler.getContentLength(request)
        val body = requestHandler.getBody(bodySize, inputStream)
        return addCsvMetaData(body)
    }

    private fun addCsvMetaData(body: String): String {
        configReaderWriter.appendField(body)
        val responseBody = JSONObject()
        responseBody.put("value", "Success")
        return response.generateResponse(responseBody.toString(), 201, ContentType.JSON.value)
    }
}