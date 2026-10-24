package com.example.util

data class StoryBlock(
    val id: String,
    val index: Int,            // 1-based index (Block 1, Block 2...)
    val rawText: String,        // Full text including { ... }
    val cleanContent: String,   // Extracted story content inside curly braces
    val batchIndex: Int,       // 1-based batch index (2 blocks per batch: Batch 1 for blocks 1,2; Batch 2 for blocks 3,4...)
    val startCharOffset: Int,
    val endCharOffset: Int
)

data class StoryBlockBatch(
    val batchNumber: Int,      // Batch 1, Batch 2...
    val blocks: List<StoryBlock> // Up to 2 blocks per batch
)

object StoryBlockParser {
    /**
     * Parses text for block patterns enclosed in curly braces, e.g.:
     * { hello world }
     * { --- hello world --- }
     * { \n multi-line block content \n }
     *
     * Un-bracketed text (e.g. "Hello world, it's okay leave it like that") is ignored as plain text,
     * while anything inside { ... } is extracted as a specific Story Block.
     */
    fun parseBlocks(text: String): List<StoryBlock> {
        if (text.isBlank()) return emptyList()

        val blocks = mutableListOf<StoryBlock>()
        
        // Regex matching { followed by anything non-greedy, then }
        val regex = Regex("""\{([\s\S]*?)\}""")
        val matches = regex.findAll(text)

        var blockIndex = 1
        for (match in matches) {
            val raw = match.value
            // Clean content inside { }
            var clean = match.groupValues[1].trim()
            // Strip optional leading/trailing dashes if present
            if (clean.startsWith("---")) clean = clean.substring(3).trim()
            else if (clean.startsWith("--")) clean = clean.substring(2).trim()
            
            if (clean.endsWith("---")) clean = clean.substring(0, clean.length - 3).trim()
            else if (clean.endsWith("--")) clean = clean.substring(0, clean.length - 2).trim()

            if (clean.isNotEmpty()) {
                val batchNo = (blockIndex + 1) / 2

                blocks.add(
                    StoryBlock(
                        id = "block_${blockIndex}_${match.range.first}",
                        index = blockIndex,
                        rawText = raw,
                        cleanContent = clean,
                        batchIndex = batchNo,
                        startCharOffset = match.range.first,
                        endCharOffset = match.range.last
                    )
                )
                blockIndex++
            }
        }

        return blocks
    }

    /**
     * Groups parsed blocks from start to end into batches of 2 blocks per AI dispatch turn
     */
    fun groupIntoBatches(blocks: List<StoryBlock>): List<StoryBlockBatch> {
        if (blocks.isEmpty()) return emptyList()
        return blocks.chunked(2).mapIndexed { index, chunk ->
            StoryBlockBatch(
                batchNumber = index + 1,
                blocks = chunk
            )
        }
    }
}
