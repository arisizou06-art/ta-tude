package com.tudecitrus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tudecitrus.data.local.dao.AppSettingsDao
import com.tudecitrus.data.local.dao.DetectionResultDao
import com.tudecitrus.data.local.dao.DetectionStatisticsDao
import com.tudecitrus.data.local.dao.DiseaseInfoDao
import com.tudecitrus.data.model.AppSettingEntity
import com.tudecitrus.data.model.DetectionResultEntity
import com.tudecitrus.data.model.DetectionStatisticEntity
import com.tudecitrus.data.model.DiseaseInfoEntity
import com.tudecitrus.data.seed.DatabaseSeedCallback

@Database(
    entities = [
        DetectionResultEntity::class,
        DiseaseInfoEntity::class,
        AppSettingEntity::class,
        DetectionStatisticEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun detectionResultDao(): DetectionResultDao
    abstract fun diseaseInfoDao(): DiseaseInfoDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun detectionStatisticsDao(): DetectionStatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "citruscare.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseSeedCallback.create())
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
