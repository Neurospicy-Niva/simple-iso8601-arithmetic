# Simple ISO 8601 Arithmetic

[![CI/CD](https://github.com/steineggerroland/simple-iso8601-arithmetic/actions/workflows/ci.yml/badge.svg)](https://github.com/steineggerroland/simple-iso8601-arithmetic/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/icu.neurospicy/simple-iso8601-arithmetic.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22icu.neurospicy%22%20AND%20a:%22simple-iso8601-arithmetic%22)

A lightweight Kotlin library for parsing and evaluating time arithmetic expressions using ISO 8601 format.

## Features

- ✅ Parse ISO 8601 dates, times, and durations
- ✅ Evaluate arithmetic expressions with `+` and `-` operations
- ✅ Variable substitution with `${variableName}` syntax
- ✅ Special variables: `TODAY` and `NOW`
- ✅ Type-safe context building
- ✅ Timezone-aware (rejects timezone information for safety)
- ✅ Comprehensive error handling

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("icu.neurospicy:simple-iso8601-arithmetic:1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'icu.neurospicy:simple-iso8601-arithmetic:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>icu.neurospicy</groupId>
    <artifactId>simple-iso8601-arithmetic</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

```kotlin
import icu.neurospicy.iso8601arithmetic.TemporalExpressionEvaluator

val evaluator = TemporalExpressionEvaluator()

// Simple arithmetic
val result1 = evaluator.evaluate("2024-01-15T09:00 + PT2H")
// Result: 2024-01-15T11:00

// Using variables
val context = mapOf("startTime" to LocalDateTime.of(2024, 1, 15, 9, 0))
val result2 = evaluator.evaluate("\${startTime} + PT30M", context)
// Result: 2024-01-15T09:30

// Special variables
val result3 = evaluator.evaluate("TODAY + P1D")
// Result: Tomorrow at 00:00
```

## Supported Formats

### Date and Time Formats

- **Date**: `2024-01-15`
- **Time**: `09:30`, `09:30:45`, `09:30:45.123`
- **DateTime**: `2024-01-15T09:30:45`

### Duration Formats (ISO 8601)

- **Hours**: `PT2H` (2 hours)
- **Minutes**: `PT30M` (30 minutes)
- **Days**: `P1D` (1 day)
- **Complex**: `P1DT2H30M` (1 day, 2 hours, 30 minutes)

### Operations

- **Addition**: `datetime + duration` or `duration + datetime`
- **Subtraction**: `datetime - duration`

## Usage Examples

### Basic Arithmetic

```kotlin
val evaluator = TemporalExpressionEvaluator()

// Date arithmetic
evaluator.evaluate("2024-01-15 + P1D")
// Result: 2024-01-16T00:00

// Time arithmetic (uses today's date)
evaluator.evaluate("09:00 + PT1H30M")
// Result: Today at 10:30

// DateTime arithmetic
evaluator.evaluate("2024-01-15T09:00 - PT30M")
// Result: 2024-01-15T08:30
```

### Variable Substitution

```kotlin
val context = mapOf(
    "meetingStart" to LocalDateTime.of(2024, 1, 15, 14, 0),
    "bufferTime" to Duration.ofMinutes(15)
)

val result = evaluator.evaluate("\${meetingStart} - \${bufferTime}", context)
// Result: 2024-01-15T13:45
```

### Type-Safe Context Building

```kotlin
val context = TemporalContext.create()
    .addDateTime("start", LocalDateTime.of(2024, 1, 15, 9, 0))
    .addDuration("duration", Duration.ofHours(2))
    .addDate("deadline", LocalDate.of(2024, 1, 20))

val result = evaluator.evaluate("\${start} + \${duration}", context)
// Result: 2024-01-15T11:00
```

### Special Variables

```kotlin
// TODAY - current date at 00:00
evaluator.evaluate("TODAY + PT9H")
// Result: Today at 09:00

// NOW - current date and time
evaluator.evaluate("NOW + PT1H")
// Result: One hour from now
```

## Error Handling

The library provides clear error messages for invalid inputs:

```kotlin
// Invalid duration format
evaluator.evaluate("2024-01-15T09:00 + invalid-duration")
// Throws: IllegalArgumentException("Unable to parse temporal value: invalid-duration")

// Missing variable
evaluator.evaluate("\${missing} + PT1H")
// Throws: IllegalArgumentException("Variable not found in context: missing")

// Timezone information (not supported)
evaluator.evaluate("2024-01-15T09:00Z + PT1H")
// Throws: IllegalArgumentException("Timezone information is not supported: 2024-01-15T09:00Z")

// Invalid operations
evaluator.evaluate("2024-01-15T09:00 + 2024-01-15T10:00")
// Throws: IllegalArgumentException("Cannot add ... One operand must be a duration.")
```

## API Reference

### TemporalExpressionEvaluator

Main class for evaluating temporal arithmetic expressions.

#### Methods

- `evaluate(expression: String): LocalDateTime`
- `evaluate(expression: String, context: Map<String, Any>): LocalDateTime`
- `evaluate(expression: String, context: TemporalContext): LocalDateTime`

### TemporalContext

Type-safe builder for creating evaluation contexts.

#### Methods

- `create(): TemporalContext` - Create empty context
- `addDate(name: String, value: LocalDate): TemporalContext`
- `addTime(name: String, value: LocalTime): TemporalContext`
- `addDateTime(name: String, value: LocalDateTime): TemporalContext`
- `addDuration(name: String, value: Duration): TemporalContext`
- `from(values: Map<String, Any>): TemporalContext` - Create from existing map

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Requirements

- Java 21 or higher
- Kotlin 2.0.21 or higher

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
``` 