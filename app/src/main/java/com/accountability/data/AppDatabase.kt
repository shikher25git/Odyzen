package com.accountability.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Secrets
    @Query("SELECT * FROM secrets")
    fun getAllSecrets(): Flow<List<SecretKeyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecret(secret: SecretKeyEntity)

    @Delete
    suspend fun deleteSecret(secret: SecretKeyEntity)

    @Query("SELECT * FROM secrets WHERE id = :id")
    suspend fun getSecretById(id: String): SecretKeyEntity?

    // Blocked Apps
    @Query("SELECT * FROM blocked_apps")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :pkg")
    suspend fun getBlockedApp(pkg: String): BlockedAppEntity?

    @Query("SELECT * FROM blocked_apps WHERE packageName = :pkg")
    fun getBlockedAppFlow(pkg: String): Flow<BlockedAppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Update
    suspend fun updateBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :pkg")
    suspend fun deleteBlockedApp(pkg: String)
}

@Database(entities = [SecretKeyEntity::class, BlockedAppEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accountability_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
