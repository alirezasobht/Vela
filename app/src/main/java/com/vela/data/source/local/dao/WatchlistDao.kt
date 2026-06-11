package com.vela.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vela.data.source.local.model.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE coinId = :coinId")
    suspend fun delete(coinId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE coinId = :coinId)")
    fun isWatchlisted(coinId: String): Flow<Boolean>
}