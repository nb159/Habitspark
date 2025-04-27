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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            lifecycleScope.launch {
                UserPreferencesManager.getUserId(this@MainActivity).collect { userId ->
                    if (userId != null) {
                        UserRepository.getUser(userId)
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val user = document.toObject(UserModel::class.java)
                                    Log.d("MainActivity", "User data: $user")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("MainActivity", "Error getting user: ", exception)
                                val intent = Intent(this@MainActivity, QuestionnaireActivity::class.java)
                                startActivity(intent)
                            }
                    } else {
                        Log.d("MainActivity", "User ID is null, navigating to QuestionnaireActivity")
                        val intent = Intent(this@MainActivity, QuestionnaireActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            HabitSparkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                }
            }

        }
    }
}
