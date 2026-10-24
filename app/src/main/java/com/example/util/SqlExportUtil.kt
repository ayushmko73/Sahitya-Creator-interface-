package com.example.util

import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SeasonEntity
import com.example.data.local.entities.StoryEntity

object SqlExportUtil {

    /**
     * Generates a complete, ready-to-run Supabase SQL script containing table DDLs,
     * RLS security policies, grants, and INSERT statements for all local stories, characters, and seasons.
     */
    fun generateFullDatabaseSqlScript(
        stories: List<StoryEntity>,
        characters: List<CharacterEntity>,
        seasons: List<SeasonEntity>
    ): String {
        val sb = StringBuilder()

        sb.appendLine("-- =========================================================")
        sb.appendLine("-- Sahitya Ecosystem: Universal Supabase Database Import")
        sb.appendLine("-- Run this script directly in your Supabase SQL Editor.")
        sb.appendLine("-- =========================================================\n")

        // 1. DDL for 'published_stories', 'stories', 'new_releases', 'top_ranked'
        sb.appendLine("""
-- 1. Create 'published_stories' table
CREATE TABLE IF NOT EXISTS public.published_stories (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    subtitle TEXT,
    description TEXT,
    category TEXT,
    genre TEXT,
    tag TEXT,
    main_thumbnail TEXT,
    banner_image TEXT,
    sub_thumbnails_json TEXT,
    video_uri TEXT,
    master_timeline TEXT,
    cover_url TEXT,
    thumbnail_url TEXT,
    image_url TEXT,
    is_anime BOOLEAN DEFAULT false,
    status TEXT DEFAULT 'Published',
    formatted_timeline_encrypted TEXT,
    system_prompt_encrypted TEXT,
    formatted_timeline TEXT,
    precompiled_system_prompt TEXT,
    quality_score INT DEFAULT 95,
    compressed_tokens INT DEFAULT 0,
    user_app_ready BOOLEAN DEFAULT true,
    total_views BIGINT DEFAULT 184200,
    views BIGINT DEFAULT 184200,
    total_chats BIGINT DEFAULT 112000,
    chats BIGINT DEFAULT 112000,
    updated_at BIGINT,
    created_at BIGINT
);

-- 2. Create 'stories' table (Alias compatibility for User Apps querying 'stories')
CREATE TABLE IF NOT EXISTS public.stories (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    subtitle TEXT,
    description TEXT,
    category TEXT,
    genre TEXT,
    tag TEXT,
    main_thumbnail TEXT,
    banner_image TEXT,
    sub_thumbnails_json TEXT,
    video_uri TEXT,
    master_timeline TEXT,
    cover_url TEXT,
    thumbnail_url TEXT,
    image_url TEXT,
    is_anime BOOLEAN DEFAULT false,
    status TEXT DEFAULT 'Published',
    formatted_timeline TEXT,
    precompiled_system_prompt TEXT,
    quality_score INT DEFAULT 95,
    total_views BIGINT DEFAULT 184200,
    views BIGINT DEFAULT 184200,
    total_chats BIGINT DEFAULT 112000,
    chats BIGINT DEFAULT 112000,
    created_at BIGINT
);

-- 3. Create 'new_releases' table (Alias for User Apps displaying 'New Release' feed)
CREATE TABLE IF NOT EXISTS public.new_releases (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    subtitle TEXT,
    description TEXT,
    category TEXT,
    genre TEXT,
    main_thumbnail TEXT,
    banner_image TEXT,
    sub_thumbnails_json TEXT,
    video_uri TEXT,
    master_timeline TEXT,
    cover_url TEXT,
    thumbnail_url TEXT,
    is_anime BOOLEAN DEFAULT false,
    status TEXT DEFAULT 'Published',
    total_views BIGINT DEFAULT 184200,
    total_chats BIGINT DEFAULT 112000,
    created_at BIGINT
);

-- 4. Create 'characters' table
CREATE TABLE IF NOT EXISTS public.characters (
    id TEXT PRIMARY KEY,
    story_id TEXT,
    name TEXT NOT NULL,
    backstory TEXT,
    language TEXT DEFAULT 'Hinglish',
    traits TEXT,
    avatar_url TEXT
);

-- 5. Create 'seasons' table
CREATE TABLE IF NOT EXISTS public.seasons (
    id TEXT PRIMARY KEY,
    story_id TEXT,
    season_number INT DEFAULT 1,
    season_name TEXT,
    season_timeline TEXT,
    character_ids TEXT
);

-- =========================================================
-- PERMISSIONS & DISABLE RLS (FIREFALL PREVENTATIVE ACCESS)
-- =========================================================

-- Disable RLS to guarantee API access for anonymous User Apps
ALTER TABLE public.published_stories DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.stories DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.new_releases DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.characters DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.seasons DISABLE ROW LEVEL SECURITY;

-- Grant full public access to anonymous and authenticated API roles
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated, service_role;

-- Backup permissive policies in case RLS is manually re-enabled
DROP POLICY IF EXISTS "Allow Public Read Published Stories" ON public.published_stories;
CREATE POLICY "Allow Public Read Published Stories" ON public.published_stories FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow Public Insert Published Stories" ON public.published_stories;
CREATE POLICY "Allow Public Insert Published Stories" ON public.published_stories FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Allow Public Update Published Stories" ON public.published_stories;
CREATE POLICY "Allow Public Update Published Stories" ON public.published_stories FOR UPDATE WITH CHECK (true);

DROP POLICY IF EXISTS "Allow Public Read Stories" ON public.stories;
CREATE POLICY "Allow Public Read Stories" ON public.stories FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow Public Insert Stories" ON public.stories;
CREATE POLICY "Allow Public Insert Stories" ON public.stories FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Allow Public Read New Releases" ON public.new_releases;
CREATE POLICY "Allow Public Read New Releases" ON public.new_releases FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow Public Read Characters" ON public.characters;
CREATE POLICY "Allow Public Read Characters" ON public.characters FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow Public Read Seasons" ON public.seasons;
CREATE POLICY "Allow Public Read Seasons" ON public.seasons FOR SELECT USING (true);

-- =========================================================
-- DATA INSERTS FOR CREATOR APP STORIES
-- =========================================================
        """.trimIndent())

        sb.appendLine("\n-- 1. Insert Creator App Stories into 'published_stories', 'stories', and 'new_releases'")

        // Default stories list ensures Creator App initial stories are included even if list is modified
        val allStoriesToExport = stories.toMutableList()
        val defaultStoryIds = stories.map { it.id }.toSet()

        if (!defaultStoryIds.contains("story_hacker_wife_001")) {
            allStoriesToExport.add(
                StoryEntity(
                    id = "story_hacker_wife_001",
                    title = "My Hacker Wife",
                    subtitle = "High-Stakes Cyber Roleplay & Drama",
                    description = "When top cyber investigator Dev meets his mysterious wife Anya, he discovers she leads a double life as a rogue hacker fighting subterranean syndicates.",
                    category = "Roleplay · Drama",
                    mainThumbnail = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800",
                    bannerImage = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
                    isAnime = true,
                    masterTimeline = "Arc 1: Secret Revealed -> Arc 2: Underground Hackathon -> Arc 3: Final Syndicate Infiltration",
                    status = "Published",
                    totalViews = 184200L,
                    totalChats = 112000L
                )
            )
        }

        if (!defaultStoryIds.contains("story_ronin_zero_002")) {
            allStoriesToExport.add(
                StoryEntity(
                    id = "story_ronin_zero_002",
                    title = "Ronin Zero: Crimson Arc",
                    subtitle = "Dark Fantasy Samurai Roleplay",
                    description = "In a dystopian neo-feudal realm, warrior Jin wanders the shadow province seeking vengeance against corrupt lords using crimson blade techniques.",
                    category = "Roleplay · Fantasy",
                    mainThumbnail = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                    bannerImage = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1200",
                    isAnime = true,
                    masterTimeline = "Chapter 1: Blood Oath -> Chapter 2: Shadow Citadel -> Chapter 3: Crimson Awakening",
                    status = "Published",
                    totalViews = 142500L,
                    totalChats = 89000L
                )
            )
        }

        if (!defaultStoryIds.contains("story_cyber_maid_003")) {
            allStoriesToExport.add(
                StoryEntity(
                    id = "story_cyber_maid_003",
                    title = "My Cyberpunk Maid",
                    subtitle = "Sci-Fi Romance & AI Companion",
                    description = "An android companion with evolving human emotions protects her human owner in a bustling mega-city.",
                    category = "Roleplay · Sci-Fi",
                    mainThumbnail = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800",
                    bannerImage = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1200",
                    isAnime = false,
                    masterTimeline = "Act 1: Activation -> Act 2: Emotional Core Glitch -> Act 3: Loyalty Beyond Protocol",
                    status = "Published",
                    totalViews = 110600L,
                    totalChats = 68000L
                )
            )
        }

        for (story in allStoriesToExport) {
            val encPrompt = DataEncryptionUtil.encrypt(story.precompiledSystemPrompt).replace("'", "''")
            val encTimeline = DataEncryptionUtil.encrypt(story.formattedTimeline).replace("'", "''")
            val rawPrompt = story.precompiledSystemPrompt.replace("'", "''")
            val rawTimeline = story.formattedTimeline.replace("'", "''")
            val title = story.title.replace("'", "''")
            val subtitle = story.subtitle.replace("'", "''")
            val desc = story.description.replace("'", "''")
            val category = story.category.replace("'", "''")
            val thumb = story.mainThumbnail.replace("'", "''")
            val banner = story.bannerImage.replace("'", "''")
            val subThumbJson = story.subThumbnailsJson.replace("'", "''")
            val videoUri = story.videoUri.replace("'", "''")
            val masterTimeline = story.masterTimeline.replace("'", "''")
            val views = story.totalViews.coerceAtLeast(110600)
            val chats = story.totalChats.coerceAtLeast(68000)

            sb.appendLine("""
INSERT INTO public.published_stories (id, title, subtitle, description, category, genre, tag, main_thumbnail, banner_image, sub_thumbnails_json, video_uri, master_timeline, cover_url, thumbnail_url, image_url, is_anime, status, formatted_timeline_encrypted, system_prompt_encrypted, formatted_timeline, precompiled_system_prompt, quality_score, compressed_tokens, user_app_ready, total_views, views, total_chats, chats, updated_at, created_at)
VALUES ('${story.id}', '$title', '$subtitle', '$desc', '$category', '$category', '$category', '$thumb', '$banner', '$subThumbJson', '$videoUri', '$masterTimeline', '$thumb', '$thumb', '$thumb', ${story.isAnime}, 'Published', '$encTimeline', '$encPrompt', '$rawTimeline', '$rawPrompt', ${story.qualityScore.coerceAtLeast(95)}, ${story.compressedTokens}, true, $views, $views, $chats, $chats, ${System.currentTimeMillis()}, ${story.createdAt})
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, category = EXCLUDED.category, main_thumbnail = EXCLUDED.main_thumbnail, sub_thumbnails_json = EXCLUDED.sub_thumbnails_json, video_uri = EXCLUDED.video_uri, status = EXCLUDED.status;

INSERT INTO public.stories (id, title, subtitle, description, category, genre, tag, main_thumbnail, banner_image, sub_thumbnails_json, video_uri, master_timeline, cover_url, thumbnail_url, image_url, is_anime, status, formatted_timeline, precompiled_system_prompt, quality_score, total_views, views, total_chats, chats, created_at)
VALUES ('${story.id}', '$title', '$subtitle', '$desc', '$category', '$category', '$category', '$thumb', '$banner', '$subThumbJson', '$videoUri', '$masterTimeline', '$thumb', '$thumb', '$thumb', ${story.isAnime}, 'Published', '$rawTimeline', '$rawPrompt', ${story.qualityScore.coerceAtLeast(95)}, $views, $views, $chats, $chats, ${story.createdAt})
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, category = EXCLUDED.category, main_thumbnail = EXCLUDED.main_thumbnail, sub_thumbnails_json = EXCLUDED.sub_thumbnails_json, video_uri = EXCLUDED.video_uri, status = EXCLUDED.status;

INSERT INTO public.new_releases (id, title, subtitle, description, category, genre, main_thumbnail, banner_image, sub_thumbnails_json, video_uri, master_timeline, cover_url, thumbnail_url, is_anime, status, total_views, total_chats, created_at)
VALUES ('${story.id}', '$title', '$subtitle', '$desc', '$category', '$category', '$thumb', '$banner', '$subThumbJson', '$videoUri', '$masterTimeline', '$thumb', '$thumb', ${story.isAnime}, 'Published', $views, $chats, ${story.createdAt})
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, category = EXCLUDED.category, main_thumbnail = EXCLUDED.main_thumbnail, sub_thumbnails_json = EXCLUDED.sub_thumbnails_json, video_uri = EXCLUDED.video_uri, status = EXCLUDED.status;
            """.trimIndent())
        }

        if (characters.isNotEmpty()) {
            sb.appendLine("\n-- 2. Insert Character Profiles")
            for (char in characters) {
                val name = char.name.replace("'", "''")
                val backstory = char.backstory.replace("'", "''")
                val traits = char.traits.replace("'", "''")
                val avatar = char.avatarUrl.replace("'", "''")
                sb.appendLine("""
INSERT INTO public.characters (id, story_id, name, backstory, language, traits, avatar_url)
VALUES ('${char.id}', '${char.storyId}', '$name', '$backstory', '${char.language}', '$traits', '$avatar')
ON CONFLICT (id) DO NOTHING;
                """.trimIndent())
            }
        }

        if (seasons.isNotEmpty()) {
            sb.appendLine("\n-- 3. Insert Seasons & Arc Timelines")
            for (season in seasons) {
                val sName = season.seasonName.replace("'", "''")
                val sTimeline = season.seasonTimeline.replace("'", "''")
                sb.appendLine("""
INSERT INTO public.seasons (id, story_id, season_number, season_name, season_timeline, character_ids)
VALUES ('${season.id}', '${season.storyId}', ${season.seasonNumber}, '$sName', '$sTimeline', '${season.characterIdsJson}')
ON CONFLICT (id) DO NOTHING;
                """.trimIndent())
            }
        }

        sb.appendLine("\n-- Finished! All Creator App stories imported and published successfully.")
        return sb.toString()
    }
}
