package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.data.local.dao.UserDao

internal class LocalAuthRequiredException(
    message: String = "Please log in first."
) : Exception(message)

internal suspend fun UserDao.requireUserId(): Long {
    return getLoggedInUser()?.id ?: throw LocalAuthRequiredException()
}
