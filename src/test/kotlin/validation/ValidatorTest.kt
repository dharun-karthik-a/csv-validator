package validation

import metaData.ConfigFileReaderWriter
import metaData.csv.CsvContentReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidatorTest {

    @Test
    fun shouldGetEmptyJsonArrayWhenTheJsonContentIsValid() {
        val configFileReaderWriter =
            ConfigFileReaderWriter("src/test/kotlin/metaDataTestFiles/configContent/csv-config-content-test.json")
        val validator = Validator(configFileReaderWriter)
        val csvContentReader =
            CsvContentReader("src/test/kotlin/metaDataTestFiles/csvContent/valid-csv-content-test.csv")
        val expectedContent =
            "{}"

        val actual = validator.validate(csvContentReader)

        assertEquals(expectedContent, actual.toString())
    }

    @Test
    fun shouldGetEmptyJsonArrayWhenThereIsNoContent() {
        val configFileReaderWriter =
            ConfigFileReaderWriter("src/test/kotlin/metaDataTestFiles/configContent/csv-config-content-test.json")
        val validator = Validator(configFileReaderWriter)
        val csvContentReader =
            CsvContentReader("src/test/kotlin/metaDataTestFiles/csvContent/empty-csv-content-test.csv")
        val expectedContent = "{}"

        val actual = validator.validate(csvContentReader)

        assertEquals(expectedContent, actual.toString())
    }

    @ParameterizedTest
    @MethodSource("inValidValidationArguments")
    fun shouldGetValidationError(configPath: String, csvPath: String, expectedContent: String) {
        val configFileReaderWriter = ConfigFileReaderWriter("src/test/kotlin/metaDataTestFiles/validation/$configPath")
        val validator = Validator(configFileReaderWriter)
        val csvContentReader = CsvContentReader("src/test/kotlin/metaDataTestFiles/csvContent/errorCsv/$csvPath")

        val actual = validator.validate(csvContentReader)

        assertEquals(expectedContent, actual.toString())
    }

    private fun inValidValidationArguments(): List<Arguments> {
        return listOf(
            Arguments.of(
                "date-meta-data-test.json",
                "date-error-test.csv",
                """{"date":{"total-error-count":2,"details":{"Type mismatch error":{"error-count":2,"lines":{"2":"Incorrect format of 'date' in date : 15/22/2002, expected format : dd/MM/yyyy","3":"Incorrect format of 'date' in date : 15/02/23, expected format : dd/MM/yyyy"}}}}}"""
            ),
            Arguments.of(
                "length-meta-data-test.json",
                "length-error-test.csv",
                """{"product description":{"total-error-count":1,"details":{"Length error":{"error-count":1,"lines":{"1":"Value length should be greater than or equal to 7 in product description : Table"}}}}}"""
            ),
            Arguments.of(
                "restricted-input-meta-data-test.json",
                "restricted-input-error-test.csv",
                """{"export":{"total-error-count":1,"details":{"Value not found":{"error-count":1,"lines":{"1":"Value not found export : fa"}}}}}"""
            ),
            Arguments.of(
                "dependency-meta-data-test.json",
                "dependency-error-test.csv",
                """{"country name":{"total-error-count":1,"details":{"Dependency error":{"error-count":1,"lines":{"1":"export is N but country name is usa"}}}}}"""
            ),
            Arguments.of(
                "email-meta-data-test.json",
                "email-error-test.csv",
                """{"email":{"total-error-count":1,"details":{"Type mismatch error":{"error-count":1,"lines":{"1":"Incorrect format of 'email' in email : talon.atlas+managedsahaj.ai"}}}}}"""
            ),
            Arguments.of(
                "text-meta-data-test.json",
                "text-error-test.csv",
                """{"text":{"total-error-count":1,"details":{"Type mismatch error":{"error-count":1,"lines":{"1":"Incorrect format of 'text' in text : £ new one"}}}}}"""
            ),
            Arguments.of(
                "null-meta-data-test.json",
                "null-error-test.csv",
                """{"name":{"total-error-count":1,"details":{"Empty value found":{"error-count":1,"lines":{"2":"Empty Value not allowed in name"}}}}}"""
            )
        )
    }
}
