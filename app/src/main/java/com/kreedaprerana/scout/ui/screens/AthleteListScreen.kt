// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/AthleteListScreen.kt
package com.kreedaprerana.scout.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.kreedaprerana.scout.data.Athlete
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteListScreen(viewModel: AthleteViewModel, navController: NavController) {
    val athletes by viewModel.allAthletes.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val filtered = athletes.filter {
        searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.sport.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Athletes", fontWeight = FontWeight.Bold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreedaGreen),
                actions = {
                    IconButton(onClick = { navController.navigate("add_athlete") }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search athletes or sport…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KreedaGreen,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏃", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (athletes.isEmpty()) "No athletes added yet" else "No results found",
                            color = KreedaTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { athlete ->
                        AthleteListCard(
                            athlete = athlete,
                            onClick = {
                                navController.navigate("athlete_detail")
                            },
                            onDelete = { viewModel.deleteAthlete(athlete) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun AthleteListCard(
    athlete: Athlete,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KreedaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    athlete.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(athlete.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${athlete.sport} • Age ${athlete.age}",
                    fontSize = 13.sp,
                    color = KreedaTextSecondary
                )
                if (athlete.school.isNotBlank()) {
                    Text(athlete.school, fontSize = 12.sp, color = KreedaTextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { showDelete = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = KreedaGreen
                )
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete Athlete?") },
            text = { Text("This will permanently delete ${athlete.name} and all their trials.") },
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