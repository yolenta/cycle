package com.example.cycle.data.db

import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PeriodDayDao {
    @Upsert suspend fun upsert(day: PeriodDay)
    @Query("DELETE FROM period_day WHERE date= :date") suspend fun deleteByDate(date: LocalDate)
    @Query("SELECT * FROM period_day ORDER BY date ASC") fun observeALL(): Flow<List<PeriodDay>>
    @Query("SELECT * FROM period_day WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<PeriodDay>
}