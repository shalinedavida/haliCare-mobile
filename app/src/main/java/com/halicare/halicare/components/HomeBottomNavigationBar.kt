package com.halicare.halicare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun HomeBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Column {
        Divider(color = Color(0xFF264A83), thickness = 1.dp)
        NavigationBar(
            modifier = Modifier
                .background(Color.White)
                .height(64.dp),
            containerColor = Color.White,
            tonalElevation = 4.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = if (selected) Color(0xFF264A83) else Color(0xFF374151),
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = if (selected) Color(0xFF264A83) else Color(0xFF374151),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = if (selected) Color(0xFFD6E4FF) else Color.Transparent,
                        selectedIconColor = Color(0xFF264A83),
                        selectedTextColor = Color(0xFF264A83),
                        unselectedIconColor = Color(0xFF374151),
                        unselectedTextColor = Color(0xFF374151)
                    ),
                    modifier = if (selected) Modifier.clip(RoundedCornerShape(8.dp)) else Modifier
                )
            }
        }
    }
}