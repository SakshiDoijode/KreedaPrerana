// File: app/src/main/java/com/kreedaprerana/scout/MainActivity.kt
package com.kreedaprerana.scout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.kreedaprerana.scout.ui.screens.*
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AthleteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KreedapreranaTheme {
                KreedaApp(viewModel = viewModel)
            }
        }
    }
}

// ─── Nav Routes ──────────────────────────────────────────────────────────────
sealed class Screen(val route: String, val label: String) {
    object Dashboard : Screen("dashboard", "Home")
    object Athletes : Screen("athletes", "Athletes")
    object Leaderboard : Screen("leaderboard", "Rankings")
    object BatchEntry : Screen("batch_entry", "Batch")
    object AddAthlete : Screen("add_athlete", "Add Athlete")
    object AthleteDetail : Screen("athlete/{athleteId}", "Profile") {
        fun createRoute(id: Long) = "athlete/$id"
    }
    object TrialLogger : Screen("trial/{athleteId}", "Log Trial") {
        fun createRoute(id: Long) = "trial/$id"
    }
    object TalentCurve : Screen("curve/{athleteId}", "Talent Curve") {
        fun createRoute(id: Long) = "curve/$id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KreedaApp(viewModel: AthleteViewModel) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        Triple(Screen.Dashboard, Icons.Default.Home, "Home"),
        Triple(Screen.Athletes, Icons.Default.People, "Athletes"),
        Triple(Screen.Leaderboard, Icons.Default.EmojiEvents, "Rankings"),
        Triple(Screen.BatchEntry, Icons.Default.GridView, "Batch"),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { (screen, icon, label) ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == screen.route
                    } == true
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(0.6f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(0.6f),
                            indicatorColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Athletes.route) {
                AthleteListScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.BatchEntry.route) {
                BatchEntryScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.AddAthlete.route) {
                AddAthleteScreen(viewModel = viewModel, navController = navController)
            }
            composable(
                Screen.AthleteDetail.route,
                arguments = listOf(navArgument("athleteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val athleteId = backStackEntry.arguments?.getLong("athleteId") ?: return@composable
                AthleteDetailScreen(
                    athleteId = athleteId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(
                Screen.TrialLogger.route,
                arguments = listOf(navArgument("athleteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val athleteId = backStackEntry.arguments?.getLong("athleteId") ?: return@composable
                TrialLoggerScreen(
                    athleteId = athleteId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(
                Screen.TalentCurve.route,
                arguments = listOf(navArgument("athleteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val athleteId = backStackEntry.arguments?.getLong("athleteId") ?: return@composable
                TalentCurveScreen(
                    athleteId = athleteId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}