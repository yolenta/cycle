package com.example.cycle.data.db

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun epochToDate(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)
    @TypeConverter fun dateToEpoch(d: LocalDate?): Long? = d?.toEpochDay()
}