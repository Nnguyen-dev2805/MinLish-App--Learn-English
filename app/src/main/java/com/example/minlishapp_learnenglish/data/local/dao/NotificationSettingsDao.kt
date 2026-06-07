package com.example.minlishapp_learnenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minlishapp_learnenglish.data.local.entity.NotificationSettingsEntity

@Dao
interface NotificationSettingsDao {
    @Query("SELECT * FROM notification_settings WHERE userId = :userId LIMIT 1")
    suspend fun getSettings(userId: Long): NotificationSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: NotificationSettingsEntity)
}
