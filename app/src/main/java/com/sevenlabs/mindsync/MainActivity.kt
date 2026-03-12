package com.sevenlabs.mindsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevenlabs.mindsync.data.AppDatabase
import com.sevenlabs.mindsync.data.JournalEntry
import com.sevenlabs.mindsync.ui.theme.MindSyncTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val MindSyncBlue = Color(0xFF0369A1)
val SkyBlueAccent = Color(0xFF0EA5E9)
val SlateText = Color(0xFF334155)
val InsightBg = Color(0xFFFFF7F0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindSyncTheme {
                JournalScreen()
            }
        }
    }
}

@Composable
fun JournalScreen() {
    var entryText by remember { mutableStateOf("") }
    var showEmptyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val journalDao = database.journalDao()
    val entries by journalDao.getAllEntries().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val greeting = remember { getGreeting() }
    val dailyQuote = remember { getDailyQuote() }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            confirmButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("I'll write something", color = MindSyncBlue, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Pause for a moment", color = MindSyncBlue, fontWeight = FontWeight.Bold) },
            text = { Text("It looks like your entry is empty. Your thoughts and feelings matter—why not share a little bit of your day?", color = SlateText) },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFDE4E6), Color(0xFFFFDAB9), Color(0xFF8A9AF8))
                )
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.size(280.dp).offset(x = (-50).dp, y = 100.dp).alpha(0.3f).clip(CircleShape).background(Color.White))
            Box(modifier = Modifier.size(200.dp).offset(x = 200.dp, y = 300.dp).alpha(0.2f).clip(CircleShape).background(Color.White))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 60.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = MindSyncBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "MindSync", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MindSyncBlue)
                }

                Spacer(modifier = Modifier.height(48.dp))

                Column {
                    Text(text = greeting, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MindSyncBlue, lineHeight = 42.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = dailyQuote, color = SkyBlueAccent, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    TextField(
                        value = entryText,
                        onValueChange = { entryText = it },
                        placeholder = { Text("What's on your mind today?", color = Color.LightGray) },
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            if (entryText.isNotBlank()) {
                                scope.launch {
                                    val currentDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
                                    val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                                    val newEntry = JournalEntry(
                                        date = currentDate,
                                        time = currentTime,
                                        content = entryText,
                                        aiInsight = "Consistent journaling can help you track patterns and gain insights into your emotions.\n\nConsider reflecting on what you're grateful for today."
                                    )
                                    journalDao.insertEntry(newEntry)
                                    entryText = ""
                                }
                            } else {
                                showEmptyDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MindSyncBlue),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth(0.7f).height(54.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Save Entry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                Text(text = "Previous Entries", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MindSyncBlue.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (entries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No entries yet...", color = MindSyncBlue.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Your journey starts with a single thought.", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp).alpha(0.9f))
                        }
                    }
                }
            } else {
                items(entries) { entry ->
                    PreviousEntryCard(entry = entry, onDelete = { scope.launch { journalDao.deleteEntry(entry.id) } })
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
                Text(text = "Have a great one!", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun PreviousEntryCard(entry: JournalEntry, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = MindSyncBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(entry.date, color = MindSyncBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MindSyncBlue.copy(alpha = 0.6f), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(entry.time, color = MindSyncBlue.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(entry.content, color = SlateText, fontSize = 16.sp, lineHeight = 22.sp)

            entry.aiInsight?.let { insight ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = InsightBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MindSyncBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Insights", color = MindSyncBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        insight.split("\n\n").forEach { line ->
                            Row(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text("•", color = MindSyncBlue, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(line, fontSize = 13.sp, color = SlateText.copy(alpha = 0.9f), lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning!"
        in 12..16 -> "Good afternoon!"
        in 17..20 -> "Good evening!"
        else -> "Good night!"
    }
}

fun getDailyQuote(): String {
    val quotes = listOf("\"Small steps lead to big results.\"", "\"Believe you can.\"", "\"Focus on the good.\"", "\"Start where you are.\"")
    return quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun JournalScreenPreview() { MindSyncTheme { JournalScreen() } }