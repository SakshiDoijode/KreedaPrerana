// File: app/src/main/java/com/kreedaprerana/scout/viewmodel/AthleteViewModel.kt
package com.kreedaprerana.scout.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kreedaprerana.scout.data.*
import com.kreedaprerana.scout.repository.AthleteRepository
import kotlinx.coroutines.launch

class AthleteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AthleteRepository
    val allAthletes: LiveData<List<Athlete>>
    val totalAthletes: LiveData<Int>
    val totalTrials: LiveData<Int>
    val allSports: LiveData<List<String>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AthleteRepository(db)
        allAthletes = repository.allAthletes
        totalAthletes = repository.totalAthletes
        totalTrials = repository.totalTrials
        allSports = repository.allSports
    }

    fun getAthleteById(id: Long): LiveData<Athlete> = repository.getAthleteById(id)

    fun getTrialsForAthlete(athleteId: Long): LiveData<List<Trial>> =
        repository.getTrialsForAthlete(athleteId)

    fun insertAthlete(athlete: Athlete, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertAthlete(athlete)
            onResult(id)
        }
    }

    fun insertAthletes(athletes: List<Athlete>, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertAthletes(athletes)
            onResult()
        }
    }

    fun updateAthlete(athlete: Athlete) {
        viewModelScope.launch { repository.updateAthlete(athlete) }
    }

    fun deleteAthlete(athlete: Athlete) {
        viewModelScope.launch { repository.deleteAthlete(athlete) }
    }

    fun insertTrial(trial: Trial, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertTrial(trial)
            onResult(id)
        }
    }

    fun insertTrials(trials: List<Trial>, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertTrials(trials)
            onResult()
        }
    }

    fun deleteTrial(trial: Trial) {
        viewModelScope.launch { repository.deleteTrial(trial) }
    }

    fun getRecentTrials(): LiveData<List<Trial>> = repository.getRecentTrials()

    // Leaderboard state
    private val _leaderboard = MutableLiveData<List<Pair<Athlete, Trial?>>>()
    val leaderboard: LiveData<List<Pair<Athlete, Trial?>>> = _leaderboard

    fun loadLeaderboard(sport: String = "All") {
        viewModelScope.launch {
            val data = repository.getLeaderboard(sport)
            _leaderboard.postValue(data)
        }
    }

    // Trials for all athletes (for leaderboard with best scores)
    private val _athleteTrialsMap = MutableLiveData<Map<Long, List<Trial>>>()
    val athleteTrialsMap: LiveData<Map<Long, List<Trial>>> = _athleteTrialsMap

    fun loadAllTrials(athletes: List<Athlete>) {
        viewModelScope.launch {
            val map = mutableMapOf<Long, List<Trial>>()
            athletes.forEach { athlete ->
                map[athlete.id] = repository.getTrialsForAthleteSync(athlete.id)
            }
            _athleteTrialsMap.postValue(map)
        }
    }
}


// File: app/src/main/java/com/kreedaprerana/scout/viewmodel/AthleteViewModelFactory.kt
// Not needed since we use AndroidViewModel — just use:
// val viewModel: AthleteViewModel by viewModels()
// in any composable's host Activity or via:
// val viewModel: AthleteViewModel = viewModel()