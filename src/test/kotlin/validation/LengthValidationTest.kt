package validation

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class LengthValidationTest {

    @Test
    fun ShouldreturnFalseIfDataLengthIsgreaterThan_maxLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.maxLength("abcdef", 5)

        assertFalse(actual)
    }

    @Test
    fun ShouldreturnTrueIfDataLengthIsLessThan_maxLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.maxLength("abcdef", 7)

        assertTrue(actual)
    }

    @Test
    fun ShouldreturnTrueIfDataLengthIsEqualTo_maxLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.maxLength("abcdef", 6)

        assertTrue(actual)
    }

    @Test
    fun ShouldreturnFalseIfDataLengthIsLessThan_minLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.minLength("abcdef", 7)

        assertFalse(actual)
    }

    @Test
    fun ShouldreturnFalseIfDataLengthIsGreatorThan_minLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.minLength("abcdef", 5)

        assertTrue(actual)
    }

    @Test
    fun ShouldreturnTrueIfDataLengthIsEqualTo_minLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.minLength("abcdef", 6)

        assertTrue(actual)
    }

    @Test
    fun ShouldReturnFalseIfDataLengthIsLessThan_fixedLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.fixedLength("abcde", 6)

        assertFalse(actual)
    }

    @Test
    fun ShouldReturnFalseIfDataLengthIsGreatorThan_fixedLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.fixedLength("abcdeas", 6)

        assertFalse(actual)
    }

    @Test
    fun ShouldReturnTrueIfDataLengthIsEqual_fixedLength() {
        val lengthValidation = LengthValidation()

        val actual = lengthValidation.fixedLength("abcdef", 6)

        assertTrue(actual)
    }
}