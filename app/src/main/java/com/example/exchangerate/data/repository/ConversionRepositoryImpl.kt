package com.example.exchangerate.data.repository

import com.example.exchangerate.data.mapper.toConversionResult
import com.example.exchangerate.data.remote.ExchangeRateApi
import com.example.exchangerate.domain.exception.ConversionExceptions
import com.example.exchangerate.domain.model.ConversionResult
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.domain.repository.ConversionRepository
import com.example.exchangerate.util.runCoroutineCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class ConversionRepositoryImpl @Inject constructor(
    private val api: ExchangeRateApi,
    private val ioDispatcher: CoroutineDispatcher
) : ConversionRepository {

    override suspend fun convertCurrency(
        from: Currency,
        to: Currency,
        amount: Double
    ): ConversionResult = withContext(ioDispatcher) {
        return@withContext runCoroutineCatching {
            api.convertCurrency(
                from = from.toString(),
                to = to.toString(),
                amount = amount
            ).toConversionResult()
        }.getOrElse {
            if (it is HttpException) {
                whenHttpExceptionThrown(exception = it)
            }

            throw it
        }
    }

    private fun whenHttpExceptionThrown(exception: HttpException) {
        exception.message?.let { message ->
            when {
                message.contains("404") -> throw ConversionExceptions.UnsupportedCode
                message.contains("400") -> throw ConversionExceptions.MalformedRequest
                else -> throw exception
            }
        }
    }
}