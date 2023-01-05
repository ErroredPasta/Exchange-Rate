package com.example.exchangerate.presentation

import com.example.exchangerate.domain.model.ConversionResult
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.domain.repository.ConversionRepository
import com.example.exchangerate.rule.TestCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var sut: MainViewModel

    // region test doubles =========================================================================
    private lateinit var repository: ConversionRepository
    // endregion test doubles ======================================================================

    @Before
    fun setup() {
        repository = mockk()

        sut = MainViewModel(repository = repository)
    }

    @Test
    fun `on base currency change, update input state and conversion result as success`() =
        runTest {
            // arrange
            coEvery {
                repository.convertCurrency(from = Currency.GBP, to = Currency.USD, amount = 1.0)
            } returns ConversionResult(
                from = Currency.GBP,
                to = Currency.USD,
                conversionResult = 1.205
            )

            val collectJob = launch(testCoroutineRule.dispatcher) {
                sut.conversionResultState.collect()
            }

            // act
            sut.onBaseCurrencyChange(baseCurrency = Currency.GBP)
            advanceUntilIdle()

            // assert
            assertThat(sut.inputState.value.baseCurrency).isEqualTo(Currency.GBP)

            val conversionResultData =
                (sut.conversionResultState.value as ConversionResultUiState.Success).data
            assertThat(conversionResultData).isEqualTo(1.205)

            collectJob.cancel()
        }

    @Test
    fun `on target currency change, update input state and conversion result as success`() =
        runTest {
            // arrange
            coEvery {
                repository.convertCurrency(from = Currency.KRW, to = Currency.GBP, amount = 1.0)
            } returns ConversionResult(
                from = Currency.KRW,
                to = Currency.GBP,
                conversionResult = 0.00064709
            )

            val collectJob = launch(testCoroutineRule.dispatcher) {
                sut.conversionResultState.collect()
            }

            // act
            sut.onTargetCurrencyChange(targetCurrency = Currency.GBP)
            advanceUntilIdle()

            // assert
            assertThat(sut.inputState.value.targetCurrency).isEqualTo(Currency.GBP)

            val conversionResultData =
                (sut.conversionResultState.value as ConversionResultUiState.Success).data
            assertThat(conversionResultData).isEqualTo(0.00064709)

            collectJob.cancel()
        }

    @Test
    fun `on base currency amount change, update input state and conversion result as success`() =
        runTest {
            // arrange
            coEvery {
                repository.convertCurrency(from = Currency.KRW, to = Currency.USD, amount = 13.0)
            } returns ConversionResult(
                from = Currency.KRW,
                to = Currency.USD,
                conversionResult = 0.01014754
            )

            val collectJob = launch(testCoroutineRule.dispatcher) {
                sut.conversionResultState.collect()
            }

            // act
            sut.onBaseCurrencyAmountChange(amount = "13.0")
            advanceUntilIdle()

            // assert
            assertThat(sut.inputState.value.baseCurrencyAmount).isEqualTo(13.0)

            val conversionResultData =
                (sut.conversionResultState.value as ConversionResultUiState.Success).data
            assertThat(conversionResultData).isEqualTo(0.01014754)

            collectJob.cancel()
        }

    @Test
    fun `on base currency amount change, given invalid amount, update input state and conversion result as error`() =
        runTest {
            // arrange
            coEvery {
                repository.convertCurrency(from = Currency.KRW, to = Currency.USD, amount = -13.0)
                // 원래는 HttpException을 throw해야하지만
                // 생성할 수 없으므로 IllegalArgumentException로 대체
            } throws IllegalArgumentException()

            val collectJob = launch(testCoroutineRule.dispatcher) {
                sut.conversionResultState.collect()
            }

            // act
            sut.onBaseCurrencyAmountChange(amount = "-13.0")
            advanceUntilIdle()

            // assert
            assertThat(sut.inputState.value.baseCurrencyAmount).isEqualTo(-13.0)

            val thrownError =
                (sut.conversionResultState.value as ConversionResultUiState.Error).error
            assertThat(thrownError).isInstanceOf(IllegalArgumentException::class.java)

            collectJob.cancel()
        }
}