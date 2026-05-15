// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/TrialLoggerScreen.kt
package com.kreedaprerana.scout.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kreedaprerana.scout.data.Trial
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialLoggerScreen(
    athleteId: Long,
    viewModel: AthleteViewModel,
    navController: NavController
) {
    val athlete by viewModel.getAthleteById(athleteId).observeAsState()

    // Stopwatch state
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<Long>()) }

    // Form state
    var manualSeconds by remember { mutableStateOf("") }
    var jumpMeters by remember { mutableStateOf("") }
    var enduranceScore by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var trialMode by remember { mutableIntStateOf(0) } // 0=sprint, 1=jump, 2=endurance
    var saved by remember { mutableStateOf(false) }

    // Stopwatch coroutine
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(10)
            elapsedMs += 10
        }
    }

    fun formatTime(ms: Long): String {
        val min = ms / 60000
        val sec = (ms % 60000) / 1000
        val centi = (ms % 1000) / 10
        return if (min > 0) "%02d:%02d.%02d".format(min, sec, centi)
        else "%02d.%02d".format(sec, centi)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Log Trial — ${athlete?.name ?: ""}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreedaOrange)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trial type tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("🏃 Sprint", "🦘 Jump", "💪 Endurance").forEachIndexed { i, label ->
                    FilterChip(
                        selected = trialMode == i,
                        onClick = { trialMode = i },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KreedaOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // ─── Stopwatch (for Sprint mode) ───────────────────────────────
            if (trialMode == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(listOf(KreedaDark, Color(0xFF2D3561)))
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                formatTime(elapsedMs),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("seconds", color = Color.White.copy(0.5f), fontSize = 14.sp)

                            Spacer(Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Start / Stop
                                Button(
                                    onClick = { isRunning = !isRunning },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRunning) Color(0xFFE53935) else KreedaGreenLight
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isRunning) "Stop" else "Start")
                                }

                                // Lap
                                OutlinedButton(
                                    onClick = { laps = laps + elapsedMs },
                                    enabled = isRunning || elapsedMs > 0
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Lap")
                                }

                                // Reset
                                OutlinedButton(
                                    onClick = {
                                        isRunning = false
                                        elapsedMs = 0L
                                        laps = emptyList()
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                }
                            }

                            // Auto-fill from stopwatch
                            if (!isRunning && elapsedMs > 0) {
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        manualSeconds = "%.2f".format(elapsedMs / 1000.0)
                                    },
                                    border = BorderStroke(1.dp, KreedaGold)
                                ) {
                                    Text(
                                        "Use ${formatTime(elapsedMs)} as result",
                                        color = KreedaGold
                                    )
                                }
                            }

                            // Laps
                            if (laps.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                laps.forEachIndexed { i, ms ->
                                    Text(
                                        "Lap ${i + 1}: ${formatTime(ms)}",
                                        color = Color.White.copy(0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = manualSeconds,
                    onValueChange = { manualSeconds = it },
                    label = { Text("Sprint Time (seconds) *") },
                    placeholder = { Text("e.g. 12.34") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaOrange),
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
                )
            }

            // ─── Jump Distance ─────────────────────────────────────────────
            if (trialMode == 1) {
                OutlinedTextField(
                    value = jumpMeters,
                    onValueChange = { jumpMeters = it },
                    label = { Text("Jump Distance (meters) *") },
                    placeholder = { Text("e.g. 4.56") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen),
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KreedaGreen.copy(alpha = 0.08f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("📏 Reference Benchmarks", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("≥ 6.5m → 🏅 National Level", fontSize = 12.sp, color = KreedaTextSecondary)
                        Text("≥ 5.5m → 🥈 State Level", fontSize = 12.sp, color = KreedaTextSecondary)
                        Text("≥ 4.5m → 🥉 District Level", fontSize = 12.sp, color = KreedaTextSecondary)
                    }
                }
            }

            // ─── Endurance Score ───────────────────────────────────────────
            if (trialMode == 2) {
                OutlinedTextField(
                    value = enduranceScore,
                    onValueChange = { enduranceScore = it.filter { c -> c.isDigit() } },
                    label = { Text("Endurance Score (0–100) *") },
                    placeholder = { Text("e.g. 78") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF7B1FA2)),
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) }
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7B1FA2).copy(0.08f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("📊 Score Reference", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("90–100 → 🏅 National Level", fontSize = 12.sp, color = KreedaTextSecondary)
                        Text("75–89  → 🥈 State Level", fontSize = 12.sp, color = KreedaTextSecondary)
                        Text("60–74  → 🥉 District Level", fontSize = 12.sp, color = KreedaTextSecondary)
                        Text("< 60   → ⭐ School Champion", fontSize = 12.sp, color = KreedaTextSecondary)
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Observations") },
                placeholder = { Text("Weather, conditions, remarks…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen)
            )

            // Save
            if (saved) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KreedaGreen.copy(0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KreedaGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Trial saved successfully!", color = KreedaGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Button(
                onClick = {
                    val trial = when (trialMode) {
                        0 -> {
                            val secs = manualSeconds.toDoubleOrNull() ?: return@Button
                            Trial(athleteId = athleteId, sprintSeconds = secs, notes = notes.trim())
                        }
                        1 -> {
                            val dist = jumpMeters.toDoubleOrNull() ?: return@Button
                            Trial(athleteId = athleteId, jumpMeters = dist, notes = notes.trim())
                        }
                        2 -> {
                            val score = enduranceScore.toIntOrNull() ?: return@Button
                            if (score !in 0..100) return@Button
                            Trial(athleteId = athleteId, enduranceScore = score, notes = notes.trim())
                        }
                        else -> return@Button
                    }
                    viewModel.insertTrial(trial) {
                        saved = true
                        manualSeconds = ""
                        jumpMeters = ""
                        enduranceScore = ""
                        notes = ""
                        elapsedMs = 0L
                        laps = emptyList()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreedaOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Trial", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
