package com.example.habitspark.ui.views.survey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.ui.components.charts.spaceDivider
import com.example.habitspark.ui.events.StatsEvent
import com.example.habitspark.ui.events.StatsEventBus
import com.example.habitspark.ui.views.user.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun surveyScreen(
    userId: String
) {

    val userViewModel: UserViewModel = viewModel()
    val user by userViewModel.userListener.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.startUser(userId)
    }

    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 1. Show loading state
            user == null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading user information...")
                }
            }

            // 2. Survey completed
            user?.surveyCompleted == true -> {
                Text(
                    text = "Thank you for participating in this study.\nYour assistance is greatly appreciated!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 3. Survey not completed (your original UI)
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Disclaimer
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                                )
                            ) {
                                append("Disclaimer: ")
                            }
                            append(
                                "Once you redirect to the survey or copy the survey link and leave this page, " +
                                        "the app will consider the survey completed and will remove this screen indefinitely. Please click when you are ready to proceed."
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    spaceDivider(height = 24)

                    // Redirect button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Redirect to survey",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                uriHandler.openUri(
                                    "https://docs.google.com/forms/d/e/1FAIpQLSdM779Or5mM3X0JUkfdCePmUIs1Tfs22d3nfnUGskQW6FgvCg/viewform?usp=pp_url&entry.1233052653=${user?.id}"
                                )
                                coroutineScope.launch {
                                    userViewModel.updateUser(user!!.copy(surveyCompleted = true))
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("Redirect To Survey!")
                        }
                    }

                    spaceDivider(height = 24, divide = true, dividerFraction = 0.5f)

                    // Copy link button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Copy survey link if you wish to send it and access it from your laptop/PC",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                clipboard.setText(
                                    androidx.compose.ui.text.AnnotatedString(
                                        "https://docs.google.com/forms/d/e/1FAIpQLSdM779Or5mM3X0JUkfdCePmUIs1Tfs22d3nfnUGskQW6FgvCg/viewform?usp=pp_url&entry.1233052653=${user?.id}"
                                    )
                                )
                                coroutineScope.launch {
                                    StatsEventBus.emit(StatsEvent.TextCopied("Survey link copied to clipboard!"))
                                    userViewModel.updateUser(user!!.copy(surveyCompleted = true))
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("Copy Survey Link!")
                        }
                    }
                }
            }
        }
    }
}