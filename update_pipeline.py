import sys

with open("app/src/main/java/com/example/data/pipeline/AiPipelineProcessor.kt", "r") as f:
    content = f.read()

# Add trainingData to PipelineResult data class
content = content.replace("val formattedTimeline: String,", "val formattedTimeline: String,\n    val trainingData: String = \"\",")

# Update stage 2 to fetch trainingData
old_stage2 = """val rawTokenEstimate = (combinedText.length / 3.8).toInt().coerceAtLeast(100)
        var formattedTimeline = callGemini8020Compressor(combinedText)"""

new_stage2 = """val trainingData = com.example.data.repository.SupabaseSyncService.fetch10StoriesForAiTraining()
        val rawTokenEstimate = (combinedText.length / 3.8).toInt().coerceAtLeast(100)
        var formattedTimeline = callGemini8020Compressor(combinedText, trainingData)"""

content = content.replace(old_stage2, new_stage2)

# Update callGemini8020Compressor signature
content = content.replace("private fun callGemini8020Compressor(rawText: String): String {", "private fun callGemini8020Compressor(rawText: String, trainingData: String): String {")

# Update prompt to include trainingData
old_prompt = """                RAW INPUT:"""
new_prompt = """                LEARNING EXAMPLES FROM PREVIOUS STORIES (Use these to improve extraction formatting and relevance):
                $trainingData

                RAW INPUT:"""

content = content.replace(old_prompt, new_prompt)

# Update buildPrecompiledSystemPrompt
content = content.replace("val precompiledPrompt = buildPrecompiledSystemPrompt(story, characters, seasons, formattedTimeline)", "val precompiledPrompt = buildPrecompiledSystemPrompt(story, characters, seasons, formattedTimeline)")

# Update calculateQualityScore
content = content.replace("val qualityScore = calculateQualityScore(story, characters, formattedTimeline)", "val qualityScore = calculateQualityScore(story, characters, formattedTimeline, trainingData)")

content = content.replace("private fun calculateQualityScore(\n        story: StoryEntity,\n        characters: List<CharacterEntity>,\n        formattedTimeline: String\n    ): Int {", "private fun calculateQualityScore(\n        story: StoryEntity,\n        characters: List<CharacterEntity>,\n        formattedTimeline: String,\n        trainingData: String\n    ): Int {")

content = content.replace("if (formattedTimeline.length > 50) score += 5", "if (formattedTimeline.length > 50) score += 5\n        if (trainingData.isNotBlank()) score += 2")

with open("app/src/main/java/com/example/data/pipeline/AiPipelineProcessor.kt", "w") as f:
    f.write(content)

