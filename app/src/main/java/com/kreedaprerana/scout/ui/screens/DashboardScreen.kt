// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/DashboardScreen.kt
package com.kreedaprerana.scout.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import com.kreedaprerana.scout.Screen
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.kreedaprerana.scout.data.Trial
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AthleteViewModel, navController: NavController) {
    val athletes by viewModel.allAthletes.observeAsState(emptyList())
    val totalAthletes by viewModel.totalAthletes.observeAsState(0)
    val totalTrials by viewModel.totalTrials.observeAsState(0)
    val allSports by viewModel.allSports.observeAsState(emptyList())
    val recentTrials by viewModel.getRecentTrials().observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ─── Header Banner ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(KreedaGreen, KreedaGreenLight)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = "🏃 Kreeda-Prerana Scout",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Digital Talent Tracker for Rural Schools",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Khelo India 🇮🇳",
                    color = KreedaGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Stats Row ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                value = "$totalAthletes",
                label = "Athletes",
                color = KreedaGreen
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                value = "$totalTrials",
                label = "Trials Logged",
                color = KreedaOrange
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Sports,
                value = "${allSports.size}",
                label = "Sports",
                color = Color(0xFF7B1FA2)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ─── Quick Actions ──────────────────────────────────────────────────
        SectionHeader("Quick Actions")
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PersonAdd,
                label = "Add Athlete",
                color = KreedaGreen,
                onClick = { navController.navigate("add_athlete") }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.GridView,
                label = "Batch Entry",
                color = KreedaOrange,
                onClick = { navController.navigate("batch_entry") }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.EmojiEvents,
                label = "Leaderboard",
                color = KreedaGold,
                onClick = { navController.navigate("leaderboard") }
            )
        }

        Spacer(Modifier.height(20.dp))

        // ─── Recent Athletes ────────────────────────────────────────────────
        if (athletes.isNotEmpty()) {
            SectionHeader("Recent Athletes")
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(athletes.take(8)) { athlete ->
                    AthleteChip(
                        name = athlete.name,
                        sport = athlete.sport,
                        onClick = {
                            navController.navigate(Screen.AthleteDetail.createRoute(athlete.id))
                        }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ─── Recent Activity ────────────────────────────────────────────────
        if (recentTrials.isNotEmpty()) {
            SectionHeader("Recent Activity")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentTrials.take(5).forEach { trial ->
                    RecentTrialRow(trial = trial, athletes = athletes)
                }
            }
        }

        // ─── Empty State ────────────────────────────────────────────────────
        if (athletes.isEmpty()) {
            Spacer(Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏅", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "No athletes yet!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = KreedaGreen
                )
                Text(
                    "Add your first athlete to start tracking.",
                    color = KreedaTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("add_athlete") },
                    colors = ButtonDefaults.buttonColors(containerColor = KreedaGreen)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add First Athlete")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─── Reusable Components ─────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = color)
            Text(label, fontSize = 11.sp, color = KreedaTextSecondary)
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AthleteChip(name: String, sport: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KreedaGreen.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KreedaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                name.split(" ").first(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(sport, fontSize = 10.sp, color = KreedaTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun RecentTrialRow(trial: Trial, athletes: List<com.kreedaprerana.scout.data.Athlete>) {
    val athlete = athletes.find { it.id == trial.athleteId }
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateStr = sdf.format(Date(trial.recordedAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50))
                .background(KreedaOrange),
            contentAlignment = Alignment.Center
        ) {
            Text(
                athlete?.name?.take(1)?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                athlete?.name ?: "Unknown",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                "${athlete?.sport ?: ""} • $dateStr",
                fontSize = 12.sp,
                color = KreedaTextSecondary
            )
        }
        Text(
            trial.getDisplayValue(),
            fontWeight = FontWeight.Bold,
            color = KreedaGreen,
            fontSize = 14.sp
        )
    }
}
