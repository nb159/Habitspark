package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.domain.featureGate.Feature
import com.example.habitspark.domain.featureGate.FeatureGate
import com.example.habitspark.ui.events.HasMessage
import com.example.habitspark.ui.events.StatsEventBus
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.views.achievements.achievementsScreen
import com.example.habitspark.ui.views.habits.habitDetailsScreen
import com.example.habitspark.ui.views.habits.habitsScreen
import com.example.habitspark.ui.views.profile.profileScreen
import com.example.habitspark.ui.views.survey.surveyScreen
import com.example.habitspark.ui.views.user.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userViewModel = UserViewModel()
            val user by userViewModel.userListener.collectAsState()
            lifecycleScope.launch {
                try {
                    val userId = UserPreferencesManager.getUserId(this@MainActivity).first()

                    if (userId != null) {
                        userViewModel.startUser(userId)
                    } else {
                        Log.e("MainActivity", "Sending user to QuestionnaireActivity: No user ID found")

                        // No user ID saved (first launch probably)
                        startActivity(Intent(this@MainActivity, QuestionnaireActivity::class.java))
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Sending user to QuestionnaireActivity: Error fetching user", e)
                    startActivity(Intent(this@MainActivity, QuestionnaireActivity::class.java))
                }
            }
            HabitSparkTheme {
                if (user != null) {

                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val config = LocalConfiguration.current
                    val screenWidth = config.screenWidthDp.dp
                    // Phone: ~70% of screen; Tablets: cap at 360dp (Material-ish)
                    val drawerWidth = remember(screenWidth) {
                        minOf(screenWidth * 0.70f, 360.dp)
                    }
                    Log.d("MainActivity", "Drawer width: $drawerWidth")
                    val scope = rememberCoroutineScope()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()

                    val snackbarHostState  = remember { SnackbarHostState() }
                    LaunchedEffect(Unit) {
                        StatsEventBus.events.collect { event ->
                            if (event is HasMessage) {
                                snackbarHostState.showSnackbar(event.message)
                            }
                        }
                    }

                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = BackgroundColor,
                                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                                modifier = Modifier
                                    .requiredWidth(drawerWidth)
                                    .fillMaxHeight()
                            ) {
                                DrawerContent(
                                    onDestinationClicked = { route ->
                                        scope.launch {
                                            drawerState.close()
                                            navController.navigate(route)
                                        }
                                    },
                                    currentRoute = currentBackStackEntry?.destination?.route,
                                    user = user!!, // Ensure user is not null here
                                )
                            }
                        },
                        drawerState = drawerState,
                        scrimColor = Color.Black.copy(alpha = 0.6f),
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row {
                                            Text(
                                                text = "HabitSpark",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontFamily = FontFamily.Cursive,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Italic
                                                ),
                                                modifier = Modifier.alignBy(LastBaseline)
                                            )
                                            Text(
                                                text = " / ${getScreenLabel(currentBackStackEntry?.destination?.route)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SecondaryText,
                                                modifier = Modifier
                                                    .padding(start = 5.dp)
                                                    .alignBy(LastBaseline) // pushes this text to bottom-align with the baseline

                                            )
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                                        }
                                    }
                                )
                            },
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState) {
                                    customSnackbar(it)
                                }
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Habits.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.Habits.route) {
                                    habitsScreen(
                                        userId = user!!.id,
                                        onHabitClick = { habitId ->
                                            navController.navigate(Screen.HabitDetail.createRoute(habitId))
                                        }
                                    )
                                }
                                composable(Screen.HabitDetail.route) { backStackEntry ->
                                    val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
                                    habitDetailsScreen(habitId = habitId, user = user!!)
                                }
                                composable(Screen.Achievements.route) {
                                    achievementsScreen(
                                        userId = user!!.id,
                                    )
                                }
                                composable(Screen.Profile.route) {
                                    profileScreen(userId = user!!.id,)
                                }
                                composable(Screen.Survey.route) {
                                    surveyScreen(userId = user!!.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    onDestinationClicked: (String) -> Unit,
    currentRoute: String? = null,
    user: UserModel,
) {
    val unlockAtMs = (user.createdDate?.toDate()?.time ?: 0L) + TimeUnit.HOURS.toMillis(36)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.habitsparkicon),
                contentDescription = "HabitSpark logo",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "HabitSpark",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                color = PrimaryText
            )
        }
        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 75.dp)
        )

        DrawerItem("Profile", Screen.Profile.route, currentRoute, onDestinationClicked)
        DrawerItem("Habits", Screen.Habits.route, currentRoute, onDestinationClicked)
        FeatureGate(user, Feature.ACHIEVEMENTS) {
            DrawerItem("Achievements", Screen.Achievements.route, currentRoute, onDestinationClicked)
        }

        // 🔒 Survey: locked until duartionToOpenSurvey == 0
        if (!user.surveyCompleted) {
            DrawerItem(
                label = "Survey",
                route = Screen.Survey.route,
                currentRoute = currentRoute,
                onClick = onDestinationClicked,
                unlockAtMs = unlockAtMs
            )
        }
        Spacer(modifier = Modifier.weight(1f))

    }

}
@Composable
fun DrawerItem(
    label: String,
    route: String,
    currentRoute: String?,
    onClick: (String) -> Unit,
    unlockAtMs: Long = 0L
) {
    val isSelected = when {
        currentRoute == null -> false
        route == Screen.Habits.route && currentRoute.startsWith("habitDetail") -> true
        else -> currentRoute == route
    }
    val baseBackground = if (isSelected) PrimaryAccent.copy(alpha = 0.1f) else Color.Transparent

    // remaining time (ms) until unlock; derived from absolute unlockAtMs
    var remainingMs by remember(unlockAtMs) {
        mutableStateOf(((unlockAtMs - System.currentTimeMillis()).coerceAtLeast(0L)))
    }
    val isLocked = remainingMs > 0L

    LaunchedEffect(unlockAtMs) {
        if (unlockAtMs <= 0L) {
            remainingMs = 0L
            return@LaunchedEffect
        }
        while (true) {
            val rem = (unlockAtMs - System.currentTimeMillis()).coerceAtLeast(0L)
            remainingMs = rem
            if (rem == 0L) break
            // Cheap: minute ticks when far away, second ticks in the last hour
            val nextDelay = if (rem > 3_600_000L) 60_000L else 1_000L
            delay(nextDelay)
        }
    }


    val textColor = when {
        isLocked -> SecondaryText.copy(alpha = 0.7f)
        isSelected -> PrimaryAccent
        else -> PrimaryText
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(baseBackground, shape = RoundedCornerShape(25.dp))
            .clickable(enabled = !isLocked) { onClick(route) }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )

            if (isLocked) {
                Text(
                    text = formatRemaining(remainingMs),
                    color = SecondaryText,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

private fun formatRemaining(ms: Long): String {
    val totalSec = ((ms + 999) / 1000).toInt() // round up
    val days = totalSec / 86_400
    val hours = (totalSec % 86_400) / 3_600
    val minutes = (totalSec % 3_600) / 60
    val seconds = totalSec % 60

    return when {
        days > 0      -> "${days}d ${hours}h"
        hours > 0     -> "${hours}h ${minutes}m"
        minutes > 0   -> String.format("%02d:%02d", minutes, seconds)
        else          -> String.format("00:%02d", seconds)
    }
}

@Composable
fun customSnackbar(data: SnackbarData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp), // controls vertical position
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(50), // pill style
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

sealed class Screen(val route: String, val label: String = "") {
    object Habits : Screen("habits", "Habits")
    object HabitDetail : Screen("habitDetail/{habitId}", "Habits/Detail") {
        fun createRoute(habitId: String) = "habitDetail/$habitId"
    }
    object Achievements : Screen("achievements", "Achievements")
    object Profile: Screen("profile", "Profile")
    object Survey: Screen("survey", "Survey")
}
fun getScreenLabel(route: String?): String {
    return when {
        route?.startsWith("habitDetail") == true -> Screen.HabitDetail.label
        route == Screen.Habits.route -> Screen.Habits.label
        route == Screen.Achievements.route -> Screen.Achievements.label
        route == Screen.Profile.route -> Screen.Profile.label
        route == Screen.Survey.route -> Screen.Survey.label
        else -> "Home"
    }
}

