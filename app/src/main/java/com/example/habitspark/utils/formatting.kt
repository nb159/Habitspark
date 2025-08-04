package com.example.habitspark.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.habitspark.ui.theme.PrimaryText


@Composable
fun textIconValue(
    text: String? = null,
    @DrawableRes iconRes: Int? = null,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    size: Dp = 14.dp,
    value: String,
    valueColor: Color = PrimaryText
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        text?.let {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText
            )
        }
        iconRes?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}