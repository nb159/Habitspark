package com.example.habitspark

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.habitspark.ui.views.onboarding.playerTypeQuestionnaireScreen


class QuestionnaireActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Navigation setup for moving between questionnaire and main app
            playerTypeQuestionnaireScreen()
        }
    }
}



