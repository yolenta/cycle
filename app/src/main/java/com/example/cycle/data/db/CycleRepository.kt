package com.example.cycle.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.math.roundToLong

data class CycleSpan(val start: LocalDate, val end: LocalDate){
    val lenghtDays: Int get()= (end.toEpochDay() - start.toEpochDay()).toInt()+1
}

data class  Summary(
    val avgCycle: Double?,
    val avgPeriod: Double?,
    val shortestCycle: Int?,
    val longestCycle: Int?,
    val lastPeriodStart: LocalDate?,
    val predictedNextStart: LocalDate?,
    val predictedOvulation: LocalDate?,
    val fertileStart: LocalDate?,
    val fertileEnd: LocalDate?
)

class CycleRepository(private  val dao: PeriodDayDao){

    fun observeAllDays(): Flow<List<PeriodDay>> = dao.observeALL()

    suspend fun toggle(date: LocalDate){
        // cek apakah sudah ada → hapus; jika belum → upsert
        val exists = dao.getBetween(date, date).isNotEmpty()
        if(exists) dao.deleteByDate(date) else dao.upsert(PeriodDay(date=date))
    }

    /** Group hari-hari haid menjadi span berurutan (siklus) */
    private  fun toSpans(sortedDates: List<LocalDate>): List<CycleSpan>{
        if(sortedDates.isEmpty()) return emptyList()
        val spans = mutableListOf<CycleSpan>()
        var start = sortedDates.first()
        var prev=start
        for (i in 1 until sortedDates.size){
            val d = sortedDates[i]
            if(d.toEpochDay()-prev.toEpochDay()<=1) {
                prev = d
            } else {
                spans += CycleSpan(start, prev)
                start = d
                prev = d
            }
        }
        spans += CycleSpan(start,prev)
        return spans
    }

    fun observeSummary(): Flow<Summary> = dao.observeALL().map { days ->
        val dates = days.map{ it.date }.distinct().sorted()
        val spans = toSpans(dates)
        val periodLengths = spans.map { it.lenghtDays}

        // start siklus = start setiap span; panjang siklus = selisih antar start
        val starts = spans.map{ it.start}
        val cycleLength = starts.zip(starts.drop(1)) { a, b ->
            (b.toEpochDay() - a.toEpochDay()).toInt()
        }

        val avgCycle = cycleLength.takeIf { it.isNotEmpty() }?.average()
        val avgPeriod = periodLengths.takeIf {  it.isNotEmpty() }?.average()
        val shortest = cycleLength.minOrNull()
        val longest = cycleLength.maxOrNull()
        val lastStart = starts.maxOrNull()

        val predicedNext = if (lastStart != null && avgCycle != null)
            lastStart.plusDays(avgCycle.roundToLong()) else null

        val predictedOv = predicedNext?.minusDays(14)
        val fertileStart = predictedOv?.minusDays(5)
        val fertileEnd = predictedOv

        Summary(
            avgCycle = avgCycle,
            avgPeriod = avgPeriod,
            shortestCycle = shortest,
            longestCycle = longest,
            lastPeriodStart = lastStart,
            predictedNextStart = predicedNext,
            predictedOvulation = predictedOv,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd
        )

    }
}