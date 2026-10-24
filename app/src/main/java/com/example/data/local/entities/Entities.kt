package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creator_profiles")
data class CreatorProfileEntity(
    @PrimaryKey val id: String = "creator_default_101",
    val email: String,
    val name: String,
    val handle: String,
    val bio: String,
    val totalViews: Long = 142500L,
    val totalChats: Long = 89200L,
    val totalEarnings: Double = 18450.50,
    val royaltyTier: String = "Pro Creator (70/30 Share)"
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: String,
    val mainThumbnail: String,
    val bannerImage: String,
    val subThumbnailsJson: String = "", // Comma-separated URIs/paths for up to 5 sub thumbnails
    val videoUri: String = "",          // Local URI/path for promotional video
    val isAnime: Boolean,
    val masterTimeline: String,
    val formattedTimeline: String = "",
    val qualityScore: Int = 0,
    val rawTokens: Int = 0,
    val compressedTokens: Int = 0,
    val precompiledSystemPrompt: String = "",
    val status: String = "Draft", // Draft, Processing, Published
    val totalViews: Long = 0,
    val totalChats: Long = 0,
    val weeklyChats: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val name: String,
    val backstory: String,
    val language: String = "Hinglish", // Hinglish or English
    val traits: String,
    val avatarUrl: String = ""
)

@Entity(tableName = "seasons")
data class SeasonEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val seasonNumber: Int,
    val seasonName: String,
    val seasonTimeline: String,
    val characterIdsJson: String // Comma separated character IDs
)

@Entity(tableName = "pipeline_logs")
data class PipelineLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: String,
    val stage: String, // Stage 1: 80/20 Formatter, Stage 2: Quality Designer
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
