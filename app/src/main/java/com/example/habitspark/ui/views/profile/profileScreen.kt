package com.example.habitspark.ui.views.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.ui.theme.ButtonBorder
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.habits.EntryViewModel
import com.example.habitspark.ui.views.habits.HabitViewModel
import com.example.habitspark.ui.views.user.UserViewModel


@Composable
fun profileScreen(
    userId: String
) {
    val userViewModel: UserViewModel = viewModel()
    val habitViewModel: HabitViewModel = viewModel()
    val entryViewModel: EntryViewModel = viewModel()

    val user by userViewModel.user

    LaunchedEffect(Unit) {
        userViewModel.getUserById(userId)
    }
    Log.d("ProfileScreen", "User: $user ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        user?.let {
            userHeader(user = it)
            Spacer(modifier = Modifier.height(16.dp))
            accountInformation(user = it)
        }
    }

}

@Composable
fun userHeader(
    user: UserModel
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(0.7f)

            ) {
                Text(
                    text = "Welcome Back",
                    color = SecondaryText,
                    style = MaterialTheme.typography.titleSmall.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
                )
                Text(
                    text = user.name,
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
//                userLevelAndProgressBar(user.xp)
                Text(
                    text = "${user.gender} • ${user.age} • ${user.country}",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }
}

@Composable
fun accountInformation(
    user: UserModel
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        profileType(
            primary = user.primaryType,
            secondary = user.secondaryType
        )
    }
}

@Composable
fun profileType(
    primary: String,
    secondary: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Profile Type",
                color = PrimaryText,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row {
                        Text(
                            text = "1: $primary",
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        InfoTooltip(content = "This is your dominant habit archetype based on behavior.")
                    }


                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2: $secondary",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun InfoTooltip(
    content: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    tooltipWidth: Dp = 220.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = ButtonBorder ,
            modifier = Modifier
                .size(iconSize)
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-50).dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .widthIn(max = tooltipWidth)
                .padding(8.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlainTooltipExample(
//    modifier: Modifier = Modifier,
//    plainTooltipText: String = "Add to favorites"
//) {
//    TooltipBox(
//        modifier = modifier,
//        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
//        tooltip = {
//            PlainTooltip { Text(plainTooltipText) }
//        },
//        state = rememberTooltipState()
//    ) {
//        IconButton(onClick = { /* Do something... */ }) {
//            Icon(
//                imageVector = Icons.Filled.Favorite,
//                contentDescription = "Add to favorites"
//            )
//        }
//    }
//}