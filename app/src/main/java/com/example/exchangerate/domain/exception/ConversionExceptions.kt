package com.example.exchangerate.domain.exception

sealed class ConversionExceptions: RuntimeException() {
    object UnsupportedCode: ConversionExceptions()
    object MalformedRequest: ConversionExceptions()
}