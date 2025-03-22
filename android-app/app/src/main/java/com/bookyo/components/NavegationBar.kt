package com.bookyo.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookyo.R
import com.bookyo.home.HomeScreenActivity
import com.bookyo.notifications.NotificationsScreenActivity
import com.bookyo.publish.PublishScreenActivity

object Navigation {
    enum class Destination(val index: Int) {
        HOME(0),
        BROWSE(1),
        PUBLISH(2),
        NOTIFICATIONS(3),
        PROFILE(4)
    }

    fun navigateTo(context: Context, destination: Destination, clearBackStack: Boolean = false) {
        // Get the target activity class
        val targetClass = when(destination) {
            Destination.HOME -> HomeScreenActivity::class.java
            Destination.BROWSE -> return // Not implemented yet
            Destination.PUBLISH -> PublishScreenActivity::class.java
            Destination.NOTIFICATIONS -> NotificationsScreenActivity::class.java
            Destination.PROFILE -> return // Not implemented yet
        }

        // Check if we're already in the target activity class
        if (context.javaClass == targetClass) {
            return // Already in the right activity, do nothing
        }

        // Create intent
        val intent = Intent(context, targetClass).apply {
            // Clear back stack if needed
            if (clearBackStack) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Add flags to disable animations
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }

        // Start the activity without animation
        context.startActivity(intent)
        if (context is android.app.Activity) {
            context.overridePendingTransition(0, 0)
        }
    }

    fun getSelectedIndexForActivity(activityClass: Class<*>): Int {
        return when(activityClass) {
            HomeScreenActivity::class.java -> Destination.HOME.index
            PublishScreenActivity::class.java -> Destination.PUBLISH.index
            NotificationsScreenActivity::class.java -> Destination.NOTIFICATIONS.index
            else -> Destination.HOME.index
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreenIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Always start expanded
    val isExpanded = remember { mutableStateOf(true) }

    // Calculate the height based on screen width to maintain square buttons
    val screenWidth = LocalDensity.current.run {
        LocalConfiguration.current.screenWidthDp.dp
    }
    val expandedHeight = (screenWidth / 5) // 5 items instead of 4
    val collapsedHeight = 56.dp

    val height by animateDpAsState(
        targetValue = if (isExpanded.value) expandedHeight else collapsedHeight,
        animationSpec = tween(durationMillis = 300),
        label = "height"
    )

    val items = listOf("Home", "Search", "Publish", "Notifications", "Profile")
    val iconResources = listOf(
        R.drawable.ic_home,
        R.drawable.ic_search,
        R.drawable.ic_plus,
        R.drawable.ic_bell,
        R.drawable.ic_person
    )

    // Map indices to destinations
    val destinations = listOf(
        Navigation.Destination.HOME,
        Navigation.Destination.BROWSE,
        Navigation.Destination.PUBLISH,
        Navigation.Destination.NOTIFICATIONS,
        Navigation.Destination.PROFILE
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                Button(
                    onClick = {
                        // Only navigate if the selected item is different from the current one
                        if (currentScreenIndex != index) {
                            // Navigate to the home screen with clear back stack, other destinations without
                            val clearBackStack = destinations[index] == Navigation.Destination.HOME
                            Navigation.navigateTo(context, destinations[index], clearBackStack)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (currentScreenIndex == index)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconResources[index]),
                            contentDescription = item,
                            tint = if (currentScreenIndex == index)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}