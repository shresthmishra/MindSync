package com.sevenlabs.mindsync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)
}