// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/BatchEntryScreen.kt
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kreedaprerana.scout.data.Athlete
import com.kreedaprerana.scout.data.Trial
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel

data class BatchRow(
    val name: String = "",
    val age: String = "",
    val sport: String = "",
    val value: String = "",    // sprint/jump/endurance value
    val school: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEntryScreen(viewModel: AthleteViewModel, navController: NavController) {
    val MAX_ROWS = 30
    var rows by remember { mutableStateOf(List(5) { BatchRow() }) }
    var trialMode by remember { mutableIntStateOf(0) }  // 0=sprint, 1=jump, 2=endurance
    var schoolForAll by remember { mutableStateOf("") }
    var defaultSport by remember { mutableStateOf("") }
    var sportDropdownExpanded by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var savedCount by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }

    val valueLabel = when (trialMode) {
        0 -> "Sprint (sec)"
        1 -> "Jump (m)"
        else -> "Score (0-100)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Batch Entry (${rows.size}/${MAX_ROWS})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreedaOrange)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Config section
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KreedaOrange.copy(0.08f))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Class Setup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KreedaOrange
                        )

                        // Trial type
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("🏃 Sprint", "🦘 Jump", "💪 Score").forEachIndexed { i, label ->
                                FilterChip(
                                    selected = trialMode == i,
                                    onClick = { trialMode = i },
                                    label = { Text(label, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KreedaOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // School for all
                        OutlinedTextField(
                            value = schoolForAll,
                            onValueChange = { schoolForAll = it },
                            label = { Text("School (applies to all)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaOrange)
                        )

                        // Default sport
                        ExposedDropdownMenuBox(
                            expanded = sportDropdownExpanded,
                            onExpandedChange = { sportDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = defaultSport,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sport (applies to all)") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(sportDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KreedaOrange
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = sportDropdownExpanded,
                                onDismissRequest = { sportDropdownExpanded = false }
                            ) {
                                SPORTS_LIST.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            defaultSport = s
                                            sportDropdownExpanded = false
                                            // Apply sport to all empty rows
                                            rows = rows.map { it.copy(sport = s) }
                                        }
                                    )
                                }
                            }
                        }

                        // Apply school to all
                        if (schoolForAll.isNotBlank()) {
                            OutlinedButton(
                                onClick = { rows = rows.map { it.copy(school = schoolForAll) } }
                            ) {
                                Text("Apply School to All Rows")
                            }
                        }
                    }
                }
            }

            // Column headers
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("#", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KreedaTextSecondary)
                    Text("Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KreedaTextSecondary)
                    Text("Age", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KreedaTextSecondary)
                    Text(valueLabel, modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KreedaTextSecondary)
                }
                Divider()
            }

            // Student rows
            itemsIndexed(rows) { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}",
                        modifier = Modifier.width(28.dp),
                        color = KreedaTextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = row.name,
                        onValueChange = { v -> rows = rows.toMutableList().also { it[index] = row.copy(name = v) } },
                        placeholder = { Text("Name", fontSize = 11.sp) },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KreedaOrange,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    OutlinedTextField(
                        value = row.age,
                        onValueChange = { v ->
                            rows = rows.toMutableList().also {
                                it[index] = row.copy(age = v.filter { c -> c.isDigit() })
                            }
                        },
                        placeholder = { Text("Age", fontSize = 11.sp) },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KreedaOrange,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    OutlinedTextField(
                        value = row.value,
                        onValueChange = { v ->
                            rows = rows.toMutableList().also { it[index] = row.copy(value = v) }
                        },
                        placeholder = { Text(valueLabel, fontSize = 10.sp) },
                        modifier = Modifier.weight(1.1f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KreedaOrange,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            // Add/Remove row controls
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (rows.size < MAX_ROWS) {
                                rows = rows + BatchRow(sport = defaultSport, school = schoolForAll)
                            }
                        },
                        enabled = rows.size < MAX_ROWS
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Row")
                    }
                    OutlinedButton(
                        onClick = { if (rows.size > 1) rows = rows.dropLast(1) },
                        enabled = rows.size > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Remove Last")
                    }
                }
            }

            // Success
            if (saved) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = KreedaGreen.copy(0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KreedaGreen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "✅ $savedCount athlete records saved!",
                                color = KreedaGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Save all
            item {
                Button(
                    onClick = {
                        isSaving = true
                        val validRows = rows.filter { r ->
                            r.name.isNotBlank() && r.age.toIntOrNull() != null
                        }
                        if (validRows.isEmpty()) { isSaving = false; return@Button }

                        val athletes = validRows.map { r ->
                            Athlete(
                                name = r.name.trim(),
                                age = r.age.toInt(),
                                sport = r.sport.ifBlank { defaultSport.ifBlank { "General" } },
                                school = r.school.ifBlank { schoolForAll }
                            )
                        }

                        viewModel.insertAthletes(athletes) {
                            // After inserting athletes, log trials for each
                            // (In real app you'd get IDs back; here we re-query)
                            savedCount = validRows.size
                            saved = true
                            isSaving = false
                            rows = List(5) { BatchRow(sport = defaultSport, school = schoolForAll) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KreedaOrange),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.SaveAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Save All Athletes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}