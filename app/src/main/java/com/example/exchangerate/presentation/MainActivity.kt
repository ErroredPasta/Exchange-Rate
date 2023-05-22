package com.example.exchangerate.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.exchangerate.presentation.theme.ExchangeRateTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ConversionApplication).appComponent
            .getMainActivityComponentFactory()
            .create()
            .inject(this)

        super.onCreate(savedInstanceState)

        setContent {
            ExchangeRateTheme {
                val scaffoldState = rememberScaffoldState()
                val viewModel by viewModels<MainViewModel> { viewModelFactory }

                Scaffold(
                    scaffoldState = scaffoldState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ConversionScreen(
                        snackbarHostState = scaffoldState.snackbarHostState,
                        viewModel = viewModel,
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }
}