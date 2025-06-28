package icu.neurospicy.iso8601arithmetic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.Temporal
import kotlin.test.assertEquals

class ExpressionParserTest {
    
    @Test
    fun `should parse datetime literal`() {
        val result = ExpressionParser.parse("2024-01-15T09:00", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 0), result)
    }
    
    @Test
    fun `should parse date literal`() {
        val result = ExpressionParser.parse("2024-01-15", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 15, 0, 0), result)
    }
    
    @Test
    fun `should parse time literal`() {
        val today = LocalDate.now()
        val result = ExpressionParser.parse("09:30", emptyMap())
        assertEquals(LocalTime.of(9, 30).atDate(today), result)
    }
    
    @Test
    fun `should substitute variables`() {
        val context = mapOf("startTime" to LocalDateTime.of(2024, 1, 15, 9, 0))
        val result = ExpressionParser.parse("\${startTime}", context)
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 0), result)
    }
    
    @Test
    fun `should substitute TODAY special variable`() {
        val today = LocalDate.now()
        val result = ExpressionParser.parse("TODAY", emptyMap())
        assertEquals(today.atStartOfDay(), result)
    }
    
    @Test
    fun `should substitute NOW special variable`() {
        val result = ExpressionParser.parse("NOW", emptyMap())
        val expected = LocalDateTime.now()
        val diff = Duration.between(expected, result).abs()
        assert(diff.toSeconds() < 1) { "Result should be within 1 second of expected" }
    }
    
    @Test
    fun `should perform addition with datetime and duration`() {
        val result = ExpressionParser.parse("2024-01-15T09:00 + PT2H", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should perform addition with duration and datetime`() {
        val result = ExpressionParser.parse("PT2H + 2024-01-15T09:00", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should perform subtraction with datetime and duration`() {
        val result = ExpressionParser.parse("2024-01-15T09:00 - PT30M", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 15, 8, 30), result)
    }
    
    @Test
    fun `should perform addition with date and duration`() {
        val result = ExpressionParser.parse("2024-01-15 + P1D", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 16, 0, 0), result)
    }
    
    @Test
    fun `should perform addition with time and duration`() {
        val today = LocalDate.now()
        val result = ExpressionParser.parse("09:00 + PT1H30M", emptyMap())
        assertEquals(LocalTime.of(10, 30).atDate(today), result)
    }
    
    @Test
    fun `should handle complex duration formats`() {
        val result = ExpressionParser.parse("2024-01-15T09:00 + P1DT2H30M", emptyMap())
        val expected = LocalDateTime.of(2024, 1, 15, 9, 0)
            .plusDays(1)
            .plusHours(2)
            .plusMinutes(30)
        assertEquals(expected, result)
    }
    
    @Test
    fun `should handle variable substitution in arithmetic`() {
        val context = mapOf(
            "startTime" to LocalDateTime.of(2024, 1, 15, 9, 0),
            "duration" to Duration.ofHours(2)
        )
        
        val result = ExpressionParser.parse("\${startTime} + \${duration}", context)
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should handle whitespace in expressions`() {
        val result = ExpressionParser.parse("  2024-01-15T09:00   +   PT2H  ", emptyMap())
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should throw error for timezone in datetime`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("2024-01-15T09:00Z + PT1H", emptyMap())
        }
        assertEquals("Timezone information is not supported: 2024-01-15T09:00Z", exception.message)
    }
    
    @Test
    fun `should throw error for timezone with offset`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("2024-01-15T09:00+01:00 + PT1H", emptyMap())
        }
        assertEquals("Timezone information is not supported: 2024-01-15T09:00+01:00", exception.message)
    }
    
    @Test
    fun `should throw error for missing variable`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("\${missingVar} + PT1H", emptyMap())
        }
        assertEquals("Variable not found in context: missingVar", exception.message)
    }
    
    @Test
    fun `should throw error for invalid datetime format`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("invalid-datetime + PT1H", emptyMap())
        }
        assert(exception.message?.contains("Unable to parse temporal value") == true)
    }
    
    @Test
    fun `should throw error for invalid duration format`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("2024-01-15T09:00 + invalid-duration", emptyMap())
        }
        assert(exception.message?.contains("Unable to parse temporal value") == true)
    }
    
    @Test
    fun `should throw error for adding two datetimes`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("2024-01-15T09:00 + 2024-01-15T10:00", emptyMap())
        }
        assert(exception.message?.contains("Cannot add") == true)
    }
    
    @Test
    fun `should throw error for subtracting datetime from duration`() {
        val exception = assertThrows<IllegalArgumentException> {
            ExpressionParser.parse("PT1H - 2024-01-15T09:00", emptyMap())
        }
        assert(exception.message?.contains("Cannot subtract") == true)
    }
    
    @Test
    fun `should handle seconds in time format`() {
        val today = LocalDate.now()
        val result = ExpressionParser.parse("09:30:45", emptyMap())
        assertEquals(LocalTime.of(9, 30, 45).atDate(today), result)
    }
    
    @Test
    fun `should handle milliseconds in time format`() {
        val today = LocalDate.now()
        val result = ExpressionParser.parse("09:30:45.123", emptyMap())
        assertEquals(LocalTime.of(9, 30, 45, 123_000_000).atDate(today), result)
    }
} 