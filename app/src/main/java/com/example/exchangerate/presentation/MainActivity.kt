package com.example.exchangerate.presentation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.exchangerate.R
import com.example.exchangerate.databinding.ActivityMainBinding
import com.example.exchangerate.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mainActivityComponent by lazy {
        (application as ConversionApplication).appComponent
            .getMainActivityComponentFactory()
            .create()
    }

    private val baseCurrencySelectionDialog by lazy {
        CurrencySelectionDialog(
            context = this,
            title = getString(R.string.select_a_base_currency),
            defaultSelectedItemIndex = Currency.values()
                .indexOf(viewModel.inputState.value.baseCurrency),
            onSelectItem = {
                viewModel.onBaseCurrencyChange(baseCurrency = it)
            }
        )
    }

    private val targetCurrencySelectionDialog by lazy {
        CurrencySelectionDialog(
            context = this,
            title = getString(R.string.select_a_target_currency),
            defaultSelectedItemIndex = Currency.values()
                .indexOf(viewModel.inputState.value.targetCurrency),
            onSelectItem = {
                viewModel.onTargetCurrencyChange(targetCurrency = it)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainActivityComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@MainActivity
            viewModel = this@MainActivity.viewModel
        }

        setClickListeners()

        collectFlowWhenStarted(viewModel.conversionResultState) {
            binding.targetCurrencyAmountTextInput.editText!!.setText(String.format("%f", it))
        }
    }

    private fun <T> collectFlowWhenStarted(flow: Flow<T>, collector: FlowCollector<T>) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(collector)
            }
        }
    }

    private fun setClickListeners() = with(binding) {
        baseCurrencyText.setOnClickListener { baseCurrencySelectionDialog.show() }
        targetCurrencyText.setOnClickListener { targetCurrencySelectionDialog.show() }
    }
}