package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.CreatorProfileEntity
import com.example.data.local.entities.PipelineLogEntity
import com.example.data.local.entities.SeasonEntity
import com.example.data.local.entities.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreatorDao {
    @Query("SELECT * FROM creator_profiles LIMIT 1")
    fun getCreatorProfile(): Flow<CreatorProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CreatorProfileEntity)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStoryById(id: String): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteStory(id: String)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE storyId = :storyId")
    fun getCharactersForStory(storyId: String): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE storyId = :storyId")
    suspend fun getCharactersListForStory(storyId: String): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    fun getCharacterById(characterId: String): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterByIdDirect(characterId: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun deleteCharacterById(characterId: String)

    @Query("DELETE FROM characters WHERE storyId = :storyId")
    suspend fun deleteCharactersForStory(storyId: String)
}

@Dao
interface SeasonDao {
    @Query("SELECT * FROM seasons WHERE storyId = :storyId ORDER BY seasonNumber ASC")
    fun getSeasonsForStory(storyId: String): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE storyId = :storyId ORDER BY seasonNumber ASC")
    suspend fun getSeasonsListForStory(storyId: String): List<SeasonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(seasons: List<SeasonEntity>)

    @Query("DELETE FROM seasons WHERE storyId = :storyId")
    suspend fun deleteSeasonsForStory(storyId: String)
}

@Dao
interface PipelineLogDao {
    @Query("SELECT * FROM pipeline_logs WHERE storyId = :storyId ORDER BY timestamp ASC")
    fun getLogsForStory(storyId: String): Flow<List<PipelineLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PipelineLogEntity)

    @Query("DELETE FROM pipeline_logs WHERE storyId = :storyId")
    suspend fun deleteLogsForStory(storyId: String)
}
