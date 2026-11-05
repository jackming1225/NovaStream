package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StreamEntity::class], version = 1, exportSchema = false)
abstract class StreamDatabase : RoomDatabase() {
    abstract fun streamDao(): StreamDao

    companion object {
        @Volatile
        private var INSTANCE: StreamDatabase? = null

        fun getDatabase(context: Context): StreamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreamDatabase::class.java,
                    "novastream_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
