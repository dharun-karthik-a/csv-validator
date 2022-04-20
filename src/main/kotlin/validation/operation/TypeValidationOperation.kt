package validation.operation

import metaData.JsonMetaDataTemplate
import org.json.JSONObject
import validation.implementation.valueValidator.*

class TypeValidationOperation : ValidationOperation {
    private val valueTypeMap: Map<String, ValueTypeValidator> = mapOf(
        "alphanumeric" to AlphaNumericValidator(),
        "alphabets" to AlphabetValidator(),
        "number" to NumberValidator(),
        "date" to DateValidator(),
        "time" to TimeValidator(),
        "date-time" to DateTimeValidator(),
        "email" to EmailValidator(),
        "text" to TextValidator()
    )

    override fun validate(
        metaDataField: JsonMetaDataTemplate, currentFieldValue: String, currentRow: JSONObject?
    ): Boolean {
        if (isFieldIsNull(currentFieldValue)) {
            return true
        }

        return checkType(metaDataField, currentFieldValue)
    }

    private fun checkType(
        metaDataField: JsonMetaDataTemplate, currentFieldValue: String
    ): Boolean {
        val isValid = valueTypeMap[metaDataField.type]!!.validate(currentFieldValue, metaDataField.pattern)

        if (!isValid) {
            return false
        }
        return true
    }
}