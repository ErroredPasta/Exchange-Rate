package com.example.exchangerate.data.repository

import com.example.exchangerate.data.remote.ExchangeRateApi
import com.example.exchangerate.data.repository.response.Response
import com.example.exchangerate.data.repository.response.malformedResponse
import com.example.exchangerate.data.repository.response.unsupportedCodeResponse
import com.example.exchangerate.data.repository.response.validConversionResponse
import com.example.exchangerate.domain.exception.ConversionExceptions
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.rule.TestCoroutineRule
import com.example.exchangerate.util.runCoroutineCatching
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
class ConversionRepositoryImplTest {

    private lateinit var sut: ConversionRepositoryImpl

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    // region test doubles =========================================================================
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var api: ExchangeRateApi
    // endregion test doubles ======================================================================

    // region constants ============================================================================
    private val validRequest = Request(from = "EUR", to = "GBP", amount = 2.0)
    private val unsupportedCodeRequest = Request(from = "EUR", to = "ABC", amount = 2.0)
    private val malformedRequest = Request(from = "EUR", to = "GBPE", amount = 2.0)
    // endregion constants =========================================================================

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        okHttpClient = buildOkHttpClient()
        api = buildRetrofit(okHttpClient = okHttpClient, mockWebServer = mockWebServer)

        sut = ConversionRepositoryImpl(
            api = api,
            ioDispatcher = testCoroutineRule.dispatcher
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `convert currency, successful response, returns conversion result`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = validConversionResponse)
        )

        // act
        val (from, to, amount) = validRequest
        val result = sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)

        // assert
        assertThat(result.from).isEqualTo(Currency.EUR)
        assertThat(result.to).isEqualTo(Currency.GBP)
        assertThat(result.conversionResult).isEqualTo(1.7178)
    }

    @Test
    fun `convert currency, unsupported code request, throws 404 HttpException`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = unsupportedCodeResponse)
        )

        // act
        val (from, to, amount) = unsupportedCodeRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
//            when (it) {
//                is HttpException -> assertThat(it).hasMessageThat().contains("404")
//                else -> Assert.fail("Expected an HttpException, but $it was thrown")
//            }
            assertThat(it).isEqualTo(ConversionExceptions.UnsupportedCode)
            return@runTest
        }

        Assert.fail("Expected an HttpException, but no exception was thrown")
    }

    @Test
    fun `convert currency, malformed request, throws 400 HttpException`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = malformedResponse)
        )

        // act
        val (from, to, amount) = malformedRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
            assertThat(it).isEqualTo(ConversionExceptions.MalformedRequest)
            return@runTest
        }

        Assert.fail("Expected an HttpException, but no exception was thrown")
    }

    // region helper methods =======================================================================
    private fun buildOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit(
        okHttpClient: OkHttpClient,
        mockWebServer: MockWebServer
    ): ExchangeRateApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ExchangeRateApi::class.java)

    private fun createMockResponse(response: Response): MockResponse = MockResponse()
        .setResponseCode(response.code)
        .setBody(response.body)

    /**
     * String을 Currency로 바꾸어주는 extension function
     *
     * @receiver Currency의 String
     * @return String과 일치하는 Currency를 return,
     * 테스트를 위해 만약 일치하는 String이 없으면 Currency.EUR을 return
     */
    private fun String.toCurrency() = kotlin.runCatching {
        Currency.valueOf(this)
    }.recover {
        Currency.EUR
    }.getOrThrow()
    // endregion helper methods ====================================================================

    // region helper classes =======================================================================
    private data class Request(
        val from: String,
        val to: String,
        val amount: Double
    )
    // endregion helper classes ====================================================================
}