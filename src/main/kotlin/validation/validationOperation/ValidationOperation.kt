package validation.validationOperation

import metaData.JsonMetaDataTemplate
import org.json.JSONObject

interface ValidationOperation {
    fun validate(
        metaDataField: JsonMetaDataTemplate,
        currentFieldValue: String,
        currentRow: JSONObject? = null,
    ): Boolean

    fun isFieldIsNull(value: String?): Boolean {
        return value.contentEquals("null", ignoreCase = true)
    }
}