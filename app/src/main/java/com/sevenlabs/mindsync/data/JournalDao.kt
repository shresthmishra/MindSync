package com.sevenlabs.mindsync.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: AIInsight)

    @Transaction
    @Query("SELECT * FROM journal_entries ORDER BY id DESC")
    fun getJournalHistory(): Flow<List<JournalWithInsight>>

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)
}