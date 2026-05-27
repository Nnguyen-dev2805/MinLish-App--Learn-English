package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minlishapp_learnenglish.domain.usecase.auth.SetOnboardingSeenUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val setOnboardingSeenUseCase: SetOnboardingSeenUseCase
) : ViewModel() {
    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun getStarted() {
        viewModelScope.launch {
            setOnboardingSeenUseCase()
            _effects.emit(AuthEffect.NavigateRegister)
        }
    }

    fun login() {
        viewModelScope.launch {
            setOnboardingSeenUseCase()
            _effects.emit(AuthEffect.NavigateLogin)
        }
    }
}
