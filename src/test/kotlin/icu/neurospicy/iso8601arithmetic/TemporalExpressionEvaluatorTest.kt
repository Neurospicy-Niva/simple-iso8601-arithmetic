package icu.neurospicy.iso8601arithmetic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.Temporal
import kotlin.test.assertEquals

class TemporalExpressionEvaluatorTest {
    
    private val evaluator = TemporalExpressionEvaluator()
    
    @Test
    fun `should evaluate datetime plus duration`() {
        val result = evaluator.evaluate("2024-01-15T09:00 + PT2H")
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should evaluate datetime minus duration`() {
        val result = evaluator.evaluate("2024-01-15T09:00 - PT30M")
        assertEquals(LocalDateTime.of(2024, 1, 15, 8, 30), result)
    }
    
    @Test
    fun `should evaluate duration plus datetime`() {
        val result = evaluator.evaluate("PT2H + 2024-01-15T09:00")
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should evaluate time plus duration using TODAY`() {
        val today = LocalDate.now()
        val result = evaluator.evaluate("09:00 + PT30M")
        assertEquals(LocalTime.of(9, 30).atDate(today), result)
    }
    
    @Test
    fun `should evaluate date plus duration`() {
        val result = evaluator.evaluate("2024-01-15 + P1D")
        assertEquals(LocalDateTime.of(2024, 1, 16, 0, 0), result)
    }
    
    @Test
    fun `should evaluate variable substitution`() {
        val context = mapOf("startTime" to LocalDateTime.of(2024, 1, 15, 9, 0))
        val result = evaluator.evaluate("\${startTime} + PT2H", context)
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should evaluate duration variable`() {
        val context = mapOf(
            "startTime" to LocalDateTime.of(2024, 1, 15, 9, 0),
            "duration" to Duration.ofHours(2)
        )
        val result = evaluator.evaluate("\${startTime} + \${duration}", context)
        assertEquals(LocalDateTime.of(2024, 1, 15, 11, 0), result)
    }
    
    @Test
    fun `should handle TODAY special variable`() {
        val today = LocalDate.now()
        val result = evaluator.evaluate("TODAY + P1D")
        assertEquals(today.plusDays(1).atStartOfDay(), result)
    }
    
    @Test
    fun `should handle NOW special variable`() {
        val result = evaluator.evaluate("NOW + PT1H")
        // We can't test exact equality due to timing, but we can check it's reasonable
        val expected = LocalDateTime.now().plusHours(1)
        val diff = Duration.between(expected, result).abs()
        assert(diff.toSeconds() < 1) { "Result should be within 1 second of expected" }
    }
    
    @Test
    fun `should evaluate with TemporalContext`() {
        val context = TemporalContext.create()
            .addDateTime("startTime", LocalDateTime.of(2024, 1, 15, 9, 0))
            .addDuration("duration", Duration.ofMinutes(30))
        
        val result = evaluator.evaluate("\${startTime} + \${duration}", context)
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 30), result)
    }
    
    @Test
    fun `should handle single datetime operand`() {
        val result = evaluator.evaluate("2024-01-15T09:00")
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 0), result)
    }
    
    @Test
    fun `should handle single date operand`() {
        val result = evaluator.evaluate("2024-01-15")
        assertEquals(LocalDateTime.of(2024, 1, 15, 0, 0), result)
    }
    
    @Test
    fun `should handle single time operand`() {
        val today = LocalDate.now()
        val result = evaluator.evaluate("09:30")
        assertEquals(LocalTime.of(9, 30).atDate(today), result)
    }
    
    @Test
    fun `should throw error for missing variable`() {
        val exception = assertThrows<IllegalArgumentException> {
            evaluator.evaluate("\${missingVar} + PT1H")
        }
        assertEquals("Variable not found in context: missingVar", exception.message)
    }
    
    @Test
    fun `should throw error for timezone information`() {
        val exception = assertThrows<IllegalArgumentException> {
            evaluator.evaluate("2024-01-15T09:00Z + PT1H")
        }
        assertEquals("Timezone information is not supported: 2024-01-15T09:00Z", exception.message)
    }
    
    @Test
    fun `should throw error for invalid datetime format`() {
        val exception = assertThrows<IllegalArgumentException> {
            evaluator.evaluate("invalid-datetime + PT1H")
        }
        assert(exception.message?.contains("Unable to parse temporal value") == true)
    }
    
    @Test
    fun `should throw error for adding two datetimes`() {
        val exception = assertThrows<IllegalArgumentException> {
            evaluator.evaluate("2024-01-15T09:00 + 2024-01-15T10:00")
        }
        assert(exception.message?.contains("Cannot add") == true)
    }
    
    @Test
    fun `should throw error for subtracting datetime from duration`() {
        val exception = assertThrows<IllegalArgumentException> {
            evaluator.evaluate("PT1H - 2024-01-15T09:00")
        }
        assert(exception.message?.contains("Cannot subtract") == true)
    }
} 