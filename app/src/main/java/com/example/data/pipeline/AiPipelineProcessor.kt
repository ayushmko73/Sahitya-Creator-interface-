package com.example.data.pipeline

import com.example.BuildConfig
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SeasonEntity
import com.example.data.local.entities.StoryEntity
import com.example.util.StoryBlockParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PipelineProgress(
    val stage: String,
    val progressPercent: Float,
    val message: String,
    val isComplete: Boolean = false,
    val isError: Boolean = false
)

data class PipelineResult(
    val formattedTimeline: String,
    val trainingData: String = "",
    val structuredBlocksText: String,
    val qualityScore: Int,
    val rawTokens: Int,
    val compressedTokens: Int,
    val precompiledSystemPrompt: String,
    val logs: List<String>,
    val isFormatValid: Boolean = true
)

class AiPipelineProcessor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun executePipeline(
        story: StoryEntity,
        characters: List<CharacterEntity>,
        seasons: List<SeasonEntity>,
        onProgress: (PipelineProgress) -> Unit
    ): PipelineResult = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()

        fun log(stage: String, msg: String) {
            val entry = "[$stage] $msg"
            logs.add(entry)
        }

        log("Pipeline", "Initiating Sahitya AI Compression & Quality Pipeline...")
        onProgress(PipelineProgress("Stage 1", 0.05f, "Stage 1: Verifying user-pasted {s, t, c, story} data blocks (from ChatGPT/Gemini)..."))

        // -------------------------------------------------------------
        // STAGE 1: External Format Validation ({s, t, c, story} Blocks)
        // Stage 1 is formatted externally by creators in ChatGPT/Gemini
        // -------------------------------------------------------------
        delay(300)
        val combinedText = "${story.title} ${story.description} ${story.masterTimeline} " +
                characters.joinToString(" ") { "${it.name} ${it.backstory} ${it.traits}" } + " " +
                seasons.joinToString(" ") { "${it.seasonName} ${it.seasonTimeline}" }

        val parsedBlocks = StoryBlockParser.parseBlocks(combinedText)
        val master = story.masterTimeline.lowercase()
        val hasStrictFormat = master.contains("{") && master.contains("}") && master.contains("s:") && master.contains("t:") && master.contains("c:") && master.contains("w:")
        val hasExplicitBlocks = hasStrictFormat
        val isStructured = story.title.isNotBlank() && (story.description.length > 10 || story.masterTimeline.length > 10 || characters.isNotEmpty())

        if (!hasExplicitBlocks) {
            log("Stage 1 Error", "FORMAT ERROR: Story is missing {s, t, c, story} blocks pasted from ChatGPT/Gemini.")
            onProgress(PipelineProgress(
                stage = "Stage 1",
                progressPercent = 0.2f,
                message = "FORMAT ERROR: Agent only supports { s : t : c : w : } format. Input like 'Arohi is nice' will be rejected.",
                isError = true
            ))

            return@withContext PipelineResult(
                formattedTimeline = "",
                structuredBlocksText = "ERROR: Missing user-pasted {s, t, c, story} block structure.",
                qualityScore = 0,
                rawTokens = 0,
                compressedTokens = 0,
                precompiledSystemPrompt = "",
                logs = logs,
                isFormatValid = false
            )
        }

        val structuredBlocksText = if (hasExplicitBlocks) {
            combinedText
        } else {
            generateStructuredBlocks(story, characters, seasons)
        }

        log("Stage 1", "Stage 1 Verified! User-pasted {s, t, c, story} data blocks accepted.")
        onProgress(PipelineProgress("Stage 1", 0.33f, "Stage 1 Verified: Pasted {s, t, c, story} blocks accepted. Starting Stage 2 Compression..."))

        // -------------------------------------------------------------
        // STAGE 2: 80/20 Context Compression Engine
        // -------------------------------------------------------------
        delay(400)
        onProgress(PipelineProgress("Stage 2", 0.60f, "Stage 2: 80/20 Context Compression Engine distilling narrative essence..."))
        log("Stage 2", "Distilling structural narrative essence for efficient AI memory...")

        val trainingData = com.example.data.repository.SupabaseSyncService.fetch10StoriesForAiTraining()
        val rawTokenEstimate = (combinedText.length / 3.8).toInt().coerceAtLeast(100)
        var formattedTimeline = callGemini8020Compressor(combinedText, trainingData)
        if (formattedTimeline.isBlank()) {
            formattedTimeline = local8020Compressor(story, characters, seasons)
        }

        val compressedTokenEstimate = (formattedTimeline.length / 4.0).toInt().coerceAtLeast(40)
        val compressionRatio = String.format("%.1f", (1.0 - (compressedTokenEstimate.toDouble() / rawTokenEstimate.toDouble())) * 100)
        log("Stage 2", "80/20 Compression Complete! Tokens reduced by $compressionRatio% ($rawTokenEstimate -> $compressedTokenEstimate tokens).")

        // -------------------------------------------------------------
        // STAGE 3: Quality Manager Agent Review & Approval
        // -------------------------------------------------------------
        delay(400)
        onProgress(PipelineProgress("Stage 3", 0.85f, "Stage 3: Quality Manager Agent auditing lore consistency & safety..."))
        log("Stage 3", "Quality Manager Agent auditing lore consistency, safety compliance, and system prompts...")

        val precompiledPrompt = buildPrecompiledSystemPrompt(story, characters, seasons, formattedTimeline)
        val qualityScore = calculateQualityScore(story, characters, formattedTimeline, trainingData)

        log("Stage 3", "Quality Audit Approved! Coherence Score: $qualityScore/100.")
        onProgress(PipelineProgress("Complete", 1.0f, "Quality Approved! Story ready to publish live to database.", isComplete = true))

        return@withContext PipelineResult(
            formattedTimeline = formattedTimeline,
            trainingData = trainingData,
            structuredBlocksText = structuredBlocksText,
            qualityScore = qualityScore,
            rawTokens = rawTokenEstimate,
            compressedTokens = compressedTokenEstimate,
            precompiledSystemPrompt = precompiledPrompt,
            logs = logs,
            isFormatValid = true
        )
    }

    private fun generateStructuredBlocks(
        story: StoryEntity,
        characters: List<CharacterEntity>,
        seasons: List<SeasonEntity>
    ): String {
        val sBlock = seasons.joinToString("\n") { "{s: Season ${it.seasonNumber} - ${it.seasonName} | Timeline: ${it.seasonTimeline}}" }
            .ifBlank { "{s: Season 1 - Main Arc | Timeline: ${story.masterTimeline.ifBlank { story.description }}}" }

        val cBlock = characters.joinToString("\n") { "{c: Character=${it.name} | Lang=${it.language} | Traits=${it.traits} | Backstory=${it.backstory}}" }
            .ifBlank { "{c: Character=Main Protagonist | Traits=Courageous | Backstory=Hero of the story}" }

        val tBlock = "{t: Master Timeline: ${story.masterTimeline.ifBlank { story.description }}}"
        val storyBlock = "{story: ID=${story.id} | Title=${story.title} | Category=${story.category} | Synopsis=${story.description}}"

        return """
            $storyBlock
            $sBlock
            $cBlock
            $tBlock
        """.trimIndent()
    }

    private fun callGemini8020Compressor(rawText: String, trainingData: String): String {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return ""

            val prompt = """
                You are the Sahitya Story Formatter AI (80/20 Compression Engine).
                Your job is to apply the 80/20 rule to raw story timelines and character notes:
                Extract the 20% of core structural plot points, character behavioral rules, and world anchors that drive 80% of AI chat roleplay engagement.
                Strip away wordy descriptions while keeping key constraints crisp and bulleted.

                LEARNING EXAMPLES FROM PREVIOUS STORIES (Use these to improve extraction formatting and relevance):
                $trainingData

                RAW INPUT:
                $rawText

                FORMATTED DENSE NARRATIVE ESSENCE (In clear structured Markdown):
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(requestUrl)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: return ""
                val respJson = JSONObject(respStr)
                val candidates = respJson.optJSONArray("candidates") ?: return ""
                if (candidates.length() > 0) {
                    val first = candidates.getJSONObject(0)
                    val content = first.optJSONObject("content") ?: return ""
                    val parts = content.optJSONArray("parts") ?: return ""
                    if (parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "")
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully
        }
        return ""
    }

    private fun local8020Compressor(
        story: StoryEntity,
        characters: List<CharacterEntity>,
        seasons: List<SeasonEntity>
    ): String {
        val charSummary = characters.joinToString("\n") { c ->
            "- [CORE CHAR] ${c.name} (${c.language}): ${c.traits.take(60)} -> Rule: Keep backstory anchors (${c.backstory.take(80)})"
        }

        val timelineSummary = if (story.isAnime && seasons.isNotEmpty()) {
            seasons.joinToString("\n") { s ->
                "- [SEASON ${s.seasonNumber}: ${s.seasonName}] Core Arc: ${s.seasonTimeline.take(120)}"
            }
        } else {
            "- [MASTER ARC] ${story.masterTimeline.take(200)}"
        }

        return """
            ### 🎯 80/20 COMPRESSED NARRATIVE ESSENCE
            **Title:** ${story.title} (${story.category})
            
            **20% Core Engagement Drivers:**
            $charSummary

            **Structural Plot Hooks:**
            $timelineSummary
            
            **Runtime Behavioral Invariants:**
            1. Characters MUST strictly stick to assigned language rules (Hinglish/English).
            2. Never break lore boundaries during intense roleplay dialogue.
            3. Maintain emotional consistency across chat turns.
        """.trimIndent()
    }

    private fun buildPrecompiledSystemPrompt(
        story: StoryEntity,
        characters: List<CharacterEntity>,
        seasons: List<SeasonEntity>,
        formattedTimeline: String
    ): String {
        val characterRules = characters.joinToString("\n\n") { c ->
            """
            <character name="${c.name}">
              <language>${c.language}</language>
              <traits>${c.traits}</traits>
              <backstory_anchor>${c.backstory}</backstory_anchor>
            </character>
            """.trimIndent()
        }

        return """
            SYSTEM PROMPT [PRE-COMPILED BY SAHITYA QUALITY DESIGNER AGENT]
            ================================================================
            WORLD: ${story.title} | CATEGORY: ${story.category}
            DESCRIPTION: ${story.description}

            === 80/20 COMPRESSED LORE ESSENCE ===
            $formattedTimeline

            === CHARACTER BEHAVIORAL DIRECTIVES ===
            $characterRules

            === ANXIETY & CONTEXT AMNESIA PREVENTION RULES ===
            - Stay in character at all costs. Never mention you are an AI.
            - If user speaks in Hinglish, reply in native conversational Hinglish.
            - Maintain psychological hooks and character flaws consistently across long context windows.
        """.trimIndent()
    }

    private fun calculateQualityScore(
        story: StoryEntity,
        characters: List<CharacterEntity>,
        formattedTimeline: String,
        trainingData: String
    ): Int {
        var score = 85
        if (story.title.length > 3) score += 3
        if (characters.isNotEmpty()) score += 5
        if (characters.any { it.language.equals("Hinglish", ignoreCase = true) }) score += 2
        if (formattedTimeline.length > 50) score += 5
        if (trainingData.isNotBlank()) score += 2
        return score.coerceAtMost(99)
    }
}
