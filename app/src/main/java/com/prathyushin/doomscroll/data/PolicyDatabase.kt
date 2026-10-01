package com.prathyushin.doomscroll.data

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "app_targets")
data class AppTargetEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val enabled: Boolean
)

@Entity(tableName = "intervention_history")
data class InterventionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestampMs: Long,
    val reason: String,
    val sessionSeconds: Long,
    val scrollRatePerMinute: Double
)

@androidx.room.Dao
interface PolicyDao {
    @androidx.room.Query("SELECT * FROM app_targets WHERE enabled = 1")
    fun enabledTargets(): List<AppTargetEntity>

    @androidx.room.Query("SELECT enabled FROM app_targets WHERE packageName = :packageName LIMIT 1")
    fun isEnabled(packageName: String): Boolean?

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun upsertTarget(target: AppTargetEntity)

    @androidx.room.Insert
    fun recordIntervention(event: InterventionHistoryEntity)
}

@Database(
    entities = [AppTargetEntity::class, InterventionHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PolicyDatabase : RoomDatabase() {
    abstract fun policyDao(): PolicyDao

    companion object {
        @Volatile private var INSTANCE: PolicyDatabase? = null

        fun get(context: Context): PolicyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PolicyDatabase::class.java,
                    "doomscroll.db"
                ).build().also { INSTANCE = it }
            }
    }
}
