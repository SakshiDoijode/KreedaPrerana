// File: app/src/main/java/com/kreedaprerana/scout/data/TrialDao.kt
package com.kreedaprerana.scout.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TrialDao {

    @Query("SELECT * FROM trials WHERE athleteId = :athleteId ORDER BY recordedAt ASC")
    fun getTrialsForAthlete(athleteId: Long): LiveData<List<Trial>>

    @Query("SELECT * FROM trials WHERE athleteId = :athleteId ORDER BY recordedAt ASC")
    suspend fun getTrialsForAthleteSync(athleteId: Long): List<Trial>

    @Query("SELECT * FROM trials ORDER BY recordedAt DESC LIMIT 50")
    fun getRecentTrials(): LiveData<List<Trial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrial(trial: Trial): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrials(trials: List<Trial>)

    @Delete
    suspend fun deleteTrial(trial: Trial)

    @Query("SELECT COUNT(*) FROM trials")
    fun getTotalTrials(): LiveData<Int>
}


// File: app/src/main/java/com/kreedaprerana/scout/data/AthleteWithBest.kt
// (Create as separate file)
/*
package com.kreedaprerana.scout.data

data class AthleteWithBest(
    val id: Long,
    val name: String,
    val age: Int,
    val sport: String,
    val school: String,
    val createdAt: Long,
    val bestSprint: Double?,
    val bestJump: Double?,
    val bestEndurance: Int?
) {
    fun getBestDisplay(): String = when {
        bestSprint != null -> "%.2f sec".format(bestSprint)
        bestJump != null -> "%.2f m".format(bestJump)
        bestEndurance != null -> "$bestEndurance pts"
        else -> "—"
    }
}
*/
