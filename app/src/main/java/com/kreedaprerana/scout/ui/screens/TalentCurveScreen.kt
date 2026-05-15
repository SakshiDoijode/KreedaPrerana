// File: app/src/main/java/com/kreedaprerana/scout/ui/screens/TalentCurveScreen.kt
package com.kreedaprerana.scout.ui.screens

import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kreedaprerana.scout.data.Trial
import com.kreedaprerana.scout.ui.theme.*
import com.kreedaprerana.scout.viewmodel.AthleteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalentCurveScreen(
    athleteId: Long,
    viewModel: AthleteViewModel,
    navController: NavController
) {
    val athlete by viewModel.getAthleteById(athleteId).observeAsState()
    val trials by viewModel.getTrialsForAthlete(athleteId).observeAsState(emptyList())

    val sortedTrials = trials.sortedBy { it.recordedAt }
    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

    // Determine metric type from first trial
    val metricLabel = when {
        sortedTrials.any { it.sprintSeconds != null } -> "Sprint Time (sec) — lower is better"
        sortedTrials.any { it.jumpMeters != null } -> "Jump Distance (m)"
        sortedTrials.any { it.enduranceScore != null } -> "Endurance Score"
        else -> "Performance"
    }

    val isSprintMode = sortedTrials.any { it.sprintSeconds != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Talent Curve — ${athlete?.name ?: ""}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
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
        ) {
            if (sortedTrials.size < 2) {
                // Not enough data
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📈", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Need at least 2 trials to show a curve",
                            color = KreedaTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Log more trials to track progress",
                            color = KreedaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(16.dp))

                // Metric label
                Text(
                    metricLabel,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = KreedaGreen
                )
                Spacer(Modifier.height(8.dp))

                // MPAndroidChart in Compose
                val labels = sortedTrials.map { sdf.format(Date(it.recordedAt)) }
                val values = sortedTrials.mapIndexed { i, t ->
                    Entry(
                        i.toFloat(),
                        when {
                            t.sprintSeconds != null -> t.sprintSeconds.toFloat()
                            t.jumpMeters != null -> t.jumpMeters.toFloat()
                            t.enduranceScore != null -> t.enduranceScore.toFloat()
                            else -> 0f
                        }
                    )
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 8.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            setPinchZoom(true)
                            setDrawGridBackground(false)
                            setBackgroundColor(AndroidColor.WHITE)

                            legend.isEnabled = true
                            legend.textColor = KreedaDark.toArgb()

                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                valueFormatter = IndexAxisValueFormatter(labels)
                                granularity = 1f
                                labelRotationAngle = -30f
                                textColor = KreedaDark.toArgb()
                                gridColor = AndroidColor.LTGRAY
                            }

                            axisLeft.apply {
                                textColor = KreedaDark.toArgb()
                                gridColor = AndroidColor.LTGRAY
                                if (isSprintMode) isInverted = true  // Lower sprint time = better
                            }
                            axisRight.isEnabled = false

                            animateX(800)
                        }
                    },
                    update = { chart ->
                        val dataSet = LineDataSet(values, "Performance Over Time").apply {
                            color = KreedaGreen.toArgb()
                            setCircleColor(KreedaOrange.toArgb())
                            lineWidth = 2.5f
                            circleRadius = 5f
                            setDrawValues(true)
                            valueTextColor = KreedaDark.toArgb()
                            valueTextSize = 10f
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            fillColor = KreedaGreen.copy(alpha = 0.3f).toArgb()
                            setDrawFilled(true)
                        }
                        chart.data = LineData(dataSet)
                        chart.invalidate()
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Stats cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val firstVal = sortedTrials.first().getNumericValue()
                    val lastVal = sortedTrials.last().getNumericValue()
                    val improvement = when {
                        isSprintMode -> firstVal - lastVal  // lower is better, positive = improved
                        else -> lastVal - firstVal          // higher is better
                    }
                    val improvePct = if (firstVal != 0.0) (improvement / firstVal * 100) else 0.0

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        value = "%.1f%%".format(improvePct),
                        label = "Improvement",
                        color = if (improvement >= 0) KreedaGreen else MaterialTheme.colorScheme.error
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        value = sortedTrials.maxByOrNull { it.getNumericValue() }
                            ?.getDisplayValue() ?: "—",
                        label = "Best Ever",
                        color = KreedaGold
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BarChart,
                        value = "${sortedTrials.size}",
                        label = "Total Trials",
                        color = KreedaOrange
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Badge earned
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KreedaGold.copy(alpha = 0.15f))
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏅", fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Current Badge", fontSize = 12.sp, color = KreedaTextSecondary)
                            Text(
                                athlete?.getBadge(trials) ?: "—",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = KreedaDark
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Trial data table
                Text(
                    "Trial History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        // Header
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                "#",
                                modifier = Modifier.width(32.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KreedaTextSecondary
                            )
                            Text(
                                "Date",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KreedaTextSecondary
                            )
                            Text(
                                "Value",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KreedaTextSecondary
                            )
                            Text(
                                "Δ Change",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KreedaTextSecondary
                            )
                        }
                        Divider(Modifier.padding(vertical = 6.dp))

                        sortedTrials.forEachIndexed { i, trial ->
                            val prev = if (i > 0) sortedTrials[i - 1] else null
                            val delta = if (prev != null) {
                                trial.getNumericValue() - prev.getNumericValue()
                            } else null
                            val deltaDisplay = delta?.let {
                                val sign = if (it >= 0) "+" else ""
                                val improved = if (isSprintMode) it < 0 else it > 0
                                "$sign%.2f".format(it) to improved
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${i + 1}",
                                    modifier = Modifier.width(32.dp),
                                    fontSize = 13.sp
                                )
                                Text(
                                    sdf.format(Date(trial.recordedAt)),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    trial.getDisplayValue(),
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = KreedaGreen
                                )
                                if (deltaDisplay != null) {
                                    Text(
                                        deltaDisplay.first,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        color = if (deltaDisplay.second) KreedaGreen
                                        else MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        "—",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        color = KreedaTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}