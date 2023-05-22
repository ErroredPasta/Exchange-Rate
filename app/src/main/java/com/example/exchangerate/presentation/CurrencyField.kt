package com.example.exchangerate.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exchangerate.domain.model.Currency
import com.example.exchangerate.presentation.theme.ExchangeRateTheme

@Composable
fun CurrencyField(
    title: String,
    currency: Currency,
    onCurrencyClick: () -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    amountFieldClickable: Boolean,
    modifier: Modifier = Modifier,
) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Text(
                text = currency.toString(),
                modifier = Modifier
                    .width(50.dp)
                    .clickable { onCurrencyClick() },
            )
            OutlinedTextField(
                value = amountText,
                label = { Text(text = currency.currencyName) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { onAmountChange(it) },
                modifier = Modifier.clickable(enabled = amountFieldClickable) {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyFieldPreview() {
    ExchangeRateTheme {
        CurrencyField(
            title = "From",
            currency = Currency.USD,
            amountText = "%f".format(12.3),
            onAmountChange = {},
            onCurrencyClick = {},
            amountFieldClickable = true
        )
    }
}