package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DraftDataStoreManager
import com.example.data.local.DraftSnapshot
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.CreatorProfileEntity
import com.example.data.local.entities.SeasonEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.pipeline.PipelineProgress
import com.example.data.pipeline.PipelineResult
import com.example.data.repository.SahityaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val profile: CreatorProfileEntity) : AuthState()
}

// Wizard state for multi-step story creation
data class StoryStudioState(
    val currentStep: Int = 1, // 1, 2, 3
    val storyId: String = UUID.randomUUID().toString(),
    // Page 1
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val selectedCategory: String = "Anime",
    val mainThumbnail: String = "",
    val bannerImage: String = "",
    val subThumbnails: List<String> = emptyList(), // Up to 5 sub-thumbnails
    val videoUri: String = "",                     // 1 Video URI/URL
    // Page 2
    val characters: List<CharacterEntity> = emptyList(),
    // Page 3
    val masterTimeline: String = "",
    val seasons: List<SeasonEntity> = emptyList()
)

data class PipelineExecutionState(
    val isRunning: Boolean = false,
    val progressPercent: Float = 0f,
    val currentStageMessage: String = "",
    val logs: List<String> = emptyList(),
    val result: PipelineResult? = null,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val draftDataStore = DraftDataStoreManager(application)

    val repository = SahityaRepository(
        creatorDao = db.creatorDao(),
        storyDao = db.storyDao(),
        characterDao = db.characterDao(),
        seasonDao = db.seasonDao(),
        pipelineLogDao = db.pipelineLogDao()
    )

    val characterRepository = repository.characterRepository

    val restorableDraft: StateFlow<DraftSnapshot?> = draftDataStore.draftFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _autoSaveStatus = MutableStateFlow<String>("DataStore Ready")
    val autoSaveStatus: StateFlow<String> = _autoSaveStatus.asStateFlow()

    private val _lastAutoSavedTime = MutableStateFlow<Long>(0L)
    val lastAutoSavedTime: StateFlow<Long> = _lastAutoSavedTime.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val creatorProfile = repository.creatorProfile
    val allStories = repository.allStories

    private val _studioState = MutableStateFlow(StoryStudioState())
    val studioState: StateFlow<StoryStudioState> = _studioState.asStateFlow()

    private val _pipelineState = MutableStateFlow(PipelineExecutionState())
    val pipelineState: StateFlow<PipelineExecutionState> = _pipelineState.asStateFlow()

    private val _selectedStoryId = MutableStateFlow<String?>(null)
    val selectedStoryId: StateFlow<String?> = _selectedStoryId.asStateFlow()

    val categories = listOf(
        "Anime",
        "Fantasy & Magic",
        "Sci-Fi / Cyberpunk",
        "Romance & Drama",
        "Mystery & Thriller",
        "Historical Fiction"
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultDataLoaded()
            val profile = repository.creatorProfile.firstOrNull()
            if (profile != null) {
                _authState.value = AuthState.Authenticated(profile)
            } else {
                _authState.value = AuthState.Authenticated(
                    CreatorProfileEntity(
                        id = "creator_demo",
                        email = "creator@sahitya.ai",
                        name = "Sahitya Creator",
                        handle = "@sahitya_writer",
                        bio = "Independent AI Storysmith & Character Architect"
                    )
                )
            }
        }

        // DataStore Local Draft Auto-Save Pipeline
        viewModelScope.launch {
            _studioState.collect { state ->
                if (state.title.isNotBlank() || state.subtitle.isNotBlank() || state.description.isNotBlank() || state.characters.isNotEmpty() || state.masterTimeline.isNotBlank()) {
                    _autoSaveStatus.value = "Saving to DataStore..."
                    draftDataStore.autoSaveDraft(
                        storyId = state.storyId,
                        step = state.currentStep,
                        title = state.title,
                        subtitle = state.subtitle,
                        description = state.description,
                        category = state.selectedCategory,
                        mainThumbnail = state.mainThumbnail,
                        bannerImage = state.bannerImage,
                        subThumbnails = state.subThumbnails,
                        videoUri = state.videoUri,
                        characters = state.characters,
                        masterTimeline = state.masterTimeline,
                        seasons = state.seasons
                    )
                    _autoSaveStatus.value = "Auto-saved in DataStore"
                    _lastAutoSavedTime.value = System.currentTimeMillis()
                }
            }
        }
    }

    fun restoreDraftFromDataStore(snapshot: DraftSnapshot) {
        _studioState.value = StoryStudioState(
            currentStep = snapshot.currentStep,
            storyId = snapshot.storyId.ifBlank { UUID.randomUUID().toString() },
            title = snapshot.title,
            subtitle = snapshot.subtitle,
            description = snapshot.description,
            selectedCategory = snapshot.selectedCategory,
            mainThumbnail = snapshot.mainThumbnail.ifBlank { "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800" },
            bannerImage = snapshot.bannerImage.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200" },
            subThumbnails = snapshot.subThumbnails,
            videoUri = snapshot.videoUri,
            characters = snapshot.characters,
            masterTimeline = snapshot.masterTimeline,
            seasons = snapshot.seasons
        )
        _autoSaveStatus.value = "Draft Restored from DataStore"
    }

    fun clearDataStoreDraft() {
        viewModelScope.launch {
            draftDataStore.clearDraft()
            _autoSaveStatus.value = "DataStore Draft Cleared"
        }
    }

    // --- Auth Actions ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val nameFromEmail = email.substringBefore("@").replace(".", " ").capitalize()
            val profile = CreatorProfileEntity(
                id = "creator_${UUID.randomUUID().toString().take(8)}",
                email = email,
                name = nameFromEmail.ifBlank { "Creator" },
                handle = "@${nameFromEmail.lowercase().replace(" ", "_")}",
                bio = "Content Creator on Sahitya AI Platform"
            )
            repository.saveCreatorProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
        }
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
    }

    // --- Story Studio Wizard Actions ---
    fun startNewStory() {
        val newId = UUID.randomUUID().toString()
        _studioState.value = StoryStudioState(
            storyId = newId,
            characters = listOf(
                CharacterEntity(
                    id = UUID.randomUUID().toString(),
                    storyId = newId,
                    name = "Hero Protagonist",
                    backstory = "Central character of the story with unique backstory and motivation.",
                    language = "Hinglish",
                    traits = "Brave, Smart, Resourceful"
                )
            ),
            seasons = listOf(
                SeasonEntity(
                    id = UUID.randomUUID().toString(),
                    storyId = newId,
                    seasonNumber = 1,
                    seasonName = "Season 1: Origins",
                    seasonTimeline = "Initial arc introducing main characters and conflict.",
                    characterIdsJson = ""
                )
            )
        )
    }

    fun editExistingStory(story: StoryEntity) {
        viewModelScope.launch {
            val chars = repository.getCharactersForStory(story.id).firstOrNull() ?: emptyList()
            val seasons = repository.getSeasonsForStory(story.id).firstOrNull() ?: emptyList()

            val subs = if (story.subThumbnailsJson.isNotBlank()) story.subThumbnailsJson.split(",") else emptyList()

            _studioState.value = StoryStudioState(
                currentStep = 1,
                storyId = story.id,
                title = story.title.take(30),
                subtitle = story.subtitle.take(150),
                description = story.description.take(500),
                selectedCategory = story.category,
                mainThumbnail = story.mainThumbnail,
                bannerImage = story.bannerImage,
                subThumbnails = subs.take(5),
                videoUri = story.videoUri,
                characters = chars,
                masterTimeline = story.masterTimeline,
                seasons = seasons
            )
        }
    }

    fun updateStudioPage1(
        title: String,
        subtitle: String,
        description: String,
        category: String,
        mainThumbnail: String,
        bannerImage: String
    ) {
        _studioState.value = _studioState.value.copy(
            title = title.take(30),
            subtitle = subtitle.take(150),
            description = description.take(500),
            selectedCategory = category,
            mainThumbnail = mainThumbnail,
            bannerImage = bannerImage
        )
    }

    fun updateSubThumbnails(urls: List<String>) {
        _studioState.value = _studioState.value.copy(
            subThumbnails = urls.take(5)
        )
    }

    fun updateVideoUri(uri: String) {
        _studioState.value = _studioState.value.copy(
            videoUri = uri
        )
    }

    fun setWizardStep(step: Int) {
        _studioState.value = _studioState.value.copy(currentStep = step)
    }

    // Character Management
    fun addCharacter() {
        val currentList = _studioState.value.characters.toMutableList()
        val newChar = CharacterEntity(
            id = UUID.randomUUID().toString(),
            storyId = _studioState.value.storyId,
            name = "Character ${currentList.size + 1}",
            backstory = "",
            language = "Hinglish",
            traits = "Trait 1, Trait 2"
        )
        currentList.add(newChar)
        _studioState.value = _studioState.value.copy(characters = currentList)
    }

    fun updateCharacter(index: Int, updated: CharacterEntity) {
        val currentList = _studioState.value.characters.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updated
            _studioState.value = _studioState.value.copy(characters = currentList)
        }
    }

    fun removeCharacter(index: Int) {
        val currentList = _studioState.value.characters.toMutableList()
        if (index in currentList.indices && currentList.size > 1) {
            val removed = currentList.removeAt(index)
            _studioState.value = _studioState.value.copy(characters = currentList)
            viewModelScope.launch {
                characterRepository.deleteCharacterById(removed.id)
            }
        }
    }

    fun saveCharacterToDb(character: CharacterEntity) {
        viewModelScope.launch {
            characterRepository.upsertCharacter(character)
        }
    }

    fun deleteCharacterFromDb(characterId: String) {
        viewModelScope.launch {
            characterRepository.deleteCharacterById(characterId)
        }
    }

    // Season Management
    fun addSeason() {
        val currentSeasons = _studioState.value.seasons.toMutableList()
        val nextNumber = currentSeasons.size + 1
        val newSeason = SeasonEntity(
            id = UUID.randomUUID().toString(),
            storyId = _studioState.value.storyId,
            seasonNumber = nextNumber,
            seasonName = "Season $nextNumber: Arc ${nextNumber}",
            seasonTimeline = "",
            characterIdsJson = ""
        )
        currentSeasons.add(newSeason)
        _studioState.value = _studioState.value.copy(seasons = currentSeasons)
    }

    fun updateSeason(index: Int, updated: SeasonEntity) {
        val currentList = _studioState.value.seasons.toMutableList()
        if (index in currentList.indices) {
            val isAnime = _studioState.value.selectedCategory.equals("Anime", ignoreCase = true)
            val cappedTimeline = if (isAnime) updated.seasonTimeline.take(50000) else updated.seasonTimeline
            currentList[index] = updated.copy(seasonTimeline = cappedTimeline)
            _studioState.value = _studioState.value.copy(seasons = currentList)
        }
    }

    fun removeSeason(index: Int) {
        val currentList = _studioState.value.seasons.toMutableList()
        if (index in currentList.indices && currentList.size > 1) {
            currentList.removeAt(index)
            val reindexed = currentList.mapIndexed { idx, item ->
                item.copy(seasonNumber = idx + 1)
            }
            _studioState.value = _studioState.value.copy(seasons = reindexed)
        }
    }

    fun updateMasterTimeline(timeline: String) {
        _studioState.value = _studioState.value.copy(masterTimeline = timeline)
    }

    // Save story as draft
    fun saveStoryDraft() {
        viewModelScope.launch {
            val s = _studioState.value
            val isAnime = s.selectedCategory.equals("Anime", ignoreCase = true)

            val story = StoryEntity(
                id = s.storyId,
                title = s.title.take(30).ifBlank { "Untitled Story" },
                subtitle = s.subtitle.take(150),
                description = s.description.take(500),
                category = s.selectedCategory,
                mainThumbnail = s.mainThumbnail,
                bannerImage = s.bannerImage,
                subThumbnailsJson = s.subThumbnails.joinToString(","),
                videoUri = s.videoUri,
                isAnime = isAnime,
                masterTimeline = s.masterTimeline,
                status = "Draft"
            )

            repository.createOrUpdateDraft(story, s.characters, s.seasons)
        }
    }

    // Save & Trigger AI Pipeline Execution
    fun saveAndRunPipeline(onStartPipeline: () -> Unit) {
        viewModelScope.launch {
            saveStoryDraft()
            _selectedStoryId.value = _studioState.value.storyId
            onStartPipeline()
            executeAiPipeline(_studioState.value.storyId)
        }
    }

    fun executeAiPipeline(storyId: String) {
        viewModelScope.launch {
            _pipelineState.value = PipelineExecutionState(
                isRunning = true,
                progressPercent = 0.05f,
                currentStageMessage = "Initializing AI Pipeline..."
            )

            val logsList = mutableListOf<String>()

            val result = repository.runAiPublishingPipeline(storyId) { progress ->
                logsList.add("[${progress.stage}] ${progress.message}")
                _pipelineState.value = _pipelineState.value.copy(
                    isRunning = !progress.isComplete,
                    progressPercent = progress.progressPercent,
                    currentStageMessage = progress.message,
                    logs = logsList.toList()
                )
            }

            _pipelineState.value = _pipelineState.value.copy(
                isRunning = false,
                progressPercent = 1.0f,
                result = result,
                logs = result?.logs ?: logsList.toList()
            )
        }
    }

    fun publishSelectedStoryToNetwork(onComplete: (com.example.data.repository.PublishSyncResult) -> Unit) {
        val storyId = _selectedStoryId.value ?: _studioState.value.storyId
        viewModelScope.launch {
            val syncResult = repository.publishStoryToNetwork(storyId)
            onComplete(syncResult)
        }
    }

    fun syncAllStoriesToSupabase(onComplete: (List<String>) -> Unit) {
        viewModelScope.launch {
            val stories = repository.allStories.firstOrNull() ?: emptyList()
            val results = mutableListOf<String>()
            for (story in stories) {
                val res = repository.publishStoryToNetwork(story.id)
                results.add("${story.title}: ${res.message}")
            }
            onComplete(results)
        }
    }

    fun selectStory(storyId: String) {
        _selectedStoryId.value = storyId
    }

    fun generateDatabaseSqlScript(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val stories = repository.allStories.firstOrNull() ?: emptyList()
            val allChars = mutableListOf<CharacterEntity>()
            val allSeasons = mutableListOf<SeasonEntity>()

            for (story in stories) {
                val chars = repository.getCharactersForStory(story.id).firstOrNull() ?: emptyList()
                val seasons = repository.getSeasonsForStory(story.id).firstOrNull() ?: emptyList()
                allChars.addAll(chars)
                allSeasons.addAll(seasons)
            }

            val sqlScript = com.example.util.SqlExportUtil.generateFullDatabaseSqlScript(
                stories = stories,
                characters = allChars,
                seasons = allSeasons
            )
            onResult(sqlScript)
        }
    }
}
