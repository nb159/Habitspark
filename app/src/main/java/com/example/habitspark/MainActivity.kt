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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.views.achievements.achievementsScreen
import com.example.habitspark.ui.views.habits.habitDetailsScreen
import com.example.habitspark.ui.views.habits.habitsScreen
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

            HabitSparkTheme {
                if (user != null) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()

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
                                        Text(
                                            text = "HabitSpark",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontFamily = FontFamily.Cursive,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Italic
                                            ),
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                                        }
                                    }
                                )
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
                                    achievementsScreen()
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

            DrawerItem("Habits", Screen.Habits.route, currentRoute, onDestinationClicked)
            DrawerItem("Achievements", Screen.Achievements.route, currentRoute, onDestinationClicked)
            DrawerItem("Account", "account", currentRoute, onDestinationClicked)

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


sealed class Screen(val route: String) {
    object Habits : Screen("habits")
    object HabitDetail : Screen("habitDetail/{habitId}/{userId}") {
        fun createRoute(habitId: String, userId: String) = "habitDetail/$habitId/$userId"
    }
    object Achievements : Screen("achievementsScreen")

}

