// File: app/src/main/java/com/kreedaprerana/scout/data/Athlete.kt
package com.kreedaprerana.scout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athletes")
data class Athlete(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val sport: String,
    val school: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getBadge(trials: List<Trial>): String {
        if (trials.isEmpty()) return "No Badge"
        val sport = sport.lowercase()
        val lastTrial = trials.maxByOrNull { it.recordedAt } ?: return "No Badge"

        return when {
            sport.contains("sprint") || sport.contains("athletics") || sport.contains("running") -> {
                val time = lastTrial.sprintSeconds ?: return "Beginner"
                when {
                    time <= 11.0 -> "🏅 National Level Ready"
                    time <= 12.5 -> "🥈 State Level Ready"
                    time <= 14.0 -> "🥉 District Level Ready"
                    else -> "⭐ School Champion"
                }
            }
            sport.contains("jump") || sport.contains("long jump") -> {
                val dist = lastTrial.jumpMeters ?: return "Beginner"
                when {
                    dist >= 6.5 -> "🏅 National Level Ready"
                    dist >= 5.5 -> "🥈 State Level Ready"
                    dist >= 4.5 -> "🥉 District Level Ready"
                    else -> "⭐ School Champion"
                }
            }
            sport.contains("kabaddi") || sport.contains("kho") -> {
                val endurance = lastTrial.enduranceScore ?: return "Beginner"
                when {
                    endurance >= 90 -> "🏅 National Level Ready"
                    endurance >= 75 -> "🥈 State Level Ready"
                    endurance >= 60 -> "🥉 District Level Ready"
                    else -> "⭐ School Champion"
                }
            }
            else -> {
                val score = lastTrial.enduranceScore ?: lastTrial.jumpMeters?.toInt() ?: 0
                when {
                    score >= 90 -> "🏅 National Level Ready"
                    score >= 70 -> "🥈 State Level Ready"
                    score >= 50 -> "🥉 District Level Ready"
                    else -> "⭐ School Champion"
                }
            }
        }
    }
}