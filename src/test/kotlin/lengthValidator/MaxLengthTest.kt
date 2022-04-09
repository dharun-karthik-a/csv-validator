package lengthValidator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import validation.LengthValidation

internal class MaxLengthTest {
    private val maxLength = MaxLength()
    private val inputText = "Abc123"
    private val lengthValidation = LengthValidation();
    @Test
    fun shouldReturnTrueIfInputTextLengthIsLessThanMaxLengthSpecified() {
        val length = 7

        val actual = maxLength.validateLengthType(inputText, length, lengthValidation)

        assertTrue(actual);
    }

    @Test
    fun shouldReturnTrueIfInputTextLengthIsLessThanOrEqualToMaxLengthSpecified() {
        val length = 6

        val actual = maxLength.validateLengthType(inputText, length, lengthValidation)

        assertTrue(actual);
    }

    @Test
    fun shouldReturnFalseIfInputTextLengthIsMoreThanMaxLengthSpecified() {
        val length = 5

        val actual = maxLength.validateLengthType(inputText, length, lengthValidation)

        assertFalse(actual);
    }

    @Test
    fun shouldReturnTrueIfMaxLengthIsNull() {
        val length = null

        val actual = maxLength.validateLengthType(inputText, length, lengthValidation)

        assertTrue(actual);
    }


}