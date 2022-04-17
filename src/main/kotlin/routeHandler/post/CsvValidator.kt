package routeHandler.post

import metaData.MetaDataReaderWriter
import org.json.JSONArray
import request.RequestHandler
import response.ContentType
import response.Response
import validation.Validation
import validation.implementation.ColumnValidation
import java.io.BufferedReader

class CsvValidator(val metaDataReaderWriter: MetaDataReaderWriter) {

    private val response = Response()
    private val requestHandler = RequestHandler()

    fun handleCsv(request: String, inputStream: BufferedReader): String {
        val bodySize = requestHandler.getContentLength(request)
        val body = requestHandler.getBody(bodySize, inputStream)
        val jsonBody = JSONArray(body)

        val errorColumnsJson = getErrorColumns(jsonBody)
        if (!errorColumnsJson.isEmpty) {
            return response.generateResponse(errorColumnsJson.toString(), 200, ContentType.JSON.value)
        }

        val validation = Validation(metaDataReaderWriter)
        val responseBody = validation.validate(jsonBody)

        return response.generateResponse(responseBody.toString(), 200, ContentType.JSON.value)
    }

    private fun getErrorColumns(jsonBody: JSONArray): JSONArray {
        val columnValidation = ColumnValidation()
        val metaDataFields = metaDataReaderWriter.readRawContent()
        return columnValidation.getColumnsNotInConfig(metaDataFields, jsonBody)
    }

}