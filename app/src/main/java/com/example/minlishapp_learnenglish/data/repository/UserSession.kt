package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.entity.UserEntity

class LocalAuthRequiredException(
    message: String = "Please log in first."
) : Exception(message)

suspend fun UserDao.requireUserId(): Long {
    val user: UserEntity = getLoggedInUser() ?: throw LocalAuthRequiredException()
    return user.id
}
