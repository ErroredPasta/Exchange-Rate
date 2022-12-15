package com.example.exchangerate.data.mapper

import com.example.exchangerate.data.dto.convertresult.ConversionResultDto
import com.example.exchangerate.domain.model.ConversionResult

fun ConversionResultDto.toConversionResult() = ConversionResult(
    from = baseCode,
    to = targetCode,
    conversionResult = conversionResult
)