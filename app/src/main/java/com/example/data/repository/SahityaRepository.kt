package com.example.data.repository

import com.example.data.local.dao.CharacterDao
import com.example.data.local.dao.CreatorDao
import com.example.data.local.dao.PipelineLogDao
import com.example.data.local.dao.SeasonDao
import com.example.data.local.dao.StoryDao
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.CreatorProfileEntity
import com.example.data.local.entities.PipelineLogEntity
import com.example.data.local.entities.SeasonEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.pipeline.AiPipelineProcessor
import com.example.data.pipeline.PipelineProgress
import com.example.data.pipeline.PipelineResult
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class PublishSyncResult(
    val success: Boolean,
    val message: String,
    val storyId: String
)

class SahityaRepository(
    private val creatorDao: CreatorDao,
    private val storyDao: StoryDao,
    private val characterDao: CharacterDao,
    private val seasonDao: SeasonDao,
    private val pipelineLogDao: PipelineLogDao,
    private val pipelineProcessor: AiPipelineProcessor = AiPipelineProcessor()
) {

    val characterRepository: CharacterRepository = CharacterRepository(characterDao)

    val creatorProfile: Flow<CreatorProfileEntity?> = creatorDao.getCreatorProfile()
    val allStories: Flow<List<StoryEntity>> = storyDao.getAllStories()

    fun getCharactersForStory(storyId: String): Flow<List<CharacterEntity>> =
        characterRepository.getCharactersForStory(storyId)

    fun getCharacterById(characterId: String): Flow<CharacterEntity?> =
        characterRepository.getCharacterById(characterId)

    suspend fun insertCharacter(character: CharacterEntity) =
        characterRepository.insertCharacter(character)

    suspend fun updateCharacter(character: CharacterEntity) =
        characterRepository.updateCharacter(character)

    suspend fun upsertCharacter(character: CharacterEntity) =
        characterRepository.upsertCharacter(character)

    suspend fun deleteCharacterById(characterId: String) =
        characterRepository.deleteCharacterById(characterId)

    fun getSeasonsForStory(storyId: String): Flow<List<SeasonEntity>> =
        seasonDao.getSeasonsForStory(storyId)

    fun getLogsForStory(storyId: String): Flow<List<PipelineLogEntity>> =
        pipelineLogDao.getLogsForStory(storyId)

    suspend fun saveCreatorProfile(profile: CreatorProfileEntity) {
        creatorDao.insertOrUpdateProfile(profile)
    }

    suspend fun createOrUpdateDraft(
        story: StoryEntity,
        characters: List<CharacterEntity>,
        seasons: List<SeasonEntity>
    ) {
        storyDao.insertStory(story)
        characterDao.deleteCharactersForStory(story.id)
        characterDao.insertCharacters(characters)
        seasonDao.deleteSeasonsForStory(story.id)
        seasonDao.insertSeasons(seasons)
    }

    suspend fun runAiPublishingPipeline(
        storyId: String,
        onProgress: (PipelineProgress) -> Unit
    ): PipelineResult? {
        val story = storyDao.getStoryById(storyId) ?: return null
        val characters = characterDao.getCharactersListForStory(storyId)
        val seasons = seasonDao.getSeasonsListForStory(storyId)

        // Clear existing logs for story
        pipelineLogDao.deleteLogsForStory(storyId)

        val result = pipelineProcessor.executePipeline(story, characters, seasons) { progress ->
            onProgress(progress)
        }

        // Save logs to Room
        result.logs.forEach { logText ->
            pipelineLogDao.insertLog(
                PipelineLogEntity(
                    storyId = storyId,
                    stage = if (logText.contains("Stage 1")) "Stage 1: 80/20 Formatter" else "Stage 2: Quality Designer",
                    message = logText
                )
            )
        }

        // Update story entity with formatted result
        val updatedStory = story.copy(
            formattedTimeline = result.formattedTimeline,
            qualityScore = result.qualityScore,
            rawTokens = result.rawTokens,
            compressedTokens = result.compressedTokens,
            precompiledSystemPrompt = result.precompiledSystemPrompt,
            status = "Formatted"
        )
        storyDao.updateStory(updatedStory)

        return result
    }

    suspend fun publishStoryToNetwork(storyId: String): PublishSyncResult {
        val story = storyDao.getStoryById(storyId) ?: return PublishSyncResult(false, "Story not found", storyId)
        val characters = characterDao.getCharactersListForStory(storyId)
        val seasons = seasonDao.getSeasonsListForStory(storyId)
        
        storyDao.updateStory(story.copy(status = "Published"))
        
        // Execute encrypted Supabase payload sync to User App runtime
        val syncResult = SupabaseSyncService.syncStoryToUserApp(story, characters, seasons)

        pipelineLogDao.insertLog(
            PipelineLogEntity(
                storyId = storyId,
                stage = "User App Sync & AES-256 Encryption",
                message = "Story published & encrypted! Status: ${syncResult.message}"
            )
        )
        return syncResult
    }

    suspend fun ensureDefaultDataLoaded() {
        // Populate sample creator and initial published story if empty
        val defaultCreator = CreatorProfileEntity(
            id = "creator_ayush_303",
            email = "creator.ayush@sahitya.ai",
            name = "Ayush Kumar",
            handle = "@ayush_storysmith",
            bio = "Independent Creator specializing in Hinglish Cyber-Anime & Supernatural Dramas. Creator of 'Neon Kismet'.",
            totalViews = 245800L,
            totalChats = 128400L,
            totalEarnings = 34500.00,
            royaltyTier = "Master Creator (80/20 Royalty Share)"
        )
        creatorDao.insertOrUpdateProfile(defaultCreator)

        val sampleStoryId = "story_anime_neon_01"
        val existingStory = storyDao.getStoryById(sampleStoryId)
        if (existingStory == null) {
            val story = StoryEntity(
                id = sampleStoryId,
                title = "Neon Kismet: Re-Birth",
                subtitle = "Cyberpunk Neo-Mumbai Anime Saga",
                description = "In the year 2088, Neo-Mumbai is ruled by AI cartels. A renegade hacker named Aarav and a mysterious Android geisha named Maya unearth the ancient Sahitya protocols.",
                category = "Anime",
                mainThumbnail = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800",
                bannerImage = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
                isAnime = true,
                masterTimeline = "Season 1: Awakening in Sector 7 -> Season 2: Cyber Heist at Indus Tower -> Season 3: Sahitya Core Protocol Activation.",
                formattedTimeline = "### 80/20 COMPRESSED ESSENCE\n- **Aarav**: Witty Hinglish cyberpunk hacker, loyal to Maya.\n- **Maya**: Silent Android warrior with suppressed human memories.\n- **Season 1 Arc**: Aarav decrypts Maya's memory chip in Neo-Dharavi.",
                qualityScore = 96,
                rawTokens = 1240,
                compressedTokens = 290,
                precompiledSystemPrompt = "SYSTEM PROMPT: Roleplay as Aarav & Maya in Hinglish. Never break character.",
                status = "Published",
                totalViews = 182400L,
                totalChats = 94200L,
                weeklyChats = 24800L
            )
            storyDao.insertStory(story)

            // Add additional sample stories to populate Top 5 Weekly AI Chats
            val story2 = StoryEntity(
                id = "story_mystic_02",
                title = "Moksha Chronicles",
                subtitle = "Vedic Cyber-Fantasy Thriller",
                description = "Ancient mantras integrated into quantum AI cores resurrect mythical guardians in Neo-Varanasi.",
                category = "Supernatural",
                mainThumbnail = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800",
                bannerImage = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
                isAnime = false,
                masterTimeline = "Act 1: Varanasi Ghats Hack -> Act 2: Awakening of Garuda Core -> Act 3: Final Quantum Mantra",
                status = "Published",
                totalViews = 95200L,
                totalChats = 48200L,
                weeklyChats = 18500L
            )
            storyDao.insertStory(story2)

            val story3 = StoryEntity(
                id = "story_romance_03",
                title = "Dil-E-Nadaan AI",
                subtitle = "Bilingual College Romance",
                description = "A college programmer builds an AI companion that secretly falls in love with her rival student.",
                category = "Romance",
                mainThumbnail = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=800",
                bannerImage = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=1200",
                isAnime = false,
                masterTimeline = "Chapter 1: Campus Hackathon -> Chapter 2: Midnight Code Talks -> Chapter 3: Confession in Hinglish",
                status = "Published",
                totalViews = 62100L,
                totalChats = 31000L,
                weeklyChats = 12400L
            )
            storyDao.insertStory(story3)

            characterDao.insertCharacters(
                listOf(
                    CharacterEntity(
                        id = "char_01",
                        storyId = sampleStoryId,
                        name = "Aarav 'Zero' Varma",
                        backstory = "Street-smart netrunner from Dharavi Cyber-Grid who built his first neural rig at age 14.",
                        language = "Hinglish",
                        traits = "Sarcastic, Loyal, Fast-talker, Tech Genius"
                    ),
                    CharacterEntity(
                        id = "char_02",
                        storyId = sampleStoryId,
                        name = "Maya-V09",
                        backstory = "Prototype bio-android escaped from Indus Cybernetics with encoded ancient Sahitya lore.",
                        language = "Hinglish",
                        traits = "Calm, Protective, Mystical, Tsundere"
                    )
                )
            )

            seasonDao.insertSeasons(
                listOf(
                    SeasonEntity(
                        id = "season_01",
                        storyId = sampleStoryId,
                        seasonNumber = 1,
                        seasonName = "Season 1: Neon Sparks",
                        seasonTimeline = "Aarav meets Maya after a botched corp heist in Sector 4.",
                        characterIdsJson = "char_01, char_02"
                    ),
                    SeasonEntity(
                        id = "season_02",
                        storyId = sampleStoryId,
                        seasonNumber = 2,
                        seasonName = "Season 2: Dark Protocol",
                        seasonTimeline = "Indus Corp sends elite bounty hunters while Maya's memory leaks.",
                        characterIdsJson = "char_01, char_02"
                    )
                )
            )
        }

        // Add an unpushed local Draft story for creator testing
        val draftStoryId = "story_draft_aetheria_01"
        val existingDraft = storyDao.getStoryById(draftStoryId)
        if (existingDraft == null) {
            val draftStory = StoryEntity(
                id = draftStoryId,
                title = "Aetheria: Shadow Protocol",
                subtitle = "High-Tech Fantasy & Rune Magic Saga",
                description = "In the floating sky-realms of Aetheria, an exiled alchemist and a rogue sky-blade hunt ancient crystalline relics before the Shadow Syndicate corrupts the core.",
                category = "Fantasy",
                mainThumbnail = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
                bannerImage = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1200",
                isAnime = true,
                masterTimeline = "Act 1: Discovery of the Rune Crystal in Sector Sky-9 -> Act 2: Infiltration of the Obsidian Spire -> Act 3: Defense of the Sky Core.",
                status = "Draft",
                totalViews = 0L,
                totalChats = 0L,
                weeklyChats = 0L
            )
            storyDao.insertStory(draftStory)

            characterDao.insertCharacters(
                listOf(
                    CharacterEntity(
                        id = "char_draft_01",
                        storyId = draftStoryId,
                        name = "Kaelen Vane",
                        backstory = "Exiled rune-smith from the Sky Spire who specializes in bio-crystal synthesis.",
                        language = "English & Hinglish",
                        traits = "Brilliant, Cautious, Strategic, Loyal"
                    ),
                    CharacterEntity(
                        id = "char_draft_02",
                        storyId = draftStoryId,
                        name = "Lyra Frost",
                        backstory = "Former captain of the Sky Guardians wielding dual tempest blades.",
                        language = "English",
                        traits = "Brave, Fiery, Swift, Protective"
                    )
                )
            )

            seasonDao.insertSeasons(
                listOf(
                    SeasonEntity(
                        id = "season_draft_01",
                        storyId = draftStoryId,
                        seasonNumber = 1,
                        seasonName = "Season 1: Crystal Awakening",
                        seasonTimeline = "Kaelen and Lyra form an unlikely alliance after an airship ambush.",
                        characterIdsJson = "char_draft_01, char_draft_02"
                    )
                )
            )
        }

        // Add a second brand new un-published draft story: Stardust & Silicon
        val draftStoryId2 = "story_draft_stardust_02"
        val existingDraft2 = storyDao.getStoryById(draftStoryId2)
        if (existingDraft2 == null) {
            val draftStory2 = StoryEntity(
                id = draftStoryId2,
                title = "Stardust & Silicon",
                subtitle = "Interstellar AI & Lost Earth Relics",
                description = "A lone archivist stationed on Jupiter's outermost moon uncovers an encrypted quantum diary from Earth's ancient archives, unlocking forgotten secrets of humanity's digital heritage.",
                category = "Sci-Fi",
                mainThumbnail = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800",
                bannerImage = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200",
                isAnime = false,
                masterTimeline = "Act 1: Signal from the Void -> Act 2: Decryption of Earth Core -> Act 3: The Quantum Awakening",
                status = "Draft",
                totalViews = 0L,
                totalChats = 0L,
                weeklyChats = 0L
            )
            storyDao.insertStory(draftStory2)

            characterDao.insertCharacters(
                listOf(
                    CharacterEntity(
                        id = "char_stardust_01",
                        storyId = draftStoryId2,
                        name = "Aria Vance",
                        backstory = "Senior Space Archivist dedicated to preserving pre-collapse digital artifacts.",
                        language = "English",
                        traits = "Curious, Analytical, Empathetic, Resilient"
                    ),
                    CharacterEntity(
                        id = "char_stardust_02",
                        storyId = draftStoryId2,
                        name = "Unit-7",
                        backstory = "Sentient maintenance android with a penchant for classic poetry and dry wit.",
                        language = "English & Binary",
                        traits = "Witty, Loyal, Secretive, Helpful"
                    )
                )
            )

            seasonDao.insertSeasons(
                listOf(
                    SeasonEntity(
                        id = "season_stardust_01",
                        storyId = draftStoryId2,
                        seasonNumber = 1,
                        seasonName = "Season 1: Signal from the Void",
                        seasonTimeline = "Aria and Unit-7 intercept an anomalous transmission emanating from deep space.",
                        characterIdsJson = "char_stardust_01, char_stardust_02"
                    )
                )
            )
        }

        val draftStoryId3 = "story_draft_youarealone_03"
        val existingDraft3 = storyDao.getStoryById(draftStoryId3)
        if (existingDraft3 == null) {
            val draftStory3 = StoryEntity(
                id = draftStoryId3,
                title = "You are alone",
                subtitle = "A Psychological Horror Experience",
                description = "An isolated researcher in a deep-sea station begins receiving transmissions from an entity mimicking their own voice, insisting that there's no one else left on Earth.",
                category = "Horror",
                mainThumbnail = "https://images.unsplash.com/photo-1509928086326-8968840e6988?w=800",
                bannerImage = "https://images.unsplash.com/photo-1534080534064-0726d1d4f24c?w=1200",
                isAnime = false,
                masterTimeline = "Act 1: The Echo Transmission -> Act 2: Paranoia Sets In -> Act 3: Breaking the Mirror",
                status = "Draft",
                totalViews = 0L,
                totalChats = 0L,
                weeklyChats = 0L
            )
            storyDao.insertStory(draftStory3)
            characterDao.insertCharacters(
                listOf(
                    CharacterEntity(
                        id = "char_youarealone_01",
                        storyId = draftStoryId3,
                        name = "Dr. Elias Vance",
                        backstory = "Stranded researcher slowly losing his grip on reality in Sector 4 of the abyssal station.",
                        language = "English",
                        traits = "Paranoid, Isolated, Desperate, Intelligent"
                    ),
                    CharacterEntity(
                        id = "char_youarealone_02",
                        storyId = draftStoryId3,
                        name = "The Echo",
                        backstory = "A malicious anomaly mimicking Elias's voice and memories, preying on his profound loneliness.",
                        language = "English",
                        traits = "Manipulative, Mimic, Psychological, Unsettling"
                    )
                )
            )
            seasonDao.insertSeasons(
                listOf(
                    SeasonEntity(
                        id = "season_youarealone_01",
                        storyId = draftStoryId3,
                        seasonNumber = 1,
                        seasonName = "Season 1: The Deep Silence",
                        seasonTimeline = "Elias is alone... or so he thinks until the first broadcast answers his radio check with his own voice.",
                        characterIdsJson = "char_youarealone_01, char_youarealone_02"
                    )
                )
            )
        }
        storyDao.deleteStory("story_draft_youarealone_03")
        storyDao.deleteStory("story_draft_youarealone_04")
        val draftStoryId5 = "story_draft_youarealone_05"
        val existingDraft5 = storyDao.getStoryById(draftStoryId5)
        if (existingDraft5 == null) {
            val draftStory5 = StoryEntity(
                id = draftStoryId5,
                title = "You are alone",
                subtitle = "A Psychological Horror Experience",
                description = "An isolated researcher in a deep-sea station begins receiving transmissions from an entity mimicking their own voice, insisting that there's no one else left on Earth.",
                category = "Horror",
                mainThumbnail = "android.resource://com.example/drawable/you_are_alone_thumb_1786869481604",
                bannerImage = "android.resource://com.example/drawable/you_are_alone_banner_1786869493571",
                subThumbnailsJson = "android.resource://com.example/drawable/you_are_alone_subthumb_1786870557233",
                isAnime = false,
                masterTimeline = "{\n  s: Act 1 - The Echo Transmission\n  t: Day 124\n  c: Dr. Elias Vance, The Echo\n  w: Deep-sea station, dark, oppressive\n}\n{\n  s: Act 2 - Paranoia Sets In\n  t: Day 126\n  c: Dr. Elias Vance, The Echo\n  w: Claustrophobic, failing lights\n}\n{\n  s: Act 3 - Breaking the Mirror\n  t: Day 127\n  c: Dr. Elias Vance, The Echo\n  w: Flooding corridors, utter darkness\n}",
                status = "Draft",
                totalViews = 0L,
                totalChats = 0L,
                weeklyChats = 0L
            )
            storyDao.insertStory(draftStory5)
            characterDao.insertCharacters(
                listOf(
                    CharacterEntity(
                        id = "char_youarealone_01",
                        storyId = draftStoryId5,
                        name = "Dr. Elias Vance",
                        backstory = "Stranded researcher slowly losing his grip on reality in Sector 4 of the abyssal station.",
                        language = "English",
                        traits = "Paranoid, Isolated, Desperate, Intelligent"
                    ),
                    CharacterEntity(
                        id = "char_youarealone_02",
                        storyId = draftStoryId5,
                        name = "The Echo",
                        backstory = "A malicious anomaly mimicking Elias's voice and memories, preying on his profound loneliness.",
                        language = "English",
                        traits = "Manipulative, Mimic, Psychological, Unsettling"
                    )
                )
            )
            seasonDao.insertSeasons(
                listOf(
                    SeasonEntity(
                        id = "season_youarealone_01",
                        storyId = draftStoryId5,
                        seasonNumber = 1,
                        seasonName = "Season 1: The Deep Silence",
                        seasonTimeline = "Elias is alone... or so he thinks until the first broadcast answers his radio check with his own voice.",
                        characterIdsJson = "char_youarealone_01, char_youarealone_02"
                    )
                )
            )
        }
    }
}
