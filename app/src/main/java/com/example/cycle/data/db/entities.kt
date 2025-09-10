package com.example.cycle.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "period_day")
data class PeriodDay(
    @PrimaryKey val date: LocalDate,
    val flow: Int = 1,
    val note: String? = null
)
