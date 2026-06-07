package com.example.minlishapp_learnenglish.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.minlishapp_learnenglish.data.local.dao.DeckDao
import com.example.minlishapp_learnenglish.data.local.dao.NotificationSettingsDao
import com.example.minlishapp_learnenglish.data.local.dao.ReviewDao
import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.dao.WordDao
import com.example.minlishapp_learnenglish.data.local.entity.DeckEntity
import com.example.minlishapp_learnenglish.data.local.entity.NotificationSettingsEntity
import com.example.minlishapp_learnenglish.data.local.entity.ReviewLogEntity
import com.example.minlishapp_learnenglish.data.local.entity.ReviewStateEntity
import com.example.minlishapp_learnenglish.data.local.entity.UserEntity
import com.example.minlishapp_learnenglish.data.local.entity.VocabularyWordEntity

@Database(
    entities = [
        UserEntity::class,
        DeckEntity::class,
        VocabularyWordEntity::class,
        ReviewStateEntity::class,
        ReviewLogEntity::class,
        NotificationSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class MinLishDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deckDao(): DeckDao
    abstract fun wordDao(): WordDao
    abstract fun reviewDao(): ReviewDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao

    companion object {
        private const val DATABASE_NAME = "minlish.db"

        @Volatile
        private var INSTANCE: MinLishDatabase? = null

        fun getDatabase(context: Context): MinLishDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MinLishDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
