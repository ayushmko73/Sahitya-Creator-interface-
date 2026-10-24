import re

with open("app/src/main/java/com/example/data/repository/SahityaRepository.kt", "r") as f:
    content = f.read()

# Remove the old story draft 3 insertion block
old_block_pattern = r'val draftStoryId3 = "story_draft_youarealone_03".*?if \(existingDraft3 == null\) \{.*?Season 1: The Deep Silence".*?\}\n\s*\)\n\s*\)\n\s*\}'
content = re.sub(old_block_pattern, '', content, flags=re.DOTALL)

# Insert the new story draft 4
new_story = """
        val draftStoryId4 = "story_draft_youarealone_04"
        val existingDraft4 = storyDao.getStoryById(draftStoryId4)
        if (existingDraft4 == null) {
            val draftStory4 = StoryEntity(
                id = draftStoryId4,
                title = "You are alone",
                subtitle = "A Psychological Horror Experience",
                description = "An isolated researcher in a deep-sea station begins receiving transmissions from an entity mimicking their own voice, insisting that there's no one else left on Earth.",
                category = "Horror",
                mainThumbnail = "android.resource://com.example/drawable/you_are_alone_thumb_1786869481604",
                bannerImage = "android.resource://com.example/drawable/you_are_alone_banner_1786869493571",
                isAnime = false,
                masterTimeline = "{\\n  s: Act 1 - The Echo Transmission\\n  t: Day 124\\n  c: Dr. Elias Vance, The Echo\\n  w: Deep-sea station, dark, oppressive\\n}\\n{\\n  s: Act 2 - Paranoia Sets In\\n  t: Day 126\\n  c: Dr. Elias Vance, The Echo\\n  w: Claustrophobic, failing lights\\n}\\n{\\n  s: Act 3 - Breaking the Mirror\\n  t: Day 127\\n  c: Dr. Elias Vance, The Echo\\n  w: Flooding corridors, utter darkness\\n}",
                status = "Draft",
                totalViews = 0L,
                totalChats = 0L,
                weeklyChats = 0L
            )
            storyDao.insertStory(draftStory4)
            characterDao.insertCharacters(
                listOf(
                    CharacterEntity(
                        id = "char_youarealone_01",
                        storyId = draftStoryId4,
                        name = "Dr. Elias Vance",
                        backstory = "Stranded researcher slowly losing his grip on reality in Sector 4 of the abyssal station.",
                        language = "English",
                        traits = "Paranoid, Isolated, Desperate, Intelligent"
                    ),
                    CharacterEntity(
                        id = "char_youarealone_02",
                        storyId = draftStoryId4,
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
                        storyId = draftStoryId4,
                        seasonNumber = 1,
                        seasonName = "Season 1: The Deep Silence",
                        seasonTimeline = "Elias is alone... or so he thinks until the first broadcast answers his radio check with his own voice.",
                        characterIdsJson = "char_youarealone_01, char_youarealone_02"
                    )
                )
            )
        }"""

content = content.replace("        }\n    }\n}", "        }" + new_story + "\n    }\n}")

with open("app/src/main/java/com/example/data/repository/SahityaRepository.kt", "w") as f:
    f.write(content)
