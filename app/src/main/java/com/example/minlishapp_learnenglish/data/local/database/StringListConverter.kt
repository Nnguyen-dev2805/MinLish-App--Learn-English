package com.example.minlishapp_learnenglish.data.local.database

import androidx.room.TypeConverter

class StringListConverter {
    private val separator = "||"

    @TypeConverter
    fun fromList(values: List<String>): String {
        return values.joinToString(separator)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return value
            .takeIf { it.isNotBlank() }
            ?.split(separator)
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
}
