package metaData

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MetaDataReaderWriterTest {
    @Test
    fun shouldBeAbleToReadRawContent() {
        val metaDataReaderWriter = MetaDataReaderWriter("src/test/kotlin/metaDataTestFiles/csv-meta-data-test.json")
        val expected = """[
  {
    "fieldName": "product id",
    "type": "alphanumeric",
    "length": 5
  },
  {
    "fieldName": "product description",
    "type": "alphanumeric",
    "minLength": 7,
    "maxLength": 20
  },
  {
    "fieldName": "price",
    "type": "number"
  },
  {
    "fieldName": "export",
    "type": "alphabets",
    "values": [
      "Y",
      "N"
    ]
  },
  {
    "fieldName": "country name",
    "type": "alphabets",
    "minLength": 3,
    "dependencies": [
      {
        "dependentOn": "export",
        "expectedDependentFieldValue": "N",
        "expectedCurrentFieldValue": "null"
      },
      {
        "dependentOn": "export",
        "expectedDependentFieldValue": "Y",
        "expectedCurrentFieldValue": "!null"
      }
    ]
  },
  {
    "fieldName": "source city",
    "type": "alphabets",
    "minLength": 3
  },
  {
    "fieldName": "country code",
    "type": "number",
    "maxLength": 3,
    "dependencies": [
      {
        "dependentOn": "country name",
        "expectedDependentFieldValue": "null",
        "expectedCurrentFieldValue": "null"
      },
      {
        "dependentOn": "country name",
        "expectedDependentFieldValue": "!null",
        "expectedCurrentFieldValue": "!null"
      }
    ]
  },
  {
    "fieldName": "source pincode",
    "type": "number",
    "length": 6,
    "values": [
      "500020",
      "110001",
      "560001",
      "500001",
      "111045",
      "230532",
      "530068",
      "226020",
      "533001",
      "600001",
      "700001",
      "212011",
      "641001",
      "682001",
      "444601"
    ]
  }
]"""

        val actual = metaDataReaderWriter.readRawContent()

        assertEquals(expected, actual)
    }

    @Test
    fun shouldBeAbleToGiveMetaDataInJson() {
        val metaDataReaderWriter = MetaDataReaderWriter("src/test/kotlin/metaDataTestFiles/csv-meta-data-test.json")
        val fields = metaDataReaderWriter.readFields()
        val expected = "500020"

        val actual = fields[7].values?.get(0)

        assertEquals(expected, actual)
    }

    @Test
    fun shouldBeAbleToGiveMetaDataInJsonWhenThereIsNoContent() {
        val metaDataReaderWriter = MetaDataReaderWriter("src/test/kotlin/metaDataTestFiles/empty-json-test.json")

        val expected = metaDataReaderWriter.readFields()

        assertNotNull(expected)
    }

    @Test
    fun shouldBeAbleToWriteJsonToFile() {
        val metaDataReaderWriter = MetaDataReaderWriter("src/test/kotlin/metaDataTestFiles/meta-data-write-test.json")
        val jsonData: Array<JsonMetaDataTemplate> =
            arrayOf(
                JsonMetaDataTemplate(
                    fieldName = "test field",
                    type = "Alphabet",
                    length = "2",
                    minLength = "1",
                    maxLength = "3",
                    values = listOf("22")
                )
            )
        metaDataReaderWriter.writeJsonContent(jsonData)
        val expected =
            """[{"fieldName":"test field","type":"Alphabet","length":"2","minLength":"1","maxLength":"3","values":["22"]}]"""

        val actual = metaDataReaderWriter.readRawContent()

        assertEquals(expected, actual)
    }

    @Test
    fun shouldBeAbleToAppendFieldToFile() {
        val metaDataReaderWriter = MetaDataReaderWriter("src/test/kotlin/metaDataTestFiles/meta-data-append-test.json")
        val oldField =
            """{"fieldName":"test field","type":"Alphabet","length":2,"minLength":1,"maxLength":3,"values":["22"]}"""
        metaDataReaderWriter.appendField(oldField)
        val field = """{"fieldName": "ProductDescription","type": "AlphaNumeric","minLength": 7,"maxLength": 20}"""
        metaDataReaderWriter.appendField(field)
        val expected =
            """[{"fieldName":"test field","type":"Alphabet","length":"2","minLength":"1","maxLength":"3","values":["22"]},{"fieldName":"ProductDescription","type":"AlphaNumeric","minLength":"7","maxLength":"20"}]"""

        val actual = metaDataReaderWriter.readRawContent()

        metaDataReaderWriter.clearFields()
        assertEquals(expected, actual)
    }
}