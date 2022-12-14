package validation.jsonConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenericValidatorTest {

    private val supportedFieldTypeList =
        listOf("number", "alphanumeric", "alphabets", "date", "time", "datetime", "email", "text")

    @Test
    fun shouldReturnErrorMessageForFieldTypeNotSupported() {
        val genericValidator = GenericValidator()
        val expected = listOf("numbers is not supported")

        val actual = genericValidator.validate("numbers", supportedFieldTypeList)

        assertEquals(expected, actual)
    }
}

