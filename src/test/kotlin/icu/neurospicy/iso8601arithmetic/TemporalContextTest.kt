package icu.neurospicy.iso8601arithmetic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.*
import java.time.temporal.Temporal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemporalContextTest {
    
    @Test
    fun `should create empty context`() {
        val context = TemporalContext.create()
        assertTrue(context.toMap().isEmpty())
    }
    
    @Test
    fun `should add different temporal types`() {
        val context = TemporalContext.create()
            .addDate("date", LocalDate.of(2024, 1, 15))
            .addTime("time", LocalTime.of(9, 30))
            .addDateTime("datetime", LocalDateTime.of(2024, 1, 15, 9, 30))
            .addDuration("duration", Duration.ofHours(2))
        
        val map = context.toMap()
        assertEquals(4, map.size)
        assertEquals(LocalDate.of(2024, 1, 15), map["date"])
        assertEquals(LocalTime.of(9, 30), map["time"])
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 30), map["datetime"])
        assertEquals(Duration.ofHours(2), map["duration"])
    }
    
    @Test
    fun `should create context from map with supported types`() {
        val originalMap: Map<String, Any> = mapOf(
            "date" to LocalDate.of(2024, 1, 15),
            "time" to LocalTime.of(9, 30),
            "datetime" to LocalDateTime.of(2024, 1, 15, 9, 30),
            "duration" to Duration.ofHours(2)
        )
        
        val context = TemporalContext.from(originalMap)
        val resultMap = context.toMap()
        
        assertEquals(4, resultMap.size)
        assertEquals(LocalDate.of(2024, 1, 15), resultMap["date"])
        assertEquals(LocalTime.of(9, 30), resultMap["time"])
        assertEquals(LocalDateTime.of(2024, 1, 15, 9, 30), resultMap["datetime"])
        assertEquals(Duration.ofHours(2), resultMap["duration"])
    }
    
    @Test
    fun `should throw error for unsupported temporal type in from method`() {
        val mapWithUnsupportedType: Map<String, Any> = mapOf(
            "valid" to LocalDate.of(2024, 1, 15),
            "invalid" to java.time.Instant.now()
        )
        
        val exception = assertThrows<IllegalArgumentException> {
            TemporalContext.from(mapWithUnsupportedType)
        }
        assertEquals("Unsupported temporal type: Instant", exception.message)
    }
    
    @Test
    fun `should allow method chaining`() {
        val context = TemporalContext.create()
            .addDate("date1", LocalDate.of(2024, 1, 15))
            .addDate("date2", LocalDate.of(2024, 1, 16))
            .addTime("time1", LocalTime.of(9, 0))
            .addDateTime("datetime1", LocalDateTime.of(2024, 1, 15, 9, 0))
            .addDuration("duration1", Duration.ofMinutes(30))
        
        assertEquals(5, context.toMap().size)
    }
    
    @Test
    fun `should overwrite values with same key`() {
        val context = TemporalContext.create()
            .addDate("key", LocalDate.of(2024, 1, 15))
            .addTime("key", LocalTime.of(9, 30))
        
        val map = context.toMap()
        assertEquals(1, map.size)
        assertEquals(LocalTime.of(9, 30), map["key"])
    }
} 