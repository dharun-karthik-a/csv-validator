package routeHandler

import Action
import AlphaNumeric
import Alphabet
import FixedLength
import LengthType
import MaxLength
import MinLength
import Numbers
import ValueTypeValidator
import metaData.JsonMetaDataTemplate
import metaData.MetaDataReaderWriter
import org.json.JSONArray
import org.json.JSONObject
import response.ResponseHead
import validation.DuplicationValidation
import validation.LengthValidation
import validation.TypeValidation
import java.io.BufferedReader


class PostRouteHandler(
    val metaDataReaderWriter: MetaDataReaderWriter,
    private val responseHead: ResponseHead = ResponseHead()
) {
    private val pageNotFoundResponse = PageNotFoundResponse()

    private val lengthTypeActionMapper: Map<LengthType, Action> = mapOf(
        LengthType.FIXED_LENGTH to FixedLength(),
        LengthType.MIN_LENGTH to MinLength(),
        LengthType.MAX_LENGTH to MaxLength()
    )
    private val valueTypeActionMapper: Map<String, ValueTypeValidator> = mapOf(
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
        println("body $body")
        val jsonBody = JSONArray(body)

        val repeatedRowList = DuplicationValidation().getDuplicateRowNumberInJSON(jsonBody)
        println("Repeated Lines :$repeatedRowList , ${repeatedRowList.isEmpty}")
        val typeValidationResultList = typeValidation(jsonBody)
        val lengthValidationResultList = lengthValidation(jsonBody)
        val responseBody = repeatedRowList.putAll(typeValidationResultList).putAll(lengthValidationResultList).toString()
        println(responseBody)
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
        try {
            dataInJSONArray.forEachIndexed { index, element ->
                val fieldElement = (element as JSONObject)
                val keys = fieldElement.keySet()
                for (key in keys) {
                    if (lengthVal(fieldArray, key, fieldElement, lengthValidation)) {
                        val jsonObject = JSONObject().put(
                            (index + 1).toString(),
                            "Length Error in $key"
                        )
                        rowMap.put(jsonObject)
                        break
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
        var isValid = true
        val value = filedElement.get(key) as String

        isValid = (lengthTypeActionMapper[LengthType.FIXED_LENGTH]!!.performAction(value, field.length, lengthValidation) &&
                lengthTypeActionMapper[LengthType.MIN_LENGTH]!!.performAction(value, field.minLength, lengthValidation) &&
                lengthTypeActionMapper[LengthType.MAX_LENGTH]!!.performAction(value, field.maxLength,lengthValidation))

        if (!isValid) {
            return true
        }
        return false
    }

    fun typeValidation(dataInJSONArray: JSONArray): JSONArray {
        val rowMap = JSONArray()
        val typeValidation = TypeValidation()
        val fieldArray = metaDataReaderWriter.readFields()
        try {
            dataInJSONArray.forEachIndexed { index, element ->
                val fieldElement = (element as JSONObject)
                val keys = fieldElement.keySet()
                println(fieldElement)
                for (key in keys) {
                    if (typeVal(fieldArray, key, fieldElement, typeValidation)) {
                        val jsonObject = JSONObject().put(
                            (index + 1).toString(),
                            "Type Error in $key"
                        )
                        rowMap.put(jsonObject)
                        break
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
        ele: JSONObject,
        typeValidation: TypeValidation
    ): Boolean {
        val field = fieldArray.first { it.fieldName == key }
        var isValid = true
        val value = ele.get(key) as String

        isValid = valueTypeActionMapper[field.type]!!.validateValueType(value, field.type, typeValidation)

        if (!isValid) {
            return true
        }
        return false
    }
}