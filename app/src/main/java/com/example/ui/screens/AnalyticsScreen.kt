package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CreatorProfileEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SahityaCyanSecondary

@Composable
fun AnalyticsScreen(
    profile: CreatorProfileEntity?,
    onExportSql: ((String) -> Unit) -> Unit = {},
    onPushToSupabase: ((List<String>) -> Unit) -> Unit = {},
    onSaveCredentials: (String, String) -> Unit = { _, _ -> }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Coming Soon",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SahityaCyanSecondary,
                letterSpacing = 1.5.sp
            )
        )
    }
}
