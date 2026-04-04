package com.sevenlabs.mindsync.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_insights",
    indices = [Index(value = ["entryId"])],
    foreignKeys = [
        ForeignKey(
            entity = JournalEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AIInsight(
    @PrimaryKey(autoGenerate = true)
    val insightId: Int = 0,
    val entryId: Int,
    val sentiment: String,
    val coachingText: String
)