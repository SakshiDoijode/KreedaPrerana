// File: app/src/main/java/com/kreedaprerana/scout/data/AthleteDao.kt
package com.kreedaprerana.scout.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AthleteDao {

    @Query("SELECT * FROM athletes ORDER BY name ASC")
    fun getAllAthletes(): LiveData<List<Athlete>>

    @Query("SELECT * FROM athletes WHERE id = :id")
    fun getAthleteById(id: Long): LiveData<Athlete>

    @Query("SELECT * FROM athletes WHERE id = :id")
    suspend fun getAthleteByIdSync(id: Long): Athlete?

    @Query("SELECT * FROM athletes WHERE sport LIKE '%' || :sport || '%'")
    fun getAthletesBySport(sport: String): LiveData<List<Athlete>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthlete(athlete: Athlete): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthletes(athletes: List<Athlete>)

    @Update
    suspend fun updateAthlete(athlete: Athlete)

    @Delete
    suspend fun deleteAthlete(athlete: Athlete)

    @Query("SELECT COUNT(*) FROM athletes")
    fun getTotalAthletes(): LiveData<Int>

    @Query("SELECT DISTINCT sport FROM athletes ORDER BY sport ASC")
    fun getAllSports(): LiveData<List<String>>
}


// File: app/src/main/java/com/kreedaprerana/scout/data/TrialDao.kt
// (Add in a separate file named TrialDao.kt)
// --- PASTE BELOW INTO TrialDao.kt ---
/*
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

    @Query("""
        SELECT a.*,
               MIN(t.sprintSeconds) as bestSprint,
               MAX(t.jumpMeters) as bestJump,
               MAX(t.enduranceScore) as bestEndurance
        FROM athletes a
        INNER JOIN trials t ON a.id = t.athleteId
        WHERE a.sport LIKE '%' || :sport || '%'
        GROUP BY a.id
        ORDER BY bestSprint ASC, bestJump DESC, bestEndurance DESC
    """)
    fun getLeaderboardBySport(sport: String): LiveData<List<AthleteWithBest>>
}

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
)
*/