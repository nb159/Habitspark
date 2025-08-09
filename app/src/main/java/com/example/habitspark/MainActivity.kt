package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.ui.events.StatsEvent
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
import com.example.habitspark.ui.views.user.UserViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userViewModel = UserViewModel()
            val user by userViewModel.user
            lifecycleScope.launch {
                try {
                    val userId = UserPreferencesManager.getUserId(this@MainActivity).first()

                    if (userId != null) {
                        userViewModel.getUserById(userId)
                    } else {
                        // No user ID saved (first launch probably)
                        startActivity(Intent(this@MainActivity, QuestionnaireActivity::class.java))
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error loading user or habits", e)
                    startActivity(Intent(this@MainActivity, QuestionnaireActivity::class.java))
                }
            }
//            data class SeedAchievement(
//                val id: String,
//                val title: String,
//                val description: String,
//                val type: String,
//                val goal: Int,
//                val rewardType: String,
//                val reward: Int,
//            )
//            val achievements = listOf(
//                SeedAchievement("habit_5", "Habit Collector", "Create 5 habits", "HABIT_COUNT", 5, "XP", 100),
//                SeedAchievement("habit_10", "Life Architect", "Create 10 habits", "HABIT_COUNT", 10, "XP", 200),
//
//                SeedAchievement("entry_5", "First Steps", "Log 5 habit entries", "ENTRY_COUNT", 5, "XP", 50),
//                SeedAchievement("entry_15", "Momentum", "Log 15 habit entries", "ENTRY_COUNT", 15, "XP", 120),
//                SeedAchievement("entry_30", "Consistency Master", "Log 30 habit entries", "ENTRY_COUNT", 30, "XP", 250),
//
//                SeedAchievement("streak_3", "3-Day Streak", "Maintain a 3-day habit streak", "STREAK", 3, "XP", 60),
//                SeedAchievement("streak_7", "One Full Week", "Maintain a 7-day habit streak", "STREAK", 7, "XP", 150),
//                SeedAchievement("streak_21", "Unbreakable", "Maintain a 21-day habit streak", "STREAK", 21, "XP", 400),
//
//                SeedAchievement("time_60", "One Hour In", "Spend 60 minutes on habits", "TIME_SPENT", 60, "XP", 40),
//                SeedAchievement("time_300", "5-Hour Focus", "Spend 300 minutes on habits", "TIME_SPENT", 300, "XP", 120),
//                SeedAchievement("time_1000", "Time Investor", "Spend 1000 minutes on habits", "TIME_SPENT", 1000, "XP", 250),
//
//                SeedAchievement("mood_10_positive", "Feeling Good", "Log 10 entries with a happy mood", "MOOD", 10, "XP", 80),
//                SeedAchievement("mood_25_positive", "Optimist", "Log 25 entries with a happy mood", "MOOD", 25, "XP", 180),
//
//                SeedAchievement("difficulty_10_high", "Challenge Accepted", "Complete 10 entries marked Hard or above", "DIFFICULTY", 10, "XP", 90),
//                SeedAchievement("difficulty_30_high", "Through the Fire", "Complete 30 entries marked Hard or above", "DIFFICULTY", 30, "XP", 200)
//            )
//            fun seedAchievementsToFirestore() {
//                val tempDb = Firebase.firestore.collection("achievements")
//
//                achievements.forEach { achievement ->
//                    tempDb.document(achievement.id).set(achievement)
//                        .addOnSuccessListener {
//                            Log.d("SeedAchievements", "Added: ${achievement.id}")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("SeedAchievements", "Error adding ${achievement.id}", e)
//                        }
//                }
//            }

            HabitSparkTheme {
                if (user != null) {
//                    seedAchievementsToFirestore()

                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()

                    val snackbarHostState  = remember { SnackbarHostState() }
                    LaunchedEffect(Unit) {
                        StatsEventBus.events.collect { event ->
                            if (event is StatsEvent.AchievementUnlocked) {
                                snackbarHostState.showSnackbar(event.message)
                            }
                        }
                    }

                    ModalNavigationDrawer(
                        drawerContent = {
                            DrawerContent(
                                onDestinationClicked = { route ->
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate(route)
                                    }},
                                currentRoute = currentBackStackEntry?.destination?.route
                            )
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
                                            navController.navigate(Screen.HabitDetail.createRoute(habitId, user!!.id))
                                        }
                                    )
                                }
                                composable(Screen.HabitDetail.route) { backStackEntry ->
                                    val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
                                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                                    habitDetailsScreen(habitId = habitId, userId = userId)
                                }
                                composable(Screen.Achievements.route) {
                                    achievementsScreen(
                                        userId = user!!.id,
                                    )
                                }
                                composable(Screen.Profile.route) {
                                    profileScreen(userId = user!!.id,)
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
    currentRoute: String? = null
) {
    val drawerWidth = LocalConfiguration.current.screenWidthDp.dp * 0.70f

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth),
        color = BackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "HabitSpark",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 15.dp)
            )
            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 75.dp)
            )

            DrawerItem("Profile", Screen.Profile.route, currentRoute, onDestinationClicked)
            DrawerItem("Habits", Screen.Habits.route, currentRoute, onDestinationClicked)
            DrawerItem("Achievements", Screen.Achievements.route, currentRoute, onDestinationClicked)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "v1.0.0",
                color = SecondaryText,
                style = MaterialTheme.typography.bodySmall
            )
        }
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



@Composable
fun DrawerItem(
    label: String,
    route: String,
    currentRoute: String?,
    onClick: (String) -> Unit
) {
    val isSelected = when {
        currentRoute == null -> false
        route == Screen.Habits.route && currentRoute.startsWith("habitDetail") -> true
        else -> currentRoute == route
    }
    val background = if (isSelected) PrimaryAccent.copy(alpha = 0.1f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape = RoundedCornerShape(25.dp))
            .clickable { onClick(route) }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) PrimaryAccent else PrimaryText,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}


sealed class Screen(val route: String, val label: String = "") {
    object Habits : Screen("habits", "Habits")
    object HabitDetail : Screen("habitDetail/{habitId}/{userId}", "Habits/Detail") {
        fun createRoute(habitId: String, userId: String) = "habitDetail/$habitId/$userId"
    }
    object Achievements : Screen("achievements", "Achievements")
    object Profile: Screen("profile", "Profile")
}
fun getScreenLabel(route: String?): String {
    return when {
        route?.startsWith("habitDetail") == true -> Screen.HabitDetail.label
        route == Screen.Habits.route -> Screen.Habits.label
        route == Screen.Achievements.route -> Screen.Achievements.label
        route == Screen.Profile.route -> Screen.Profile.label
        else -> "Home"
    }
}

