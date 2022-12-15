package com.example.exchangerate.di.app

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    NetworkModule::class,
    DispatcherModule::class
])
interface AppComponent {

}