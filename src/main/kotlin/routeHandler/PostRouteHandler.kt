package routeHandler

import lengthValidator.*
import metaData.JsonMetaDataTemplate
import metaData.MetaDataReaderWriter
import org.json.JSONArray
import org.json.JSONObject
import response.ResponseHead
import validation.DependencyValidation
import validation.DuplicationValidation
import validation.LengthValidation
import validation.TypeValidation
import valueValidator.AlphaNumeric
import valueValidator.Alphabet
import valueValidator.Numbers
import valueValidator.ValueTypeValidator
import java.io.BufferedReader


class PostRouteHandler(
    val metaDataReaderWriter: MetaDataReaderWriter,
    private val responseHead: ResponseHead = ResponseHead()
) {
    private val pageNotFoundResponse = PageNotFoundResponse()

    private val lengthTypeMap: Map<LengthType, LengthTypeValidator> = mapOf(
        LengthType.FIXED_LENGTH to FixedLength(),
        LengthType.MIN_LENGTH to MinLength(),
        LengthType.MAX_LENGTH to MaxLength()
    )
    private val valueTypeMap: Map<String, ValueTypeValidator> = mapOf(
        "AlphaNumeric" to AlphaNumeric(),
        "Alphabet" to Alphabet(),
        "Number" to Numbers()
    )

    fun handlePostRequest(
        request: String,
        inputStream: BufferedReader,
    ): String {
        return when (getPath(request)) {
            "/csv" -> handleCsv(request, inputStream)
            "/add-meta-data" -> handleAddingCsvMetaData(request, inputStream)
            else -> pageNotFoundResponse.handleUnknownRequest()
        }
    }

    private fun getPath(request: String): String {
        return request.split("\r\n")[0].split(" ")[1].substringBefore("?")
    }

    fun handleCsv(request: String, inputStream: BufferedReader): String {
        val bodySize = getContentLength(request)
        val body = getBody(bodySize, inputStream)
        val jsonBody = JSONArray(body)

        val repeatedRowList = DuplicationValidation().getDuplicateRowNumberInJSON(jsonBody)
        val typeValidationResultList = typeValidation(jsonBody)
        val lengthValidationResultList = lengthValidation(jsonBody)
        val dependencyValidation = dependencyValidation(jsonBody)
        val responseBody =
            repeatedRowList.putAll(typeValidationResultList).putAll(lengthValidationResultList)
                .putAll(dependencyValidation).toString()
        val contentLength = responseBody.length
        val endOfHeader = "\r\n\r\n"
        return responseHead.getHttpHead(200) + """Content-Type: text/json; charset=utf-8
            |Content-Length: $contentLength""".trimMargin() + endOfHeader + responseBody
    }


    fun handleAddingCsvMetaData(request: String, inputStream: BufferedReader): String {
        val bodySize = getContentLength(request)
        val body = getBody(bodySize, inputStream)
        return addCsvMetaData(body)
    }

    fun addCsvMetaData(body: String): String {
        metaDataReaderWriter.appendField(body)
        val endOfHeader = "\r\n\r\n"
        val responseBody = "Successfully Added"
        val contentLength = responseBody.length
        return responseHead.getHttpHead(200) + """Content-Type: text/plain; charset=utf-8
            |Content-Length: $contentLength""".trimMargin() + endOfHeader + responseBody
    }

    private fun getBody(bodySize: Int, inputStream: BufferedReader): String {
        val buffer = CharArray(bodySize)
        inputStream.read(buffer)
        return String(buffer)
    }

    private fun getContentLength(request: String): Int {
        request.split("\n").forEach { headerString ->
            val keyValue = headerString.split(":", limit = 2)
            if (keyValue[0].contains("Content-Length")) {
                return keyValue[1].trim().toInt()
            }
        }
        return 0
    }

    //Todo isolate common code between length and type validation
    //todo single responsibility
    //todo eliminate if else
    fun lengthValidation(dataInJSONArray: JSONArray): JSONArray {
        val rowMap = JSONArray()
        val lengthValidation = LengthValidation()
        val fieldArray = metaDataReaderWriter.readFields()
        val errorMessage = "Length Error in "
        try {
            dataInJSONArray.forEachIndexed { index, element ->
                val fieldElement = (element as JSONObject)
                val keys = fieldElement.keySet()
                for (key in keys) {
                    if (lengthVal(fieldArray, key, fieldElement, lengthValidation)) {
                        val jsonObject = JSONObject().put(
                            (index + 1).toString(),
                            "$errorMessage$key"
                        )
                        rowMap.put(jsonObject)
                    }
                }
            }
        } catch (err: Exception) {
            println(err.message)
            return JSONArray()
        }
        return rowMap
    }

    private fun lengthVal(
        fieldArray: Array<JsonMetaDataTemplate>,
        key: String?,
        filedElement: JSONObject,
        lengthValidation: LengthValidation
    ): Boolean {
        val field = fieldArray.first { it.fieldName == key }
        val isValid: Boolean
        val value = filedElement.get(key) as String

        isValid = (lengthTypeMap[LengthType.FIXED_LENGTH]!!.validateLengthType(
            value,
            field.length?.toInt(),
            lengthValidation
        ) &&
                lengthTypeMap[LengthType.MIN_LENGTH]!!.validateLengthType(
                    value,
                    field.minLength?.toInt(),
                    lengthValidation
                ) &&
                lengthTypeMap[LengthType.MAX_LENGTH]!!.validateLengthType(
                    value,
                    field.maxLength?.toInt(),
                    lengthValidation
                ))

        if (!isValid) {
            return true
        }
        return false
    }

    fun typeValidation(dataInJSONArray: JSONArray): JSONArray {
        val rowMap = JSONArray()
        val typeValidation = TypeValidation()
        val fieldArray = metaDataReaderWriter.readFields()
        val errorMessage = "Type Error in "
        try {
            dataInJSONArray.forEachIndexed { index, element ->
                val fieldElement = (element as JSONObject)
                val keys = fieldElement.keySet()
                for (key in keys) {
                    if (typeVal(fieldArray, key, fieldElement, typeValidation)) {
                        val jsonObject = JSONObject().put(
                            (index + 1).toString(),
                            "$errorMessage$key"
                        )
                        rowMap.put(jsonObject)
                    }
                }
            }
        } catch (err: Exception) {
            println(err.message)
            return JSONArray()
        }
        return rowMap
    }

    private fun typeVal(
        fieldArray: Array<JsonMetaDataTemplate>,
        key: String?,
        fieldElement: JSONObject,
        typeValidation: TypeValidation
    ): Boolean {
        val field = fieldArray.first { it.fieldName == key }
        val isValid: Boolean
        val value = fieldElement.get(key) as String
        if(isFieldIsNull(value)){
            return false
        }

        isValid = valueTypeMap[field.type]!!.validateValueType(value, typeValidation)

        if (!isValid) {
            return true
        }
        return false
    }

    fun dependencyValidation(dataInJSONArray: JSONArray): JSONArray {
        val rowMap = JSONArray()
        val dependencyValidation = DependencyValidation()
        val fieldArray = metaDataReaderWriter.readFields()
        val errorMessage = "Dependency Error in "
        try {
            dataInJSONArray.forEachIndexed { index, element ->
                val fieldElement = (element as JSONObject)
                val keys = fieldElement.keySet()
                for (key in keys) {
                    if (depVal(fieldArray, key, fieldElement, dependencyValidation)) {
                        val jsonObject = JSONObject().put(
                            (index + 1).toString(),
                            "$errorMessage$key"
                        )
                        rowMap.put(jsonObject)
                    }
                }
            }
        } catch (err: Exception) {
            println(err.message)
            return JSONArray()
        }
        return rowMap
    }

    private fun depVal(
        fieldArray: Array<JsonMetaDataTemplate>,
        key: String?,
        fieldElement: JSONObject,
        dependencyValidation: DependencyValidation
    ): Boolean {
        val field = fieldArray.first { it.fieldName == key }
        val value = fieldElement.get(key) as String
        if (field.dependencies != null) {
            val dependencies = field.dependencies
            for (dependency in dependencies) {
                val dependentValue = fieldElement.get(dependency.dependentOn) as String
                val expectedCurrentValue = dependency.expectedCurrentFieldValue
                val expectedDependentValue = dependency.expectedDependentFieldValue
                val isValid: Boolean = dependencyValidation.validate(
                    value,
                    dependentValue,
                    expectedDependentValue,
                    expectedCurrentValue
                )
                if (!isValid) {
                    return true
                }
            }
        }
        return false
    }

    private fun isFieldIsNull(value: String?): Boolean {
        return value.contentEquals("null", ignoreCase = true)
    }
}