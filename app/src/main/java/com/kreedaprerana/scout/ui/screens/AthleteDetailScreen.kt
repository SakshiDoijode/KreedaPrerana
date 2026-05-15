// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/AthleteDetailScreen.kt
package com.kreedaprerana.scout.ui.screens

import androidx.compose.foundation.*
import com.kreedaprerana.scout.Screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kreedaprerana.scout.data.Athlete
import com.kreedaprerana.scout.data.Trial
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteDetailScreen(
    athleteId: Long,
    viewModel: AthleteViewModel,
    navController: NavController
) {
    val athleteLd = viewModel.getAthleteById(athleteId)
    val athlete by athleteLd.observeAsState()
    val trials by viewModel.getTrialsForAthlete(athleteId).observeAsState(emptyList())

    val badge = athlete?.getBadge(trials) ?: "—"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        athlete?.name ?: "Athlete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("talent_curve")
                    }) {
                        Icon(Icons.Default.ShowChart, contentDescription = "Curve", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreedaGreen)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.TrialLogger.createRoute(athleteId)
                    )
                },
                containerColor = KreedaOrange,
                contentColor = Color.White
            )  {
                Icon(Icons.Default.Timer, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Log Trial")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(KreedaGreen, KreedaGreenLight)))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    athlete?.name?.take(1)?.uppercase() ?: "?",
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    athlete?.name ?: "—",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Text(
                                    "${athlete?.sport ?: ""} • Age ${athlete?.age ?: ""}",
                                    color = Color.White.copy(0.85f),
                                    fontSize = 14.sp
                                )
                                if (athlete?.school?.isNotBlank() == true) {
                                    Text(
                                        athlete?.school ?: "",
                                        color = Color.White.copy(0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    badge,
                                    color = KreedaGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        value = "${trials.size}",
                        label = "Trials",
                        color = KreedaGreen
                    )
                    val best = trials.maxByOrNull { it.getNumericValue() }
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        value = best?.getDisplayValue() ?: "—",
                        label = "Best",
                        color = KreedaOrange
                    )
                    val latest = trials.maxByOrNull { it.recordedAt }
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        value = if (latest != null) {
                            SimpleDateFormat("MMM dd", Locale.getDefault())
                                .format(Date(latest.recordedAt))
                        } else "—",
                        label = "Last Trial",
                        color = Color(0xFF7B1FA2)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Trials History
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Trial History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    TextButton(
                        onClick = {
                            navController.navigate("talent_curve")
                        }
                    ) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = KreedaGreen
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("View Curve", color = KreedaGreen)
                    }
                }
            }

            if (trials.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No trials logged yet", color = KreedaTextSecondary)
                            Text(
                                "Tap 'Log Trial' to record first performance",
                                color = KreedaTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(trials.sortedByDescending { it.recordedAt }) { trial ->
                    TrialHistoryRow(
                        trial = trial,
                        onDelete = { viewModel.deleteTrial(trial) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TrialHistoryRow(trial: Trial, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = KreedaGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    trial.getDisplayValue(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = KreedaGreen
                )
                Text(
                    sdf.format(Date(trial.recordedAt)),
                    fontSize = 12.sp,
                    color = KreedaTextSecondary
                )
                if (trial.notes.isNotBlank()) {
                    Text(trial.notes, fontSize = 12.sp, color = KreedaTextSecondary)
                }
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray)
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete Trial?") },
            text = { Text("Remove this trial entry?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDelete = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}
