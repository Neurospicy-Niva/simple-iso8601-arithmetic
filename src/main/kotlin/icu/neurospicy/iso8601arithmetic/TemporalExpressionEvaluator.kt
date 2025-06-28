package icu.neurospicy.iso8601arithmetic

import java.time.LocalDateTime
import java.time.temporal.Temporal

/**
 * Main API for evaluating temporal arithmetic expressions.
 * 
 * Supports expressions with ISO 8601 formatted dates, times, durations, and variables.
 * Only addition (+) and subtraction (-) operations are supported.
 * 
 * Examples:
 * - "2024-01-15T09:00 + PT2H"
 * - "${startTime} + PT30M"
 * - "TODAY + P1D"
 * - "09:00 + PT1H30M"
 */
class TemporalExpressionEvaluator {
    
    /**
     * Evaluates a temporal arithmetic expression with the given context.
     * 
     * @param expression The expression to evaluate (e.g., "${startTime} + PT30M")
     * @param context Map of variable names to temporal values (supports Temporal and Duration)
     * @return The result as a LocalDateTime
     * @throws IllegalArgumentException if the expression is invalid or contains unsupported operations
     */
    fun evaluate(expression: String, context: Map<String, Any> = emptyMap()): LocalDateTime {
        return ExpressionParser.parse(expression, context)
    }
    
    /**
     * Evaluates a temporal arithmetic expression with the given typed context.
     * 
     * @param expression The expression to evaluate (e.g., "${startTime} + PT30M")
     * @param context Typed context containing temporal values
     * @return The result as a LocalDateTime
     * @throws IllegalArgumentException if the expression is invalid or contains unsupported operations
     */
    fun evaluate(expression: String, context: TemporalContext): LocalDateTime {
        return ExpressionParser.parse(expression, context.toMap())
    }
} 