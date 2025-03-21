package com.bookyo.components


import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import com.bookyo.ui.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person


@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    BottomNavigation(
        backgroundColor = white,
        contentColor = black
    ) {
        val items = listOf("Home", "Search", "Add", "Notifications", "Profile")
        val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.AddCircle,
            Icons.Default.Notifications, Icons.Default.Person)

        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = icons[index], contentDescription = item) },
                //label = { Text(item) },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                selectedContentColor = orange,
                unselectedContentColor = blueGray
            )
        }
    }
}