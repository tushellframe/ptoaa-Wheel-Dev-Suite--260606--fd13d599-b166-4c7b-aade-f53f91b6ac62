package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RelationalNode::class,
        RelationalEdge::class,
        CeremonyLog::class,
        ResearchCycle::class,
        VoiceRecording::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nodeDao(): RelationalNodeDao
    abstract fun edgeDao(): RelationalEdgeDao
    abstract fun ceremonyDao(): CeremonyLogDao
    abstract fun cycleDao(): ResearchCycleDao
    abstract fun recordingDao(): VoiceRecordingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicinewheel_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
