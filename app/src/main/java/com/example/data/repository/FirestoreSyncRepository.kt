package com.example.data.repository

import android.util.Log
import com.example.data.local.entities.StoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository class to manage syncing local stories and content to Firebase Firestore.
 * Ready for database structure customization and sync configuration.
 */
class FirestoreSyncRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "FirestoreSyncRepo"
        private const val COLLECTION_PUBLISHED_STORIES = "published_stories"
    }

    /**
     * Placeholder method to sync a single story to Firestore.
     */
    suspend fun syncStoryToFirestore(story: StoryEntity): Boolean {
        return try {
            val storyData = mapOf(
                "id" to story.id,
                "title" to story.title,
                "subtitle" to story.subtitle,
                "description" to story.description,
                "category" to story.category,
                "mainThumbnail" to story.mainThumbnail,
                "bannerImage" to story.bannerImage,
                "isAnime" to story.isAnime,
                "status" to story.status,
                "totalViews" to story.totalViews,
                "totalChats" to story.totalChats,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_PUBLISHED_STORIES)
                .document(story.id)
                .set(storyData)
                .await()

            Log.d(TAG, "Successfully synced story ${story.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing story to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Placeholder method to sync a list of stories to Firestore.
     */
    suspend fun syncAllStories(stories: List<StoryEntity>): Int {
        var successCount = 0
        for (story in stories) {
            if (syncStoryToFirestore(story)) {
                successCount++
            }
        }
        return successCount
    }
}
