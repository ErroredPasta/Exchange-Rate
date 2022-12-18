package com.example.exchangerate.di.app

import androidx.lifecycle.ViewModelProvider
import com.example.exchangerate.presentation.MainViewModel
import dagger.Binds
import dagger.Module

@Module
interface ViewModelFactoryModule {
    @Binds
    fun bindMainViewModelFactory(factory: MainViewModel.Factory): ViewModelProvider.Factory
}