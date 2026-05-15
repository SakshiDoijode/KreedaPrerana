// File: app/src/main/java/com/kreedaprerana/scout/repository/AthleteRepository.kt
package com.kreedaprerana.scout.repository

import androidx.lifecycle.LiveData
import com.kreedaprerana.scout.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AthleteRepository(private val db: AppDatabase) {

    private val athleteDao = db.athleteDao()
    private val trialDao = db.trialDao()

    // --- Athletes ---
    val allAthletes: LiveData<List<Athlete>> = athleteDao.getAllAthletes()
    val totalAthletes: LiveData<Int> = athleteDao.getTotalAthletes()
    val allSports: LiveData<List<String>> = athleteDao.getAllSports()

    fun getAthleteById(id: Long): LiveData<Athlete> = athleteDao.getAthleteById(id)

    suspend fun insertAthlete(athlete: Athlete): Long =
        withContext(Dispatchers.IO) { athleteDao.insertAthlete(athlete) }

    suspend fun insertAthletes(athletes: List<Athlete>) =
        withContext(Dispatchers.IO) { athleteDao.insertAthletes(athletes) }

    suspend fun updateAthlete(athlete: Athlete) =
        withContext(Dispatchers.IO) { athleteDao.updateAthlete(athlete) }

    suspend fun deleteAthlete(athlete: Athlete) =
        withContext(Dispatchers.IO) { athleteDao.deleteAthlete(athlete) }

    // --- Trials ---
    val totalTrials: LiveData<Int> = trialDao.getTotalTrials()

    fun getTrialsForAthlete(athleteId: Long): LiveData<List<Trial>> =
        trialDao.getTrialsForAthlete(athleteId)

    suspend fun getTrialsForAthleteSync(athleteId: Long): List<Trial> =
        withContext(Dispatchers.IO) { trialDao.getTrialsForAthleteSync(athleteId) }

    fun getRecentTrials(): LiveData<List<Trial>> = trialDao.getRecentTrials()

    suspend fun insertTrial(trial: Trial): Long =
        withContext(Dispatchers.IO) { trialDao.insertTrial(trial) }

    suspend fun insertTrials(trials: List<Trial>) =
        withContext(Dispatchers.IO) { trialDao.insertTrials(trials) }

    suspend fun deleteTrial(trial: Trial) =
        withContext(Dispatchers.IO) { trialDao.deleteTrial(trial) }

    // --- Leaderboard (computed in memory for flexibility) ---
    suspend fun getLeaderboard(sport: String): List<Pair<Athlete, Trial?>> =
        withContext(Dispatchers.IO) {
            val athletes = if (sport == "All") {
                // Get all athletes
                var list = emptyList<Athlete>()
                athleteDao.getAllAthletes().observeForever { list = it }
                list
            } else {
                var list = emptyList<Athlete>()
                athleteDao.getAthletesBySport(sport).observeForever { list = it }
                list
            }
            athletes.map { athlete ->
                val trials = trialDao.getTrialsForAthleteSync(athlete.id)
                val bestTrial = trials.maxByOrNull { trial ->
                    when {
                        trial.sprintSeconds != null -> -trial.sprintSeconds  // Lower is better
                        trial.jumpMeters != null -> trial.jumpMeters
                        trial.enduranceScore != null -> trial.enduranceScore.toDouble()
                        else -> 0.0
                    }
                }
                Pair(athlete, bestTrial)
            }.sortedWith(compareBy { pair ->
                val trial = pair.second ?: return@compareBy Double.MAX_VALUE
                when {
                    trial.sprintSeconds != null -> trial.sprintSeconds
                    trial.jumpMeters != null -> -trial.jumpMeters
                    trial.enduranceScore != null -> -trial.enduranceScore.toDouble()
                    else -> Double.MAX_VALUE
                }
            })
        }
}