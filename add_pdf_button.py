import re

with open("app/src/main/java/com/example/ui/screens/StoryStudioScreen.kt", "r") as f:
    content = f.read()

import_statement = "import androidx.compose.material.icons.filled.Description\n"
if "import androidx.compose.material.icons.filled.Description" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Delete\n", "import androidx.compose.material.icons.filled.Delete\n" + import_statement)


button_ui = """                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { uriHandler.openUri("https://drive.google.com/file/d/1b7t8bH710JOxAgLP-U0z7WuJg3Abj8PJ/view?usp=drivesdk") },
                            colors = CardDefaults.cardColors(containerColor = SahityaEmeraldAccent.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SahityaEmeraldAccent)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = "PDF Template", tint = SahityaEmeraldAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Download Formatting Template PDF", color = SahityaEmeraldAccent, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Use this template with another AI to properly format your narrative arc before pasting.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        ExpandableSmartWritingPad("""

content = content.replace("                        ExpandableSmartWritingPad(", button_ui)

with open("app/src/main/java/com/example/ui/screens/StoryStudioScreen.kt", "w") as f:
    f.write(content)
