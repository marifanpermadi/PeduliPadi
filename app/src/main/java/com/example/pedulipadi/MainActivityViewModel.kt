package com.example.pedulipadi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {

    private val _isSplashScreen = MutableStateFlow(true)
    val isSplashScreen = _isSplashScreen

    init {
        viewModelScope.launch {
            delay(1000)
            _isSplashScreen.value = false
        }
    }
}