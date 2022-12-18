package com.example.exchangerate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.domain.repository.ConversionRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Provider

class MainViewModel @Inject constructor(
    private val repository: ConversionRepository
) : ViewModel() {

    private val _inputState = MutableStateFlow(
        ConversionInputUiState(
            baseCurrency = Currency.KRW,
            targetCurrency = Currency.USD
        )
    )
    val inputState = _inputState.asStateFlow()

    val conversionResultState = _inputState.map { uiState ->
        repository.convertCurrency(
            from = uiState.baseCurrency,
            to = uiState.targetCurrency,
            amount = uiState.baseCurrencyAmount
        ).conversionResult
    }


    fun onBaseCurrencyAmountChange(amount: String) {
        val amountValue = if (amount.isEmpty()) 0.0 else amount.toDouble()
        _inputState.update { it.copy(baseCurrencyAmount = amountValue) }
    }

    fun onBaseCurrencyChange(baseCurrency: Currency) {
        _inputState.update { it.copy(baseCurrency = baseCurrency) }
    }

    fun onTargetCurrencyChange(targetCurrency: Currency) {
        _inputState.update { it.copy(targetCurrency = targetCurrency) }
    }

    class Factory @Inject constructor(
        private val provider: Provider<MainViewModel>
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return provider.get() as T
        }
    }
}

data class ConversionInputUiState(
    val baseCurrency: Currency,
    val baseCurrencyAmount: Double = 1.0,
    val targetCurrency: Currency
)