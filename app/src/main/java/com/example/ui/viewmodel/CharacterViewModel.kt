package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CharacterEntity
import com.example.data.repository.CharacterRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class CharacterOperationResult {
    object Idle : CharacterOperationResult()
    object Loading : CharacterOperationResult()
    data class Success(val message: String) : CharacterOperationResult()
    data class Error(val errorMessage: String) : CharacterOperationResult()
}

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val characterRepository = CharacterRepository(db.characterDao())

    private val _selectedStoryId = MutableStateFlow<String?>(null)
    val selectedStoryId: StateFlow<String?> = _selectedStoryId.asStateFlow()

    private val _selectedCharacterId = MutableStateFlow<String?>(null)
    val selectedCharacterId: StateFlow<String?> = _selectedCharacterId.asStateFlow()

    private val _operationResult = MutableStateFlow<CharacterOperationResult>(CharacterOperationResult.Idle)
    val operationResult: StateFlow<CharacterOperationResult> = _operationResult.asStateFlow()

    val allCharacters: StateFlow<List<CharacterEntity>> = characterRepository.allCharacters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val charactersForSelectedStory: StateFlow<List<CharacterEntity>> = _selectedStoryId
        .flatMapLatest { storyId ->
            if (storyId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                characterRepository.getCharactersForStory(storyId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCharacter: StateFlow<CharacterEntity?> = _selectedCharacterId
        .flatMapLatest { charId ->
            if (charId.isNullOrBlank()) {
                flowOf(null)
            } else {
                characterRepository.getCharacterById(charId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectStory(storyId: String?) {
        _selectedStoryId.value = storyId
    }

    fun selectCharacter(characterId: String?) {
        _selectedCharacterId.value = characterId
    }

    // CRUD - Create
    fun createCharacter(
        storyId: String,
        name: String,
        backstory: String,
        language: String = "Hinglish",
        traits: String = "",
        avatarUrl: String = ""
    ) {
        viewModelScope.launch {
            _operationResult.value = CharacterOperationResult.Loading
            try {
                if (name.isBlank()) {
                    _operationResult.value = CharacterOperationResult.Error("Character name cannot be empty.")
                    return@launch
                }
                val newCharacter = CharacterEntity(
                    id = UUID.randomUUID().toString(),
                    storyId = storyId,
                    name = name.trim(),
                    backstory = backstory.trim(),
                    language = language,
                    traits = traits.trim(),
                    avatarUrl = avatarUrl.trim()
                )
                characterRepository.insertCharacter(newCharacter)
                _operationResult.value = CharacterOperationResult.Success("Character '${newCharacter.name}' created successfully!")
            } catch (e: Exception) {
                _operationResult.value = CharacterOperationResult.Error("Failed to create character: ${e.localizedMessage}")
            }
        }
    }

    // CRUD - Read (direct list retrieval)
    fun fetchCharactersForStoryDirect(storyId: String, onResult: (List<CharacterEntity>) -> Unit) {
        viewModelScope.launch {
            val list = characterRepository.getCharactersListForStory(storyId)
            onResult(list)
        }
    }

    // CRUD - Update
    fun updateCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            _operationResult.value = CharacterOperationResult.Loading
            try {
                if (character.name.isBlank()) {
                    _operationResult.value = CharacterOperationResult.Error("Character name cannot be empty.")
                    return@launch
                }
                characterRepository.updateCharacter(character)
                _operationResult.value = CharacterOperationResult.Success("Character '${character.name}' updated successfully!")
            } catch (e: Exception) {
                _operationResult.value = CharacterOperationResult.Error("Failed to update character: ${e.localizedMessage}")
            }
        }
    }

    // CRUD - Upsert
    fun upsertCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            _operationResult.value = CharacterOperationResult.Loading
            try {
                if (character.name.isBlank()) {
                    _operationResult.value = CharacterOperationResult.Error("Character name cannot be empty.")
                    return@launch
                }
                characterRepository.upsertCharacter(character)
                _operationResult.value = CharacterOperationResult.Success("Character '${character.name}' saved!")
            } catch (e: Exception) {
                _operationResult.value = CharacterOperationResult.Error("Failed to save character: ${e.localizedMessage}")
            }
        }
    }

    // CRUD - Batch Insert / Upsert
    fun insertCharacters(characters: List<CharacterEntity>) {
        viewModelScope.launch {
            _operationResult.value = CharacterOperationResult.Loading
            try {
                characterRepository.insertCharacters(characters)
                _operationResult.value = CharacterOperationResult.Success("${characters.size} character(s) saved!")
            } catch (e: Exception) {
                _operationResult.value = CharacterOperationResult.Error("Failed to insert characters: ${e.localizedMessage}")
            }
        }
    }

    // CRUD - Delete Single
    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            _operationResult.value = CharacterOperationResult.Loading
            try {
                characterRepository.deleteCharacterById(characterId)
                if (_selectedCharacterId.value == characterId) {
                    _selectedCharacterId.value = null
                }
                _operationResult.value = CharacterOperationResult.Success("Character deleted successfully.")
            } catch (e: Exception) {
                _operationResult.value = CharacterOperationResult.Error("Failed to delete character: ${e.localizedMessage}")
            }
        }
    }

    // CRUD - Delete All for Story
    fun deleteCharactersForStory(storyId: String) {
        viewModelScope.launch {
            _operationResult.value = CharacterOperationResult.Loading
            try {
                characterRepository.deleteCharactersForStory(storyId)
                _operationResult.value = CharacterOperationResult.Success("All characters for story deleted.")
            } catch (e: Exception) {
                _operationResult.value = CharacterOperationResult.Error("Failed to delete story characters: ${e.localizedMessage}")
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = CharacterOperationResult.Idle
    }
}
