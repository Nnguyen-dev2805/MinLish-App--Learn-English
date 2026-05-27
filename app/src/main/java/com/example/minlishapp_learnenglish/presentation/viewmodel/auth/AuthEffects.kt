package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

sealed interface AuthEffect {
    data object NavigateHome : AuthEffect
    data object NavigateOnboarding : AuthEffect
    data object NavigateLogin : AuthEffect
    data object NavigateRegister : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
}
