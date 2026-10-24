package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PipelineExecutionScreen
import com.example.ui.screens.StoryDetailScreen
import com.example.ui.screens.StoryStudioScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SahityaCyanSecondary
import com.example.ui.theme.SahityaCreatorTheme
import com.example.ui.theme.SahityaIndigoPrimary
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.SupabaseConfig.init(applicationContext)

        setContent {
            SahityaCreatorTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()
    val creatorProfile by viewModel.creatorProfile.collectAsState(initial = null)
    val stories by viewModel.allStories.collectAsState(initial = emptyList())
    val studioState by viewModel.studioState.collectAsState()
    val autoSaveStatus by viewModel.autoSaveStatus.collectAsState()
    val lastAutoSavedTime by viewModel.lastAutoSavedTime.collectAsState()
    val restorableDraft by viewModel.restorableDraft.collectAsState()
    val pipelineState by viewModel.pipelineState.collectAsState()
    val selectedStoryId by viewModel.selectedStoryId.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val bottomNavScreens = listOf(
        Screen.Dashboard,
        Screen.StoryStudio,
        Screen.Analytics
    )

    if (authState is AuthState.Unauthenticated) {
        AuthScreen(
            onLoginSuccess = { email, pass ->
                viewModel.login(email, pass)
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomNavScreens.map { it.route }) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar"),
                    containerColor = DarkSurface,
                    contentColor = Color.White
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon ?: Icons.Default.Dashboard,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            onClick = {
                                if (screen == Screen.StoryStudio && !isSelected) {
                                    viewModel.startNewStory()
                                }
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = false }
                                    launchSingleTop = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SahityaCyanSecondary,
                                selectedTextColor = SahityaCyanSecondary,
                                indicatorColor = SahityaIndigoPrimary.copy(alpha = 0.3f),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {
                // Dashboard / Home Hub
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        profile = creatorProfile,
                        stories = stories,
                        onCreateNewStory = {
                            viewModel.startNewStory()
                            navController.navigate(Screen.StoryStudio.route)
                        },
                        onSelectStory = { story ->
                            viewModel.selectStory(story.id)
                            navController.navigate(Screen.StoryDetail.route)
                        },
                        onEditStory = { story ->
                            viewModel.editExistingStory(story)
                            navController.navigate(Screen.StoryStudio.route)
                        }
                    )
                }

                // 3-Step Story Creation Studio
                composable(Screen.StoryStudio.route) {
                    StoryStudioScreen(
                        state = studioState,
                        categories = viewModel.categories,
                        autoSaveStatus = autoSaveStatus,
                        lastAutoSavedTime = lastAutoSavedTime,
                        restorableDraft = restorableDraft,
                        onRestoreDraft = { draft -> viewModel.restoreDraftFromDataStore(draft) },
                        onClearDraft = { viewModel.clearDataStoreDraft() },
                        onSetStep = { step -> viewModel.setWizardStep(step) },
                        onUpdatePage1 = { title, sub, desc, cat, mainThumb, banner ->
                            viewModel.updateStudioPage1(title, sub, desc, cat, mainThumb, banner)
                        },
                        onUpdateSubThumbnails = { urls -> viewModel.updateSubThumbnails(urls) },
                        onAddCharacter = { viewModel.addCharacter() },
                        onUpdateCharacter = { index, char -> viewModel.updateCharacter(index, char) },
                        onRemoveCharacter = { index -> viewModel.removeCharacter(index) },
                        onAddSeason = { viewModel.addSeason() },
                        onUpdateSeason = { index, season -> viewModel.updateSeason(index, season) },
                        onRemoveSeason = { index -> viewModel.removeSeason(index) },
                        onUpdateMasterTimeline = { timeline -> viewModel.updateMasterTimeline(timeline) },
                        onSaveDraft = {
                            viewModel.saveStoryDraft()
                            navController.navigate(Screen.Dashboard.route)
                        },
                        onRunPipeline = {
                            viewModel.saveAndRunPipeline {
                                navController.navigate(Screen.PipelineExecution.route)
                            }
                        }
                    )
                }

                // Dual-Layer Cost-Optimized AI Publishing Pipeline Monitor
                composable(Screen.PipelineExecution.route) {
                    PipelineExecutionScreen(
                        state = pipelineState,
                        onRetry = {
                            selectedStoryId?.let { id ->
                                viewModel.executeAiPipeline(id)
                            }
                        },
                        onPublishToNetwork = { onComplete ->
                            viewModel.publishSelectedStoryToNetwork { syncResult ->
                                onComplete(syncResult)
                            }
                        },
                        onViewStoryDetails = {
                            navController.navigate(Screen.StoryDetail.route)
                        },
                        onBackToDashboard = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Story Details
                composable(Screen.StoryDetail.route) {
                    val activeStory = stories.find { it.id == selectedStoryId }
                    val charsState by viewModel.repository.getCharactersForStory(selectedStoryId ?: "").collectAsState(initial = emptyList())
                    val seasonsState by viewModel.repository.getSeasonsForStory(selectedStoryId ?: "").collectAsState(initial = emptyList())
                    val logsState by viewModel.repository.getLogsForStory(selectedStoryId ?: "").collectAsState(initial = emptyList())

                    StoryDetailScreen(
                        story = activeStory,
                        characters = charsState,
                        seasons = seasonsState,
                        logs = logsState,
                        onBack = { navController.popBackStack() },
                        onEdit = {
                            activeStory?.let { viewModel.editExistingStory(it) }
                            navController.navigate(Screen.StoryStudio.route)
                        }
                    )
                }

                // Analytics View
                composable(Screen.Analytics.route) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    AnalyticsScreen(
                        profile = creatorProfile,
                        onExportSql = { callback -> viewModel.generateDatabaseSqlScript(callback) },
                        onPushToSupabase = { callback -> viewModel.syncAllStoriesToSupabase(callback) },
                        onSaveCredentials = { url, key -> com.example.util.SupabaseConfig.saveCredentials(context, url, key) }
                    )
                }
            }
        }
    }
}
