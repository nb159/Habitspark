package com.example.habitspark.ui.views.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log


@Composable
fun playerTypeQuestionnaireScreen() {
    val allQuestions = remember {
        questionData.entries.flatMap { (type, questions) ->
            questions.map { question -> QuestionItem(type, question) }
        }.shuffled()
    }

    var currentIndex by remember { mutableStateOf(0) }

    val answersByType = remember {
        mutableStateMapOf<String, MutableList<Int>>()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Player Type Questionnaire",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentQuestions = allQuestions.subList(
                    currentIndex,
                    minOf(currentIndex + 3, allQuestions.size)
                )

                currentQuestions.forEach { item ->
                    QuestionCard(
                        item = item,
                        onAnswerSelected = { type, score ->
                            val list = answersByType.getOrPut(type) { mutableListOf() }
                            list.add(score)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (currentIndex + 3 < allQuestions.size) {
                        currentIndex += 3
                    }else{
                        Log.d("UserType", "Answers: $answersByType")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Next")
            }
        }

    }
}



@Composable
fun QuestionCard(
    item: QuestionItem,
    onAnswerSelected: (String, Int) -> Unit
) {
    var selectedOption by remember(item) { mutableStateOf<Int?>(null) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Consistent card height
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Flexible space
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.question,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the buttons to the bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp), // Optional bottom margin
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..7).forEach { num ->
                    circleButton(
                        number = num,
                        isSelected = selectedOption == num,
                        onClick = {
                            selectedOption = num
                            onAnswerSelected(item.type, num)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun circleButton(number: Int, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.LightGray,
        ),
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = number.toString(), color = Color.White, fontSize = 16.sp)
    }
}




// Data class for questions
data class QuestionItem(val type: String, val question: String)

val questionData = mapOf(
    "Philanthropist" to listOf(
        "It makes me happy if I am able to help others.",
        "I like helping others to orient themselves in new situations.",
        "I like sharing my knowledge.",
        "The wellbeing of others is important to me."
    ),
    "Socialiser" to listOf(
        "Interacting with others is important to me.",
        "I like being part of a team.",
        "It is important to me to feel like I am part of a community.",
        "I enjoy group activities."
    ),
    "Free Spirit" to listOf(
        "It is important to me to follow my own path.",
        "I often let my curiosity guide me.",
        "I like to try new things.",
        "Being independent is important to me."
    ),
    "Achiever" to listOf(
        "I like defeating obstacles.",
        "It is important to me to always carry out my tasks completely.",
        "It is difficult for me to let go of a problem before I have found a solution.",
        "I like mastering difficult tasks."
    ),
    "Disruptor" to listOf(
        "I like to provoke.",
        "I like to question the status quo.",
        "I see myself as a rebel.",
        "I dislike following rules."
    ),
    "Player" to listOf(
        "I like competitions where a prize can be won.",
        "Rewards are a great way to motivate me.",
        "Return of investment is important to me.",
        "If the reward is sufficient I will put in the effort."
    )
)