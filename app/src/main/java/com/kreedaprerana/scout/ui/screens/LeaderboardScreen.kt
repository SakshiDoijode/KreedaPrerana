// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/LeaderboardScreen.kt
package com.kreedaprerana.scout.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: AthleteViewModel, navController: NavController) {
    val athletes by viewModel.allAthletes.observeAsState(emptyList())
    val sports by viewModel.allSports.observeAsState(emptyList())
    val athleteTrialsMap by viewModel.athleteTrialsMap.observeAsState(emptyMap())

    var selectedSport by remember { mutableStateOf("All") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Load trials whenever athletes change
    LaunchedEffect(athletes) {
        if (athletes.isNotEmpty()) viewModel.loadAllTrials(athletes)
    }

    // Build ranked list
    val rankedList = remember(athletes, athleteTrialsMap, selectedSport) {
        val filtered = if (selectedSport == "All") athletes
        else athletes.filter { it.sport.contains(selectedSport, ignoreCase = true) }

        filtered.map { athlete ->
            val trials = athleteTrialsMap[athlete.id] ?: emptyList()
            val bestTrial = trials.maxByOrNull { t ->
                when {
                    t.sprintSeconds != null -> -t.sprintSeconds
                    t.jumpMeters != null -> t.jumpMeters
                    t.enduranceScore != null -> t.enduranceScore.toDouble()
                    else -> -Double.MAX_VALUE
                }
            }
            Pair(athlete, bestTrial)
        }.filter { it.second != null }
            .sortedWith(compareBy { (_, trial) ->
                when {
                    trial?.sprintSeconds != null -> trial.sprintSeconds
                    trial?.jumpMeters != null -> -(trial.jumpMeters)
                    trial?.enduranceScore != null -> -(trial.enduranceScore.toDouble())
                    else -> Double.MAX_VALUE
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("🏆 Leaderboard", color = Color.White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreedaGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Sport filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter by Sport:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedSport,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Sports") },
                            onClick = { selectedSport = "All"; dropdownExpanded = false }
                        )
                        sports.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { selectedSport = s; dropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Top 3 podium
            if (rankedList.size >= 3) {
                PodiumRow(rankedList = rankedList.take(3))
                Spacer(Modifier.height(8.dp))
            }

            // Full list
            if (rankedList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏅", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No data available yet",
                            color = KreedaTextSecondary
                        )
                        Text(
                            "Log trials to generate rankings",
                            color = KreedaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(rankedList) { index, (athlete, trial) ->
                        LeaderboardRow(
                            rank = index + 1,
                            athlete = athlete,
                            trial = trial,
                            onClick = {
                                navController.navigate("athlete_detail")
                            }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun PodiumRow(rankedList: List<Pair<Athlete, Trial?>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place
        if (rankedList.size > 1) {
            PodiumCard(
                rank = 2,
                athlete = rankedList[1].first,
                trial = rankedList[1].second,
                height = 80.dp,
                color = Color(0xFFB0BEC5),
                emoji = "🥈"
            )
        }
        // 1st place
        PodiumCard(
            rank = 1,
            athlete = rankedList[0].first,
            trial = rankedList[0].second,
            height = 110.dp,
            color = KreedaGold,
            emoji = "🥇"
        )
        // 3rd place
        if (rankedList.size > 2) {
            PodiumCard(
                rank = 3,
                athlete = rankedList[2].first,
                trial = rankedList[2].second,
                height = 65.dp,
                color = Color(0xFFCD7F32),
                emoji = "🥉"
            )
        }
    }
}

@Composable
fun PodiumCard(
    rank: Int,
    athlete: Athlete,
    trial: Trial?,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            athlete.name.split(" ").first(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
        Text(
            trial?.getDisplayValue() ?: "—",
            fontSize = 11.sp,
            color = KreedaTextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text("#$rank", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
    }
}

@Composable
fun LeaderboardRow(
    rank: Int,
    athlete: Athlete,
    trial: Trial?,
    onClick: () -> Unit
) {
    val rankColor = when (rank) {
        1 -> KreedaGold
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFCD7F32)
        else -> KreedaTextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (rank <= 3) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(rankColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$rank",
                    fontWeight = FontWeight.ExtraBold,
                    color = rankColor,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KreedaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    athlete.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(athlete.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "${athlete.sport} • Age ${athlete.age}",
                    fontSize = 12.sp,
                    color = KreedaTextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    trial?.getDisplayValue() ?: "—",
                    fontWeight = FontWeight.ExtraBold,
                    color = KreedaGreen,
                    fontSize = 16.sp
                )
                Text(
                    athlete.getBadge(if (trial != null) listOf(trial) else emptyList())
                        .substringAfter(" ").take(12),
                    fontSize = 10.sp,
                    color = KreedaTextSecondary
                )
            }
        }
    }
}