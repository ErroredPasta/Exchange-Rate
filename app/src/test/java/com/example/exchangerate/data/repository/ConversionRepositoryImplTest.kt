package com.example.exchangerate.data.repository

import com.example.exchangerate.data.remote.ExchangeRateApi
import com.example.exchangerate.data.repository.response.*
import com.example.exchangerate.domain.exception.ConversionException
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.domain.repository.ConversionRepository
import com.example.exchangerate.rule.TestCoroutineRule
import com.example.exchangerate.util.runCoroutineCatching
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
class ConversionRepositoryImplTest {

    private lateinit var sut: ConversionRepository

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
    private val invalidAmountRequest = Request(from = "EUR", to = "GBP", amount = -1.0)
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
        val result = runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.getOrElse {
            Assert.fail(it.toString())
            return@runTest
        }

        // assert
        assertThat(result.from).isEqualTo(Currency.EUR)
        assertThat(result.to).isEqualTo(Currency.GBP)
        assertThat(result.conversionResult).isEqualTo(1.7178)
    }

    @Test
    fun `convert currency, unsupported code request, throws UnsupportedCode exception`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = unsupportedCodeResponse)
        )

        // act
        val (from, to, amount) = unsupportedCodeRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
            // assert
            assertThat(it).isEqualTo(ConversionException.UnsupportedCode)
            return@runTest
        }

        noExceptionThrown(expectedException = ConversionException.UnsupportedCode::class.java)
    }

    @Test
    fun `convert currency, malformed request, throws MalformedRequest exception`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = malformedRequestResponse)
        )

        // act
        val (from, to, amount) = malformedRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
            // assert
            assertThat(it).isEqualTo(ConversionException.MalformedRequest)
            return@runTest
        }

        noExceptionThrown(expectedException = ConversionException.MalformedRequest::class.java)
    }

    @Test
    fun `convert currency, request using invalid key, throws InvalidKey exception`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = invalidKeyResponse)
        )

        // act
        val (from, to, amount) = validRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
            // assert
            assertThat(it).isEqualTo(ConversionException.InvalidKey)
            return@runTest
        }

        noExceptionThrown(expectedException = ConversionException.InvalidKey::class.java)
    }

    @Test
    fun `convert currency, when the account is inactive, throws InactiveAccount exception`() =
        runTest {
            // arrange
            mockWebServer.enqueue(
                createMockResponse(response = inactiveAccountResponse)
            )

            // act
            val (from, to, amount) = validRequest

            runCoroutineCatching {
                sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
            }.onFailure {
                // assert
                assertThat(it).isEqualTo(ConversionException.InactiveAccount)
                return@runTest
            }

            noExceptionThrown(expectedException = ConversionException.InactiveAccount::class.java)
        }

    @Test
    fun `convert currency, when quota reached, throws QuotaReached exception`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = quotaReachedResponse)
        )

        // act
        val (from, to, amount) = validRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
            // assert
            assertThat(it).isEqualTo(ConversionException.QuotaReached)
            return@runTest
        }

        noExceptionThrown(expectedException = ConversionException.QuotaReached::class.java)
    }

    @Test
    fun `convert currency, given invalid amount, throws 404 HttpException`() = runTest {
        // arrange
        mockWebServer.enqueue(
            createMockResponse(response = invalidAmountResponse)
        )

        // act
        val (from, to, amount) = invalidAmountRequest

        runCoroutineCatching {
            sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
        }.onFailure {
            // assert
            assertThat(it).isInstanceOf(HttpException::class.java)
            assertThat(it.message).contains("404")
            return@runTest
        }

        noExceptionThrown(expectedException = HttpException::class.java)
    }

    @Test
    fun `convert currency, given an exception other than ConversionException, rethrows it`() =
        runTest {
            // arrange
            setUpWithMock {
                coEvery {
                    it.convertCurrency(from = "EUR", to = "GBP", amount = String.format("%f", 2.0))
                } throws Exception("Not a ConversionException")
            }

            // act
            val (from, to, amount) = validRequest

            runCoroutineCatching {
                sut.convertCurrency(from = from.toCurrency(), to = to.toCurrency(), amount = amount)
            }.onFailure {
                // assert
                assertThat(it).isNotInstanceOf(ConversionException::class.java)
                assertThat(it.message).isEqualTo("Not a ConversionException")
                return@runTest
            }

            noExceptionThrown(expectedException = Exception::class.java)
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

    private fun <T> noExceptionThrown(expectedException: Class<T>) {
        Assert.fail("Expected ${expectedException.simpleName} exception, but no exception was thrown")
    }

    private fun setUpWithMock(mockApiBehavior: (ExchangeRateApi) -> Unit) {
        sut = ConversionRepositoryImpl(
            api = mockk<ExchangeRateApi>().also { mockApiBehavior(it) },
            ioDispatcher = testCoroutineRule.dispatcher
        )
    }
    // endregion helper methods ====================================================================

    // region helper classes =======================================================================
    private data class Request(
        val from: String,
        val to: String,
        val amount: Double
    )
    // endregion helper classes ====================================================================
}