// File: app/src/main/java/com/kreedaprerana/scout/data/Trial.kt
package com.kreedaprerana.scout.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trials",
    foreignKeys = [ForeignKey(
        entity = Athlete::class,
        parentColumns = ["id"],
        childColumns = ["athleteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("athleteId")]
)
data class Trial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val athleteId: Long,
    val sprintSeconds: Double? = null,      // Sprint time in seconds (e.g. 12.34)
    val jumpMeters: Double? = null,          // Jump distance in meters (e.g. 4.56)
    val enduranceScore: Int? = null,         // Score 0–100 for endurance/team sports
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
) {
    fun getDisplayValue(): String {
        return when {
            sprintSeconds != null -> "%.2f sec".format(sprintSeconds)
            jumpMeters != null -> "%.2f m".format(jumpMeters)
            enduranceScore != null -> "$enduranceScore pts"
            else -> "—"
        }
    }

    fun getNumericValue(): Double {
        return when {
            sprintSeconds != null -> sprintSeconds
            jumpMeters != null -> jumpMeters
            enduranceScore != null -> enduranceScore.toDouble()
            else -> 0.0
        }
    }
}