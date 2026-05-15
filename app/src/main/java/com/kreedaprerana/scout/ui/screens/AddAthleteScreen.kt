// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/AddAthleteScreen.kt
package com.kreedaprerana.scout.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel

val SPORTS_LIST = listOf(
    "Athletics (Sprint)", "Long Jump", "High Jump", "Kabaddi",
    "Kho-Kho", "Football", "Cricket", "Basketball", "Volleyball",
    "Wrestling", "Boxing", "Swimming", "Badminton", "Table Tennis",
    "Javelin Throw", "Shot Put", "Discus Throw", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAthleteScreen(viewModel: AthleteViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var sportDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var sportError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Athlete", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Athlete Information",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = KreedaGreen
            )

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) {{ Text("Name is required") }} else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen)
            )

            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() }; ageError = false },
                label = { Text("Age *") },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = ageError,
                supportingText = if (ageError) {{ Text("Valid age (5–25) is required") }} else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen)
            )

            // Sport Dropdown
            ExposedDropdownMenuBox(
                expanded = sportDropdownExpanded,
                onExpandedChange = { sportDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = sport,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sport *") },
                    leadingIcon = { Icon(Icons.Default.Sports, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = sportError,
                    supportingText = if (sportError) {{ Text("Please select a sport") }} else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen)
                )
                ExposedDropdownMenu(
                    expanded = sportDropdownExpanded,
                    onDismissRequest = { sportDropdownExpanded = false }
                ) {
                    SPORTS_LIST.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                sport = s
                                sportDropdownExpanded = false
                                sportError = false
                            }
                        )
                    }
                }
            }

            // School
            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { Text("School Name") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KreedaGreen)
            )

            Spacer(Modifier.height(8.dp))

            // Submit
            Button(
                onClick = {
                    nameError = name.isBlank()
                    val ageInt = age.toIntOrNull()
                    ageError = ageInt == null || ageInt !in 5..25
                    sportError = sport.isBlank()

                    if (!nameError && !ageError && !sportError) {
                        viewModel.insertAthlete(
                            Athlete(
                                name = name.trim(),
                                age = ageInt!!,
                                sport = sport,
                                school = school.trim()
                            )
                        ) { newId ->
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreedaGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Athlete", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}