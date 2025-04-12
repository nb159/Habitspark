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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext


data class PlayerTypeResult(
    val primaryType: String,
    val secondaryType: String
)


@Composable
fun playerTypeQuestionnaireScreen(
    onBoadingStep: Int = 2,
    toalOnBoadingSteps: Int = 3,
    onNext: (PlayerTypeResult) -> Unit
) {
    val allQuestions = remember {
        questionData.entries.flatMap { (type, questions) ->
            questions.map { question -> QuestionItem(type, question) }
        }.shuffled()
    }

    var currentIndex by remember { mutableStateOf(0) }
    val answersByType = remember { mutableStateMapOf<String, MutableList<Int>>() }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Demographic Questions ($onBoadingStep/$toalOnBoadingSteps)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Questions ${currentIndex + 1}-${minOf(currentIndex + 3, allQuestions.size)} of ${allQuestions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 5.dp)
            )

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

        Button(
            onClick = {
                if (currentIndex + 3 < allQuestions.size) {
                    currentIndex += 3
                } else {
                    val typeAverages = answersByType.mapValues { entry ->
                        val scores = entry.value
                        if (scores.isNotEmpty()) scores.sum() / scores.size else 0
                    }

                    val sorted = typeAverages.entries.sortedByDescending { it.value }

                    val primary = sorted.getOrNull(0)?.key
                    val secondary = sorted.getOrNull(1)?.key

                    if (primary != null && secondary != null) {
                        onNext(PlayerTypeResult(primaryType = primary, secondaryType = secondary))
                    } else {
                        Toast.makeText(
                            context,
                            "Please complete the questionnaire to continue.",
                            Toast.LENGTH_SHORT
                        ).show()
                        currentIndex = 0
                        answersByType.clear()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Next")
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
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
                    .heightIn(min = 80.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.question,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
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
    val selectedColor = MaterialTheme.colorScheme.secondary
    val unselectedColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else unselectedColor
        ),
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp
        )
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