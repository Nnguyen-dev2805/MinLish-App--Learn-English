package com.example.minlishapp_learnenglish.viewModel.auth

sealed interface AuthEffect {
    data object NavigateHome : AuthEffect
    data object NavigateLogin : AuthEffect
    data object NavigateRegister : AuthEffect
    data object NavigateForgotPassword : AuthEffect
    data object NavigateVerifyOtp : AuthEffect
    data object NavigateResetPassword : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
}
