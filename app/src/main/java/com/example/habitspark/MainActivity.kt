package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.views.habits.habitDetailsScreen
import com.example.habitspark.ui.views.habits.habitsScreen
import com.example.habitspark.ui.views.user.UserViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userViewModel = UserViewModel()
            val user by userViewModel.user
            lifecycleScope.launch {
                try {
//                    val userId = "TMMFEXhbgy5hYFIMtASi"
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
                    NavHost(navController = navController, startDestination = Screen.Habits.route) {

                        composable(Screen.Habits.route) {
                            habitsScreen(
                                userId = user!!.id,
                                onHabitClick = { habitId: String ->
                                    navController.navigate(Screen.HabitDetail.createRoute(habitId, user!!.id))
                                }
                            )
                        }

                        composable(Screen.HabitDetail.route) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
                            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                            habitDetailsScreen(habitId = habitId, userId = userId)
                        }
                    }
                }

//
//                    Log.d("MainActivity", "habits screen: ${user}")
//                    habitsScreen(
//                        user = user!!,
//                    )
//                }
            }

        }
    }
}


sealed class Screen(val route: String) {
    object Habits : Screen("habits")
    object HabitDetail : Screen("habitDetail/{habitId}/{userId}") {
        fun createRoute(habitId: String, userId: String) = "habitDetail/$habitId/$userId"
    }

}

