package com.example.data.repository

import com.example.data.local.dao.CharacterDao
import com.example.data.local.entities.CharacterEntity
import kotlinx.coroutines.flow.Flow

class CharacterRepository(
    private val characterDao: CharacterDao
) {
    val allCharacters: Flow<List<CharacterEntity>> = characterDao.getAllCharacters()

    fun getCharactersForStory(storyId: String): Flow<List<CharacterEntity>> =
        characterDao.getCharactersForStory(storyId)

    suspend fun getCharactersListForStory(storyId: String): List<CharacterEntity> =
        characterDao.getCharactersListForStory(storyId)

    fun getCharacterById(characterId: String): Flow<CharacterEntity?> =
        characterDao.getCharacterById(characterId)

    suspend fun getCharacterByIdDirect(characterId: String): CharacterEntity? =
        characterDao.getCharacterByIdDirect(characterId)

    suspend fun insertCharacter(character: CharacterEntity) {
        characterDao.insertCharacter(character)
    }

    suspend fun insertCharacters(characters: List<CharacterEntity>) {
        characterDao.insertCharacters(characters)
    }

    suspend fun updateCharacter(character: CharacterEntity) {
        characterDao.updateCharacter(character)
    }

    suspend fun upsertCharacter(character: CharacterEntity) {
        characterDao.insertCharacter(character)
    }

    suspend fun deleteCharacterById(characterId: String) {
        characterDao.deleteCharacterById(characterId)
    }

    suspend fun deleteCharactersForStory(storyId: String) {
        characterDao.deleteCharactersForStory(storyId)
    }
}
