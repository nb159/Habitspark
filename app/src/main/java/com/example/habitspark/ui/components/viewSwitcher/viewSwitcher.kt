package com.example.habitspark.ui.components.viewSwitcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habitspark.ui.theme.SecondaryText

@Composable
fun ViewSwitcher(
    selectedView: String,
    options: List<String>,
    onViewChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { label ->
            val isSelected = selectedView == label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onViewChange(label) }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else SecondaryText,
                    style = if (isSelected)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(24.dp)
                        .background(if (isSelected) Color.White else Color.Transparent)
                )
            }
        }
    }
}