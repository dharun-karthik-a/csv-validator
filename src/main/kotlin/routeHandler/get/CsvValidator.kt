package routeHandler.get

import metaData.ConfigFileReaderWriter
import metaData.CsvContentReader
import org.json.JSONArray
import response.ContentType
import response.Response
import validation.Validator
import validation.implementation.ColumnValidation

class CsvValidator(
    val configFileReaderWriter: ConfigFileReaderWriter,
    private val csvContentReader: CsvContentReader
) {

    private val response = Response()

    fun handleCsv(): String {
        //todo update validation
//        val csvContent = csvContentReader.readJsonData()
        val csvContent = JSONArray()

        val errorColumnsJson = getColumnErrors()
        if (!errorColumnsJson.isEmpty) {
            return response.generateResponse(errorColumnsJson.toString(), 200, ContentType.JSON.value)
        }

        val validator = Validator(configFileReaderWriter)
        val responseBody = validator.validate(csvContent)

        return response.generateResponse(responseBody.toString(), 200, ContentType.JSON.value)
    }

    private fun getColumnErrors(): JSONArray {
        val columnValidation = ColumnValidation()
        val metaDataFields = configFileReaderWriter.readRawContent()
        return columnValidation.validate(metaDataFields, csvContentReader.headers)
    }
}