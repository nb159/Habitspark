package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.views.habits.habitsScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var start by remember { mutableStateOf(false) }
            var user: UserModel? = null
            var habits: List<HabitModel>? = null
            lifecycleScope.launch {
                try {
                    val userId = UserPreferencesManager.getUserId(this@MainActivity).first()

                    if (userId != null) {
                        val userDocument = UserRepository.getUser(userId).await()
                        if (userDocument.exists()) {
                            user = userDocument.toObject(UserModel::class.java)?.copy(id = userDocument.id)

                            val habitDocuments = HabitRepository.getUserHabits(user!!.id).await()

                            habits = habitDocuments.documents.mapNotNull { document ->
                                document.toObject(HabitModel::class.java)?.copy(id = document.id)
                            }
                            start = true
                            Log.d("MainActivity", "User: $user")
                            Log.d("MainActivity", "Habits: $habits")
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
                // A surface container using the 'background' color from the theme

                Log.d("HabitsScreen", "habits screen")
                if (start) {
                    Log.d("HabitsScreen", "Starting habits screen")
                    habitsScreen(
                        user = user!!,
                        habits = habits ?: emptyList(),
                        onAddHabitClicked = {
                            // Handle habit click
                        },
                        onQuickAddEntryClicked = {
                            // Handle add habit click
                        }
                    )
                }
            }

        }
    }
}
