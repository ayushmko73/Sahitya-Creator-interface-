package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CharacterDao
import com.example.data.local.dao.CreatorDao
import com.example.data.local.dao.PipelineLogDao
import com.example.data.local.dao.SeasonDao
import com.example.data.local.dao.StoryDao
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.CreatorProfileEntity
import com.example.data.local.entities.PipelineLogEntity
import com.example.data.local.entities.SeasonEntity
import com.example.data.local.entities.StoryEntity

@Database(
    entities = [
        CreatorProfileEntity::class,
        StoryEntity::class,
        CharacterEntity::class,
        SeasonEntity::class,
        PipelineLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creatorDao(): CreatorDao
    abstract fun storyDao(): StoryDao
    abstract fun characterDao(): CharacterDao
    abstract fun seasonDao(): SeasonDao
    abstract fun pipelineLogDao(): PipelineLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahitya_creator_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
