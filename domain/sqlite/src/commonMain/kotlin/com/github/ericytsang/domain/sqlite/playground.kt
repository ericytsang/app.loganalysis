
package com.github.ericytsang.domain.sqlite

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

// shared/src/commonMain/kotlin/Database.kt

@Database(entities = [TodoEntity::class], version = 1)
//@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): TodoDao
}

// The Room compiler generates the `actual` implementations.
//@Suppress(
////    "KotlinNoActualForExpect",
//    "NO_ACTUAL_FOR_EXPECT",
////    "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
//)
//expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
//    override fun initialize(): AppDatabase
//}

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(item: TodoEntity)

    @Query("SELECT count(*) FROM TodoEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM TodoEntity")
    fun getAllAsFlow(): Flow<List<TodoEntity>>
}

@Entity
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String
)

fun getRoomDatabase(
    kotlinDependencyProvider:KotlinDependencyProvider,
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .addMigrations(*MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinDependencyProvider.dispatchers.io)
        .build()
}

val MIGRATIONS = arrayOf<Migration>()
