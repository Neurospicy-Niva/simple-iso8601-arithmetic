package icu.neurospicy.iso8601arithmetic

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.Temporal

/**
 * Type-safe builder for creating temporal contexts for expression evaluation.
 * 
 * Provides methods to add different types of temporal values with proper type checking.
 */
class TemporalContext private constructor() {
    private val values = mutableMapOf<String, Any>()
    
    /**
     * Adds a LocalDate value to the context.
     */
    fun addDate(name: String, value: LocalDate): TemporalContext {
        values[name] = value
        return this
    }
    
    /**
     * Adds a LocalTime value to the context.
     */
    fun addTime(name: String, value: LocalTime): TemporalContext {
        values[name] = value
        return this
    }
    
    /**
     * Adds a LocalDateTime value to the context.
     */
    fun addDateTime(name: String, value: LocalDateTime): TemporalContext {
        values[name] = value
        return this
    }
    
    /**
     * Adds a Duration value to the context.
     */
    fun addDuration(name: String, value: Duration): TemporalContext {
        values[name] = value
        return this
    }
    
    /**
     * Converts this context to a Map for use with the expression evaluator.
     */
    internal fun toMap(): Map<String, Any> {
        return values.toMap()
    }
    
    companion object {
        /**
         * Creates a new empty temporal context.
         */
        fun create(): TemporalContext = TemporalContext()
        
        /**
         * Creates a temporal context from an existing map of temporal values.
         * 
         * @param values Map of variable names to temporal values
         * @throws IllegalArgumentException if any value is not a supported type
         */
        fun from(values: Map<String, Any>): TemporalContext {
            val context = TemporalContext()
            values.forEach { (name, value) ->
                when (value) {
                    is LocalDate -> context.addDate(name, value)
                    is LocalTime -> context.addTime(name, value)
                    is LocalDateTime -> context.addDateTime(name, value)
                    is Duration -> context.addDuration(name, value)
                    else -> throw IllegalArgumentException("Unsupported temporal type: ${value::class.simpleName}")
                }
            }
            return context
        }
    }
} 