package com.example.minlishapp_learnenglish.core.result

fun AppError.isEmailNotVerified(): Boolean =
    this is AppError.Forbidden && code == "EMAIL_NOT_VERIFIED"
