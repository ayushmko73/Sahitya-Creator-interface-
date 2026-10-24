import re

with open("app/src/main/java/com/example/data/repository/SupabaseSyncService.kt", "r") as f:
    content = f.read()

# remove the extra class closing brace and append the function properly
content = re.sub(r'\}\s*\}\s*suspend fun fetch10StoriesForAiTraining', '    }\n\n    suspend fun fetch10StoriesForAiTraining', content)

with open("app/src/main/java/com/example/data/repository/SupabaseSyncService.kt", "w") as f:
    f.write(content)

