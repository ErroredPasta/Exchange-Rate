package com.example.exchangerate.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchangerate.R
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.presentation.theme.ExchangeRateTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ConversionScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val inputUiState by viewModel.inputState.collectAsState()
    val resultUiState by viewModel.conversionResultState.collectAsState()

    val baseCurrencySelectionDialog = CurrencySelectionDialog(
        context = context,
        title = context.getString(R.string.select_a_base_currency),
        defaultSelectedItemIndex = Currency.values()
            .indexOf(inputUiState.baseCurrency),
        onSelectItem = viewModel::onBaseCurrencyChange
    )

    val targetCurrencySelectionDialog = CurrencySelectionDialog(
        context = context,
        title = context.getString(R.string.select_a_target_currency),
        defaultSelectedItemIndex = Currency.values()
            .indexOf(inputUiState.targetCurrency),
        onSelectItem = viewModel::onTargetCurrencyChange
    )

    LaunchedEffect(key1 = viewModel.conversionResultState) {
        viewModel.conversionResultState.collectLatest {
            if (it is ConversionResultUiState.Error) {
                snackbarHostState.showSnackbar(
                    message = it.error.message ?: context.getString(R.string.unknown_error)
                )
            }
        }
    }

    ConversionScreenContent(
        inputUiState = inputUiState,
        onBaseCurrencyClick = baseCurrencySelectionDialog::show,
        onBaseAmountChange = viewModel::onBaseCurrencyAmountChange,
        resultUiState = resultUiState,
        onTargetCurrencyClick = targetCurrencySelectionDialog::show,
        modifier = modifier.padding(10.dp)
    )
}


@Composable
private fun ConversionScreenContent(
    modifier: Modifier = Modifier,
    inputUiState: ConversionInputUiState,
    onBaseCurrencyClick: () -> Unit = {},
    onBaseAmountChange: (String) -> Unit = {},
    resultUiState: ConversionResultUiState,
    onTargetCurrencyClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        CurrencyField(
            title = stringResource(R.string.from),
            currency = inputUiState.baseCurrency,
            onCurrencyClick = onBaseCurrencyClick,
            amountText = "%f".format(inputUiState.baseCurrencyAmount),
            onAmountChange = onBaseAmountChange,
            amountFieldClickable = true
        )

        CurrencyField(
            title = stringResource(R.string.to),
            currency = inputUiState.targetCurrency,
            onCurrencyClick = onTargetCurrencyClick,
            amountText = resultUiState.amountText,
            onAmountChange = {},
            amountFieldClickable = false
        )
    }
}

private val ConversionResultUiState.amountText: String
    @Composable get() = when (this) {
        ConversionResultUiState.Loading -> stringResource(R.string.loading)
        is ConversionResultUiState.Success -> "%f".format(this.data)
        is ConversionResultUiState.Error -> "" // error message will be shown through snackbar
    }


@Preview(showBackground = true)
@Composable
fun ConversionScreenSuccessPreview() {
    ExchangeRateTheme {
        ConversionScreenContent(
            inputUiState = ConversionInputUiState(
                baseCurrency = Currency.USD,
                baseCurrencyAmount = 12.3,
                targetCurrency = Currency.KRW
            ),
            resultUiState = ConversionResultUiState.Success(
                data = 2345.67
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConversionScreenLoadingPreview() {
    ExchangeRateTheme {
        ConversionScreenContent(
            inputUiState = ConversionInputUiState(
                baseCurrency = Currency.USD,
                baseCurrencyAmount = 12.3,
                targetCurrency = Currency.KRW
            ),
            resultUiState = ConversionResultUiState.Loading
        )
    }
}