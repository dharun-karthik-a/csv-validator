package validation

import metaData.ConfigFileReaderWriter
import metaData.template.JsonConfigTemplate
import org.json.JSONArray
import org.json.JSONObject
import validation.implementation.DuplicationValidation
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

    fun validate(dataInJSONArray: JSONArray): JSONArray {
        val metaDataList = configFileReaderWriter.readFields()
        return iterateJsonContent(dataInJSONArray, metaDataList)
    }

    private fun iterateJsonContent(
        dataInJSONArray: JSONArray, metaDataList: Array<JsonConfigTemplate>
    ): JSONArray {
        val arrayOfAllErrorsByLine = JSONArray()
        val duplicationValidation = DuplicationValidation()
        dataInJSONArray.forEachIndexed { index, element ->
            val lineErrors = mutableListOf<String>()
            val currentRow = (element as JSONObject)
            val keys = currentRow.keySet()
            appendDuplicationError(duplicationValidation, currentRow, index, lineErrors)
            appendValidationErrors(keys, metaDataList, currentRow, lineErrors)
            val lineNumberInCsv = index + 2
            if (lineErrors.isNotEmpty()) {
                val singleLineErrors = parseErrorsIntoSingleJson(lineNumberInCsv, lineErrors)
                arrayOfAllErrorsByLine.put(singleLineErrors)
            }
        }
        return arrayOfAllErrorsByLine
    }

    private fun appendDuplicationError(
        duplicationValidation: DuplicationValidation,
        currentRow: JSONObject,
        index: Int,
        lineErrors: MutableList<String>
    ) {
        val previousDuplicateIndex = duplicationValidation.isDuplicateIndexAvailable(currentRow, index)
        if (previousDuplicateIndex != null) {
            val name = "Row Duplication"
            lineErrors.add("$name ${previousDuplicateIndex + 1}")
        }
    }

    private fun appendValidationErrors(
        keys: MutableSet<String>,
        metaDataList: Array<JsonConfigTemplate>,
        currentRow: JSONObject,
        lineErrors: MutableList<String>
    ) {
        for (key in keys) {
            appendValidationErrorForCurrentField(metaDataList, key, currentRow, lineErrors)
        }
    }

    private fun appendValidationErrorForCurrentField(
        metaDataList: Array<JsonConfigTemplate>,
        key: String,
        currentRow: JSONObject,
        lineErrors: MutableList<String>
    ) {
        val metaDataField = metaDataList.first { it.fieldName.contentEquals(key, ignoreCase = true) }
        val currentFieldValue = currentRow.get(key) as String

        validationMap.forEach { entry ->
            val validationType = entry.value
            val result = validationType.validate(metaDataField, currentFieldValue, key, currentRow)
            if (result != null) {
                lineErrors.add(result)
            }
        }
    }

    private fun parseErrorsIntoSingleJson(index: Int, lineErrors: MutableList<String>): JSONObject {
        return JSONObject().put(index.toString(), lineErrors)
    }
}