// File: app/src/main/java/com/kreedaprerana/scout/data/AppDatabase.kt
package com.kreedaprerana.scout.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Athlete::class, Trial::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao
    abstract fun trialDao(): TrialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kreeda_prerana_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
