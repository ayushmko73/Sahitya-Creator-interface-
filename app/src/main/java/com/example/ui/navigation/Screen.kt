package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Auth : Screen("auth", "Sign In")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object StoryStudio : Screen("story_studio", "Story Studio", Icons.Default.AutoAwesome)
    object PipelineExecution : Screen("pipeline_execution", "AI Pipeline")
    object StoryDetail : Screen("story_detail", "Story Details")
    object Analytics : Screen("analytics", "Earnings", Icons.Default.Analytics)
}
