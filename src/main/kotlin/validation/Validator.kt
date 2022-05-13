package validation

import metaData.ConfigFileReaderWriter
import metaData.csv.CsvContentReader
import metaData.template.JsonConfigTemplate
import org.json.JSONObject
import validation.operation.*
import validation.operation.ValidationType.*

class Validator(private val configFileReaderWriter: ConfigFileReaderWriter) {
    private val validationMap = mutableMapOf(
        NULL_VALIDATION to NullValidationOperation(),
        TYPE_VALIDATION to TypeValidationOperation(),
        LENGTH_VALIDATION to LengthValidationOperation(),
        RESTRICTED_INPUT_VALIDATION to RestrictedInputValidationOperation(),
        DEPENDENCY_VALIDATION to DependencyValidationOperation()
    )

    fun validate(csvContentReader: CsvContentReader): JSONObject {
        val metaDataList = configFileReaderWriter.readFields()
        return iterateJsonContent(csvContentReader, metaDataList)
    }

    private fun iterateJsonContent(
        csvContentReader: CsvContentReader, metaDataList: Array<JsonConfigTemplate>
    ): JSONObject {
        var rowNumber = 0
        var rowJson = csvContentReader.readNextLineInJson()
        val errorJsonObject = JSONObject()
        while (rowJson != null) {
            rowNumber++
            val keys = rowJson.keySet()
            appendValidationErrors2(keys, metaDataList, rowJson, errorJsonObject, rowNumber)
            rowJson = csvContentReader.readNextLineInJson()
        }
        return errorJsonObject
    }


    private fun appendValidationErrors2(
        keys: Set<String>,
        metaDataList: Array<JsonConfigTemplate>,
        currentRow: JSONObject,
        errorObject: JSONObject,
        rowNumber: Int
    ) {
        for (key in keys) {
            val metaDataField = metaDataList.first { it.fieldName.contentEquals(key, ignoreCase = true) }
            val currentFieldValue = currentRow.getString(key)
            validationMap.forEach { entry ->
                val validationType = entry.value
                val result = validationType.validate(metaDataField, currentFieldValue, key, currentRow)
                if (result != null) {
                    appendColumnNameIfNotAvailable(errorObject, key)
                    appendToErrorObject(errorObject, key, rowNumber, result, entry.key.errorName)
                }

            }
        }
    }

    private fun appendColumnNameIfNotAvailable(errorObject: JSONObject, key: String) {
        if (!errorObject.has(key)) {
            errorObject.put(key, createJsonObjectWithDetails())
        }
    }

    private fun appendToErrorObject(
        errorObject: JSONObject,
        key: String,
        rowNumber: Int,
        result: String,
        validationType: String
    ) {
        val columnObject = errorObject.getJSONObject(key)
        val totalErrorCount = columnObject.getInt("total-error-count") + 1
        columnObject.put("total-error-count", totalErrorCount)
        val detailList = columnObject.getJSONObject("details")
        if (!detailList.has(validationType)) {
            detailList.put(validationType, createNewErrorDetailObject())
        }
        appendError(result, rowNumber, detailList, validationType)

    }

    private fun appendError(
        result: String,
        rowNumber: Int,
        detailsObject: JSONObject,
        validationType: String
    ) {
        val specificErrorObject = detailsObject.getJSONObject(validationType)
        val newErrorCount = specificErrorObject.getInt("error-count") + 1
        specificErrorObject.put("error-count", newErrorCount)
        val errorLines = specificErrorObject.getJSONObject("lines")
        errorLines.put(rowNumber.toString(), result)

    }

    private fun createNewErrorDetailObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("error-count", 0)
        jsonObject.put("lines", JSONObject())
        return jsonObject
    }

    private fun createJsonObjectWithDetails(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("total-error-count", 0)
        jsonObject.put("details", JSONObject())
        return jsonObject
    }
}