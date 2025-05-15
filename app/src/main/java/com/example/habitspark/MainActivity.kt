package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.views.habits.habitDetailsScreen
import com.example.habitspark.ui.views.habits.habitsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var user by remember { mutableStateOf<UserModel?>(null) }
            lifecycleScope.launch {
                try {
                    val userId = "TMMFEXhbgy5hYFIMtASi"
//                    val userId = UserPreferencesManager.getUserId(this@MainActivity).first()

                    if (userId != null) {
                        Log.d("MainActivity", "userId: ${userId}")
                        val userDocument = UserRepository.getUser(userId).await()
                        Log.d("MainActivity", "userDocument: ${userDocument}")
                        if (userDocument.exists()) {
                            user = userDocument.toObject(UserModel::class.java)?.copy(id = userDocument.id)
                        } else {
                            // User document not found (maybe deleted)
                            startActivity(Intent(this@MainActivity, QuestionnaireActivity::class.java))
                        }
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
                            Log.d("HabitsScreen", user.toString())

                            habitsScreen(
                                user = user!!,
                                onHabitClick = { habitId: String ->
                                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                                }
                            )
                        }

                        composable(Screen.HabitDetail.route) { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
                            habitDetailsScreen(habitId = habitId)
                        }
                    }
                }

                Log.d("MainActivity", "habits screen")
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
    object HabitDetail : Screen("habitDetail/{habitId}") {
        fun createRoute(habitId: String) = "habitDetail/$habitId"
    }

}

