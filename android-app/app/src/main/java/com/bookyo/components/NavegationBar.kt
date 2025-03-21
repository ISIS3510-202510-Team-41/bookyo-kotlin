package com.bookyo.components

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.bookyo.ui.*
import androidx.compose.ui.res.painterResource
import com.bookyo.R
import androidx.compose.material3.MaterialTheme


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
        val iconResources = listOf(
            R.drawable.ic_home,
            R.drawable.ic_search,
            R.drawable.ic_plus,
            R.drawable.ic_bell,
            R.drawable.ic_person
        )

        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = iconResources[index]), contentDescription = item) },
                //label = { Text(item) },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                selectedContentColor = MaterialTheme.colorScheme.tertiary,
                unselectedContentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}