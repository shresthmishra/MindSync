package com.sevenlabs.mindsync.data

import androidx.room.Embedded
import androidx.room.Relation

data class JournalWithInsight(
    @Embedded val entry: JournalEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val insight: AIInsight?
)