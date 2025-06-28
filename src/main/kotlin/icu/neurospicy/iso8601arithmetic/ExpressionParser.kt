package icu.neurospicy.iso8601arithmetic

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount

/**
 * Internal parser for temporal arithmetic expressions.
 */
internal object ExpressionParser {
    
    private val VARIABLE_PATTERN = Regex("""\$\{([^}]+)\}""")
    
    fun parse(expression: String, context: Map<String, Any>): LocalDateTime {
        val normalizedExpression = expression.trim()
        
        // Substitute variables first
        val substituted = substituteVariables(normalizedExpression, context)
        
        // Find the operator (+ or -)
        val plusIndex = substituted.lastIndexOf(" + ")
        val minusIndex = substituted.lastIndexOf(" - ")
        
        return when {
            plusIndex > 0 && (minusIndex < 0 || plusIndex > minusIndex) -> {
                val left = substituted.substring(0, plusIndex).trim()
                val right = substituted.substring(plusIndex + 3).trim()
                performAddition(left, right)
            }
            minusIndex > 0 -> {
                val left = substituted.substring(0, minusIndex).trim()
                val right = substituted.substring(minusIndex + 3).trim()
                performSubtraction(left, right)
            }
            else -> {
                // No operator found, try to parse as single operand
                parseSingleOperand(substituted)
            }
        }
    }
    
    private fun substituteVariables(expression: String, context: Map<String, Any>): String {
        var result = expression
        
        // Handle special variables
        result = result.replace("TODAY", LocalDate.now().toString())
        result = result.replace("NOW", LocalDateTime.now().toString())
        
        // Handle user-defined variables
        VARIABLE_PATTERN.findAll(result).forEach { match ->
            val variableName = match.groupValues[1]
            val value = context[variableName] 
                ?: throw IllegalArgumentException("Variable not found in context: $variableName")
            
            result = result.replace(match.value, value.toString())
        }
        
        return result
    }
    
    private fun performAddition(leftStr: String, rightStr: String): LocalDateTime {
        val left = parseTemporalValue(leftStr)
        val right = parseTemporalValue(rightStr)
        
        return when {
            left is LocalDateTime && right is Duration -> left.plus(right)
            left is Duration && right is LocalDateTime -> right.plus(left)
            left is LocalDate && right is Duration -> left.atStartOfDay().plus(right)
            left is Duration && right is LocalDate -> right.atStartOfDay().plus(left)
            left is LocalTime && right is Duration -> left.atDate(LocalDate.now()).plus(right)
            left is Duration && right is LocalTime -> right.atDate(LocalDate.now()).plus(left)
            else -> throw IllegalArgumentException("Cannot add $left and $right. One operand must be a duration.")
        }
    }
    
    private fun performSubtraction(leftStr: String, rightStr: String): LocalDateTime {
        val left = parseTemporalValue(leftStr)
        val right = parseTemporalValue(rightStr)
        
        return when {
            left is LocalDateTime && right is Duration -> left.minus(right)
            left is LocalDate && right is Duration -> left.atStartOfDay().minus(right)
            left is LocalTime && right is Duration -> left.atDate(LocalDate.now()).minus(right)
            else -> throw IllegalArgumentException("Cannot subtract $right from $left. Right operand must be a duration.")
        }
    }
    
    private fun parseSingleOperand(operand: String): LocalDateTime {
        val temporal = parseTemporalValue(operand)
        return when (temporal) {
            is LocalDateTime -> temporal
            is LocalDate -> temporal.atStartOfDay()
            is LocalTime -> temporal.atDate(LocalDate.now())
            else -> throw IllegalArgumentException("Single operand must be a date, time, or datetime, not: $operand")
        }
    }
    
    private fun parseTemporalValue(value: String): Any {
        val trimmed = value.trim()
        
        // Check for timezone information and reject it
        if (trimmed.contains("Z") || 
            (trimmed.contains("+") || trimmed.contains("-")) && 
            trimmed.matches(Regex(".*[+\\-]\\d{2}:?\\d{2}$"))) {
            throw IllegalArgumentException("Timezone information is not supported: $trimmed")
        }
        
        return when {
            // Try to parse as Duration
            trimmed.startsWith("P") -> {
                try {
                    Duration.parse(trimmed)
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("Invalid duration format: $trimmed", e)
                }
            }
            
            // Try to parse as LocalDateTime
            trimmed.contains("T") -> {
                try {
                    LocalDateTime.parse(trimmed)
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("Invalid datetime format: $trimmed", e)
                }
            }
            
            // Try to parse as LocalDate
            trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                try {
                    LocalDate.parse(trimmed)
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("Invalid date format: $trimmed", e)
                }
            }
            
            // Try to parse as LocalTime
            trimmed.matches(Regex("\\d{2}:\\d{2}(:\\d{2})?(\\.\\d+)?")) -> {
                try {
                    LocalTime.parse(trimmed)
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("Invalid time format: $trimmed", e)
                }
            }
            
            else -> throw IllegalArgumentException("Unable to parse temporal value: $trimmed")
        }
    }
} 