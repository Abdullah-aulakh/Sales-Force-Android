package com.example.salesforce.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SalesTab(
    val label: String,
    val icon: ImageVector,
) {
    Home("Home", Icons.Rounded.Home),
    Dashboard("Dashboard", Icons.Rounded.GridView),
    Profile("Profile", Icons.Rounded.Person),
}

@Composable
fun SalesBottomNavigation(
    selectedTab: SalesTab,
    onTabSelected: (SalesTab) -> Unit,
    modifier: Modifier = Modifier,
    barHeight: Dp = 48.dp, // Touch target height for each tab item
    selectedItemWidth: Dp = 42.dp, // Width of the gradient selection pill
    selectedItemHeight: Dp = 42.dp, // Height of the gradient selection pill
    iconSize: Dp = 28.dp, // Size of the icon
    bottomMargin: Dp = 24.dp, // Bottom margin above navigation bar
    horizontalMargin: Dp = 24.dp, // Horizontal side margin
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f

    // Black background and white icons for bottom bar
    val barBackground = if (isDark) colors.surface else Color(0xFF161B22)
    val unselectedColor = Color.White.copy(alpha = 0.65f)

    val gradient = Brush.verticalGradient(
        listOf(lerp(colors.primary, Color.White, 0.25f), colors.primary),
    )

    val barShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalMargin, vertical = bottomMargin),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = barShape,
                    ambientColor = colors.scrim,
                    spotColor = colors.scrim,
                )
                .background(barBackground, barShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SalesTab.entries.forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    selectedBrush = gradient,
                    selectedIconColor = Color.White,
                    unselectedIconColor = unselectedColor,
                    itemSize = barHeight,
                    selectedItemWidth = selectedItemWidth,
                    selectedItemHeight = selectedItemHeight,
                    iconSize = iconSize,
                    onClick = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: SalesTab,
    selected: Boolean,
    selectedBrush: Brush,
    selectedIconColor: Color,
    unselectedIconColor: Color,
    itemSize: Dp,
    selectedItemWidth: Dp,
    selectedItemHeight: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) selectedIconColor else unselectedIconColor,
        label = "navIconColor",
    )

    val pillShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .size(itemSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(width = selectedItemWidth, height = selectedItemHeight)
                    .background(selectedBrush, pillShape),
            )
        }
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier.size(iconSize),
        )
    }
}
