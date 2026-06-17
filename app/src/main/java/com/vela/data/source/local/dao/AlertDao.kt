package com.vela.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vela.data.source.local.model.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts WHERE coinId = :coinId")
    fun observeByCoinId(coinId: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getById(id: Long): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AlertEntity): Long

    @Update
    suspend fun update(entity: AlertEntity)

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun delete(id: Long)
}
