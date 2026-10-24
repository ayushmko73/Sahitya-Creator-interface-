package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SeasonEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sahitya_story_drafts")

data class DraftSnapshot(
    val storyId: String = "",
    val currentStep: Int = 1,
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val selectedCategory: String = "Anime",
    val mainThumbnail: String = "",
    val bannerImage: String = "",
    val subThumbnails: List<String> = emptyList(),
    val videoUri: String = "",
    val characters: List<CharacterEntity> = emptyList(),
    val masterTimeline: String = "",
    val seasons: List<SeasonEntity> = emptyList(),
    val lastAutoSavedTimestamp: Long = 0L
)

class DraftDataStoreManager(private val context: Context) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val characterListType = Types.newParameterizedType(List::class.java, CharacterEntity::class.java)
    private val characterAdapter = moshi.adapter<List<CharacterEntity>>(characterListType)

    private val seasonListType = Types.newParameterizedType(List::class.java, SeasonEntity::class.java)
    private val seasonAdapter = moshi.adapter<List<SeasonEntity>>(seasonListType)

    companion object {
        val KEY_HAS_ACTIVE_DRAFT = stringPreferencesKey("draft_has_active")
        val KEY_STORY_ID = stringPreferencesKey("draft_story_id")
        val KEY_CURRENT_STEP = stringPreferencesKey("draft_current_step")
        val KEY_TITLE = stringPreferencesKey("draft_title")
        val KEY_SUBTITLE = stringPreferencesKey("draft_subtitle")
        val KEY_DESCRIPTION = stringPreferencesKey("draft_description")
        val KEY_CATEGORY = stringPreferencesKey("draft_category")
        val KEY_MAIN_THUMBNAIL = stringPreferencesKey("draft_main_thumbnail")
        val KEY_BANNER_IMAGE = stringPreferencesKey("draft_banner_image")
        val KEY_SUB_THUMBNAILS = stringPreferencesKey("draft_sub_thumbnails")
        val KEY_VIDEO_URI = stringPreferencesKey("draft_video_uri")
        val KEY_CHARACTERS_JSON = stringPreferencesKey("draft_characters_json")
        val KEY_MASTER_TIMELINE = stringPreferencesKey("draft_master_timeline")
        val KEY_SEASONS_JSON = stringPreferencesKey("draft_seasons_json")
        val KEY_LAST_SAVED = longPreferencesKey("draft_last_saved")
    }

    val draftFlow: Flow<DraftSnapshot?> = context.dataStore.data.map { prefs ->
        val hasActive = prefs[KEY_HAS_ACTIVE_DRAFT] == "true"
        if (!hasActive) return@map null

        val subThumbStr = prefs[KEY_SUB_THUMBNAILS] ?: ""
        val subThumbs = if (subThumbStr.isNotBlank()) subThumbStr.split(",") else emptyList()

        val charsJson = prefs[KEY_CHARACTERS_JSON] ?: "[]"
        val parsedChars = runCatching { characterAdapter.fromJson(charsJson) }.getOrNull() ?: emptyList()

        val seasonsJson = prefs[KEY_SEASONS_JSON] ?: "[]"
        val parsedSeasons = runCatching { seasonAdapter.fromJson(seasonsJson) }.getOrNull() ?: emptyList()

        DraftSnapshot(
            storyId = prefs[KEY_STORY_ID] ?: "",
            currentStep = prefs[KEY_CURRENT_STEP]?.toIntOrNull() ?: 1,
            title = prefs[KEY_TITLE] ?: "",
            subtitle = prefs[KEY_SUBTITLE] ?: "",
            description = prefs[KEY_DESCRIPTION] ?: "",
            selectedCategory = prefs[KEY_CATEGORY] ?: "Anime",
            mainThumbnail = prefs[KEY_MAIN_THUMBNAIL] ?: "",
            bannerImage = prefs[KEY_BANNER_IMAGE] ?: "",
            subThumbnails = subThumbs,
            videoUri = prefs[KEY_VIDEO_URI] ?: "",
            characters = parsedChars,
            masterTimeline = prefs[KEY_MASTER_TIMELINE] ?: "",
            seasons = parsedSeasons,
            lastAutoSavedTimestamp = prefs[KEY_LAST_SAVED] ?: 0L
        )
    }

    suspend fun autoSaveDraft(
        storyId: String,
        step: Int,
        title: String,
        subtitle: String,
        description: String,
        category: String,
        mainThumbnail: String,
        bannerImage: String,
        subThumbnails: List<String>,
        videoUri: String,
        characters: List<CharacterEntity>,
        masterTimeline: String,
        seasons: List<SeasonEntity>
    ) {
        val charsJson = runCatching { characterAdapter.toJson(characters) }.getOrDefault("[]")
        val seasonsJson = runCatching { seasonAdapter.toJson(seasons) }.getOrDefault("[]")

        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_ACTIVE_DRAFT] = "true"
            prefs[KEY_STORY_ID] = storyId
            prefs[KEY_CURRENT_STEP] = step.toString()
            prefs[KEY_TITLE] = title
            prefs[KEY_SUBTITLE] = subtitle
            prefs[KEY_DESCRIPTION] = description
            prefs[KEY_CATEGORY] = category
            prefs[KEY_MAIN_THUMBNAIL] = mainThumbnail
            prefs[KEY_BANNER_IMAGE] = bannerImage
            prefs[KEY_SUB_THUMBNAILS] = subThumbnails.joinToString(",")
            prefs[KEY_VIDEO_URI] = videoUri
            prefs[KEY_CHARACTERS_JSON] = charsJson
            prefs[KEY_MASTER_TIMELINE] = masterTimeline
            prefs[KEY_SEASONS_JSON] = seasonsJson
            prefs[KEY_LAST_SAVED] = System.currentTimeMillis()
        }
    }

    suspend fun clearDraft() {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_ACTIVE_DRAFT] = "false"
            prefs.remove(KEY_STORY_ID)
            prefs.remove(KEY_CURRENT_STEP)
            prefs.remove(KEY_TITLE)
            prefs.remove(KEY_SUBTITLE)
            prefs.remove(KEY_DESCRIPTION)
            prefs.remove(KEY_CATEGORY)
            prefs.remove(KEY_MAIN_THUMBNAIL)
            prefs.remove(KEY_BANNER_IMAGE)
            prefs.remove(KEY_SUB_THUMBNAILS)
            prefs.remove(KEY_VIDEO_URI)
            prefs.remove(KEY_CHARACTERS_JSON)
            prefs.remove(KEY_MASTER_TIMELINE)
            prefs.remove(KEY_SEASONS_JSON)
            prefs.remove(KEY_LAST_SAVED)
        }
    }
}
