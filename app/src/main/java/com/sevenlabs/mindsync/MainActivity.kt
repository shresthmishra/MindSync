package com.sevenlabs.mindsync

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
val SlateText = Color(0xFF0F172A)
val InsightBg = Color(0xFFF0F9FF)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mindsync_prefs", Context.MODE_PRIVATE) }

    var entryText by remember { mutableStateOf(prefs.getString("draft_entry", "") ?: "") }
    var showEmptyDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isWritingFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val database = remember { AppDatabase.getDatabase(context) }
    val journalDao = database.journalDao()
    val entries by journalDao.getAllEntries().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val greeting: String = remember { getGreeting() }
    val dailyQuote: String = remember { getDailyQuote() }
    val timelineDays = remember { getPastTimelineDays() }

    val verticalListState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = verticalListState)
    val mainGradient = Brush.verticalGradient(listOf(Color(0xFFFDE4E6), Color(0xFFFFDAB9), Color(0xFF8A9AF8)))

    val snackbarHostState = remember { SnackbarHostState() }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    val focusDimAlpha by animateFloatAsState(targetValue = if (isWritingFocused) 0.10f else 1f, label = "dim")
    val focusBlurRadius by animateDpAsState(targetValue = if (isWritingFocused) 14.dp else 0.dp, label = "blur")

    val pagerState = rememberPagerState(pageCount = { timelineDays.size })
    val calendarListState = rememberLazyListState()

    LaunchedEffect(pagerState.currentPage) {
        calendarListState.animateScrollToItem(pagerState.currentPage)
    }

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && isWritingFocused) focusManager.clearFocus()
    }

    BackHandler {
        if (isWritingFocused) {
            focusManager.clearFocus()
        } else if (verticalListState.firstVisibleItemIndex == 1) {
            scope.launch { verticalListState.animateScrollToItem(0) }
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                scope.launch {
                    snackbarHostState.showSnackbar("Press back again to exit.", withDismissAction = true, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(mainGradient)) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            if (showSuccessDialog) {
                AlertDialog(onDismissRequest = { showSuccessDialog = false }, confirmButton = { TextButton(onClick = { showSuccessDialog = false }) { Text("Great!", color = MindSyncBlue, fontWeight = FontWeight.Bold) } }, title = { Text("Done", color = MindSyncBlue, fontWeight = FontWeight.Bold) }, text = { Text("Entry saved successfully!", color = SlateText) }, shape = RoundedCornerShape(24.dp), containerColor = Color.White)
            }
            if (showEmptyDialog) {
                AlertDialog(onDismissRequest = { showEmptyDialog = false }, confirmButton = { TextButton(onClick = { showEmptyDialog = false }) { Text("I'll write something", color = MindSyncBlue, fontWeight = FontWeight.Bold) } }, title = { Text("Pause for a moment", color = MindSyncBlue, fontWeight = FontWeight.Bold) }, text = { Text("It looks like your entry is empty. Your thoughts and feelings matter.", color = SlateText) }, shape = RoundedCornerShape(24.dp), containerColor = Color.White)
            }

            val scrollProgress by remember {
                derivedStateOf {
                    if (verticalListState.layoutInfo.visibleItemsInfo.isEmpty()) 0f
                    else {
                        val info = verticalListState.layoutInfo.visibleItemsInfo[0]
                        ((info.offset.toFloat() / info.size.toFloat()) * -1).coerceIn(0f, 1f)
                    }
                }
            }

            val todayLabel = remember { SimpleDateFormat("MMMM dd", Locale.getDefault()).format(Date()) }

            LazyColumn(
                state = verticalListState,
                flingBehavior = snapBehavior,
                userScrollEnabled = !isWritingFocused,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize().blur(radius = (scrollProgress * 20).dp + focusBlurRadius).alpha(focusDimAlpha)) {
                            Box(modifier = Modifier.size(280.dp).offset(x = (-50).dp, y = 100.dp).alpha(0.3f).clip(CircleShape).background(Color.White))
                            Box(modifier = Modifier.size(200.dp).offset(x = 200.dp, y = 300.dp).alpha(0.2f).clip(CircleShape).background(Color.White))
                        }

                        Column(
                            modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding().padding(horizontal = 24.dp, vertical = 32.dp).verticalScroll(rememberScrollState())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(focusDimAlpha).blur(focusBlurRadius)) {
                                Icon(Icons.Default.Favorite, null, tint = MindSyncBlue, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "MindSync", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MindSyncBlue)
                            }
                            Spacer(modifier = Modifier.height(48.dp))
                            Text(text = greeting, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MindSyncBlue, modifier = Modifier.alpha(focusDimAlpha).blur(focusBlurRadius))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = dailyQuote, color = SkyBlueAccent, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.alpha(focusDimAlpha).blur(focusBlurRadius))

                            Spacer(modifier = Modifier.height(40.dp))

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isWritingFocused) 16.dp else 0.dp),
                                modifier = Modifier.fillMaxWidth().height(280.dp)
                            ) {
                                TextField(
                                    value = entryText,
                                    onValueChange = { entryText = it; prefs.edit().putString("draft_entry", it).apply() },
                                    placeholder = { Text("What's on your mind today?", color = Color.LightGray) },
                                    modifier = Modifier.fillMaxSize().onFocusChanged { isWritingFocused = it.isFocused },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = SlateText, unfocusedTextColor = SlateText)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Box(modifier = Modifier.fillMaxWidth().alpha(focusDimAlpha).blur(focusBlurRadius), contentAlignment = Alignment.Center) {
                                Button(
                                    onClick = {
                                        if (entryText.isNotBlank()) {
                                            scope.launch {
                                                val date = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
                                                val time = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                                                journalDao.insertEntry(JournalEntry(date = date, time = time, content = entryText, aiInsight = "Analyzing your reflection..."))
                                                entryText = ""; prefs.edit().remove("draft_entry").apply()
                                                focusManager.clearFocus(); showSuccessDialog = true
                                            }
                                        } else { showEmptyDialog = true }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MindSyncBlue),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(0.7f).height(54.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Save Entry", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(if (isWritingFocused) 80.dp else 20.dp))
                        }

                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp).alpha((1f - (scrollProgress * 2f)) * focusDimAlpha).blur(focusBlurRadius),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                            Text("History", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Box(modifier = Modifier.fillParentMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(vertical = 24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { scope.launch { verticalListState.animateScrollToItem(0) } }) { Icon(Icons.Default.KeyboardArrowDown, null, tint = MindSyncBlue) }
                                Text(text = "Journal History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MindSyncBlue)
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            LazyRow(state = calendarListState, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(timelineDays) { dateObj ->
                                    val dateLabel = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(dateObj)
                                    val isSelected = timelineDays[pagerState.currentPage] == dateObj
                                    val isToday = dateLabel == todayLabel
                                    Column(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(if (isSelected) MindSyncBlue else if (isToday) MindSyncBlue.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.3f)).clickable { scope.launch { pagerState.animateScrollToPage(timelineDays.indexOf(dateObj)) } }.padding(vertical = 10.dp, horizontal = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = SimpleDateFormat("MMM", Locale.getDefault()).format(dateObj).uppercase(), color = if (isSelected) Color.White.copy(alpha = 0.7f) else MindSyncBlue.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = SimpleDateFormat("dd", Locale.getDefault()).format(dateObj), color = if (isSelected) Color.White else MindSyncBlue, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(text = if (isToday) "TODAY" else SimpleDateFormat("EEE", Locale.getDefault()).format(dateObj).uppercase(), color = if (isSelected) Color.White.copy(alpha = 0.7f) else MindSyncBlue.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(text = SimpleDateFormat("yyyy", Locale.getDefault()).format(dateObj), color = if (isSelected) Color.White.copy(alpha = 0.5f) else MindSyncBlue.copy(alpha = 0.3f), fontSize = 8.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                HorizontalPager(
                                    state = pagerState,
                                    contentPadding = PaddingValues(horizontal = 32.dp),
                                    pageSpacing = 16.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) { page ->
                                    val dateForPage = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(timelineDays[page])
                                    val entryForPage = entries.find { it.date.contains(dateForPage) }

                                    if (entryForPage != null) {
                                        PreviousEntryCard(entry = entryForPage, onDelete = { scope.launch { journalDao.deleteEntry(entryForPage.id) } })
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(44.dp))
                                            Spacer(Modifier.height(16.dp))
                                            Text("No entry for this day", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, textAlign = TextAlign.Center)
                                            Spacer(Modifier.height(24.dp))
                                            Button(
                                                onClick = {
                                                    if (dateForPage == todayLabel) {
                                                        scope.launch { verticalListState.animateScrollToItem(0) }
                                                    } else {
                                                        scope.launch { pagerState.animateScrollToPage(0) }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MindSyncBlue),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.height(50.dp)
                                            ) {
                                                Text(if (dateForPage == todayLabel) "Write an entry" else "Jump to Today", fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Looking back at the progress", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviousEntryCard(entry: JournalEntry, onDelete: () -> Unit) {
    val scrollTrap = remember { object : NestedScrollConnection { override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available } }
    Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().height(360.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(entry.date, color = MindSyncBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(entry.time, color = MindSyncBlue.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).alpha(0.3f)) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp)) }
            }
            Column(modifier = Modifier.fillMaxSize().nestedScroll(scrollTrap).padding(start = 24.dp, end = 24.dp, bottom = 24.dp).verticalScroll(rememberScrollState())) {
                Text(text = entry.content, color = SlateText, fontSize = 16.sp, lineHeight = 22.sp)
                entry.aiInsight?.let { insight ->
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = InsightBg), border = BorderStroke(1.dp, MindSyncBlue.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = MindSyncBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Insights", color = MindSyncBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(insight, fontSize = 13.sp, color = SlateText.copy(alpha = 0.8f), lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

fun getPastTimelineDays(): List<Date> {
    val days = mutableListOf<Date>()
    val cal = Calendar.getInstance()
    for (i in 0 until 90) {
        days.add(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return days
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return if (hour < 12) "Good morning!" else if (hour < 17) "Good afternoon!" else "Good evening!"
}

fun getDailyQuote(): String {
    val quotes = listOf(
        "\"The only way to do great work is to love what you do.\"",
        "\"Success is not final, failure is not fatal: it is the courage to continue that counts.\"",
        "\"Believe you can and you're halfway there.\"",
        "\"Your time is limited, so don't waste it living someone else's life.\"",
        "\"Hardships often prepare ordinary people for an extraordinary destiny.\"",
        "\"Do what you can, with what you have, where you are.\"",
        "\"Don't let yesterday take up too much of today.\"",
        "\"If you are working on something that you really care about, you don't have to be pushed.\"",
        "\"The secret of getting ahead is getting started.\"",
        "\"It always seems impossible until it's done.\"",
        "\"Everything you've ever wanted is on the other side of fear.\"",
        "\"Opportunities don't happen. You create them.\"",
        "\"Don't be pushed around by the fears in your mind. Be led by the dreams in your heart.\"",
        "\"Great things are not done by impulse, but by a series of small things brought together.\"",
        "\"Small steps lead to big results.\"",
        "\"Reflection is the mirror of clarity.\"",
        "\"Focus on the good.\"",
        "\"Your potential is endless.\"",
        "\"Action is the foundational key to all success.\"",
        "\"Character is how you treat those who can do nothing for you.\"",
        "\"Peace begins with a smile.\"",
        "\"Kindness is a language which the deaf can hear and the blind can see.\"",
        "\"You are never too old to set another goal or to dream a new dream.\"",
        "\"Start where you are. Use what you have. Do what you can.\"",
        "\"Keep your face always toward the sunshine—and shadows will fall behind you.\"",
        "\"Success usually comes to those who are too busy to be looking for it.\"",
        "\"The only limit to our realization of tomorrow will be our doubts of today.\"",
        "\"Do not wait to strike till the iron is hot; but make it hot by striking.\"",
        "\"A goal is a dream with a deadline.\"",
        "\"Growth and comfort do not coexist.\"",
        "\"Energy and persistence conquer all things.\"",
        "\"Difficulties in life are intended to make us better, not bitter.\"",
        "\"It's not whether you get knocked down, it's whether you get up.\"",
        "\"A winner is a dreamer who never gives up.\"",
        "\"Quality is not an act, it is a habit.\"",
        "\"Happiness is not something readymade. It comes from your own actions.\"",
        "\"Magic is believing in yourself.\"",
        "\"The best way to predict your future is to create it.\"",
        "\"Don't count the days, make the days count.\"",
        "\"Either you run the day or the day runs you.\"",
        "\"Turn your wounds into wisdom.\"",
        "\"If you want to lift yourself up, lift up someone else.\"",
        "\"Your life does not get better by chance, it gets better by change.\"",
        "\"What you do today can improve all your tomorrows.\"",
        "\"Discipline is the bridge between goals and accomplishment.\"",
        "\"The power of imagination makes us infinite.\"",
        "\"Try to be a rainbow in someone else's cloud.\"",
        "\"Courage is being scared to death, but saddling up anyway.\"",
        "\"I find that the harder I work, the more luck I seem to have.\"",
        "\"Success is walking from failure to failure with no loss of enthusiasm.\"",
        "\"The mind is everything. What you think you become.\"",
        "\"Do one thing every day that scares you.\"",
        "\"Innovation distinguishes between a leader and a follower.\"",
        "\"Be yourself; everyone else is already taken.\"",
        "\"Stay hungry, stay foolish.\"",
        "\"The purpose of our lives is to be happy.\"",
        "\"Life is what happens when you're busy making other plans.\"",
        "\"Get busy living or get busy dying.\"",
        "\"You only live once, but if you do it right, once is enough.\"",
        "\"The way to get started is to quit talking and begin doing.\"",
        "\"Your work is going to fill a large part of your life.\"",
        "\"Life is either a daring adventure or nothing at all.\"",
        "\"Go confidently in the direction of your dreams.\"",
        "\"In the end, it's not the years in your life that count.\"",
        "\"Never let the fear of striking out keep you from playing the game.\"",
        "\"Money and success don't change people; they only amplify what is already there.\"",
        "\"Your time is limited, so don't waste it.\"",
        "\"Not how long, but how well you have lived is the main thing.\"",
        "\"The whole secret of a successful life is to find out what is one's destiny to do.\"",
        "\"In order to write about life first you must live it.\"",
        "\"Life is not a problem to be solved, but a reality to be experienced.\"",
        "\"Watch your thoughts; they become words.\"",
        "\"Watch your words; they become actions.\"",
        "\"Watch your actions; they become habits.\"",
        "\"Watch your habits; they become character.\"",
        "\"Character is destiny.\"",
        "\"Self-mastery is the ultimate victory.\"",
        "\"The unexamined life is not worth living.\"",
        "\"Quiet the mind, and the soul will speak.\"",
        "\"Simplicity is the ultimate sophistication.\"",
        "\"The obstacle is the way.\"",
        "\"Breathe. It's just a bad day, not a bad life.\"",
        "\"Stay close to anything that makes you glad you are alive.\"",
        "\"Be the change you wish to see in the world.\"",
        "\"Every strike brings me closer to the next home run.\"",
        "\"I attribute my success to this: I never gave or took any excuse.\"",
        "\"The most difficult thing is the decision to act.\"",
        "\"Definiteness of purpose is the starting point of all achievement.\"",
        "\"We become what we think about.\"",
        "\"The mind is its own place, and in itself can make a heaven of hell.\"",
        "\"Nothing is impossible, the word itself says 'I'm possible'!\"",
        "\"I have not failed. I've just found 10,000 ways that won't work.\"",
        "\"A person who never made a mistake never tried anything new.\"",
        "\"Dream big and dare to fail.\"",
        "\"What you get by achieving your goals is not as important as what you become.\"",
        "\"It does not matter how slowly you go as long as you do not stop.\"",
        "\"The best revenge is massive success.\"",
        "\"Everything has beauty, but not everyone sees it.\"",
        "\"If the wind will not serve, take to the oars.\"",
        "\"Perfection is not attainable, but if we chase perfection we can catch excellence.\"",
        "\"I would rather die of passion than of boredom.\"",
        "\"Productivity is never an accident.\"",
        "\"Make each day your masterpiece.\"",
        "\"Wisdom begins in wonder.\"",
        "\"The only journey is the one within.\"",
        "\"You must be the change you wish to see.\"",
        "\"Integrity is doing the right thing, even when no one is watching.\"",
        "\"Well done is better than well said.\"",
        "\"The only true wisdom is in knowing you know nothing.\"",
        "\"Act as if what you do makes a difference. It does.\"",
        "\"To be the best, you must be able to handle the worst.\"",
        "\"Everything you can imagine is real.\"",
        "\"The beginning is the most important part of the work.\"",
        "\"Silence is a source of great strength.\"",
        "\"A soft answer turneth away wrath.\"",
        "\"He who has a why to live can bear almost any how.\"",
        "\"In the middle of every difficulty lies opportunity.\"",
        "\"Love the life you live. Live the life you love.\"",
        "\"The standard you walk past is the standard you accept.\"",
        "\"Do what is right, not what is easy.\"",
        "\"To live is the rarest thing in the world.\"",
        "\"Keep your eyes on the stars, and your feet on the ground.\"",
        "\"Success is the sum of small efforts.\"",
        "\"The greatest glory in living lies not in never falling.\"",
        "\"The journey of a thousand miles begins with one step.\"",
        "\"You must do the things you think you cannot do.\"",
        "\"If you think you can, you can.\"",
        "\"The only thing we have to fear is fear itself.\"",
        "\"Life is short, and it's up to you to make it sweet.\"",
        "\"What we think, we become.\"",
        "\"All our dreams can come true if we have the courage to pursue them.\"",
        "\"Life is 10% what happens to us and 90% how we react to it.\"",
        "\"There is no substitute for hard work.\"",
        "\"Don't wait. The time will never be just right.\"",
        "\"Whatever you are, be a good one.\"",
        "\"If you change the way you look at things, the things you look at change.\"",
        "\"Change your thoughts and you change your world.\"",
        "\"The only person you are destined to become is the person you decide to be.\"",
        "\"Be not afraid of greatness.\"",
        "\"The heart has its reasons which reason knows nothing of.\"",
        "\"Keep calm and carry on.\"",
        "\"Live as if you were to die tomorrow.\"",
        "\"The best thing to hold onto in life is each other.\"",
        "\"Tough times never last, but tough people do.\"",
        "\"Focus on being productive instead of busy.\"",
        "\"The best preparation for tomorrow is doing your best today.\"",
        "\"Don't stop when you're tired. Stop when you're done.\"",
        "\"A day without laughter is a day wasted.\"",
        "\"Life is a journey, not a destination.\"",
        "\"Believe in yourself.\"",
        "\"Your limitation—it's only your imagination.\"",
        "\"Push yourself, because no one else is going to do it for you.\"",
        "\"Sometimes later becomes never. Do it now.\"",
        "\"Great things never come from comfort zones.\"",
        "\"Dream it. Wish it. Do it.\"",
        "\"Success doesn’t just find you. You have to go out and get it.\"",
        "\"Wake up with determination. Go to bed with satisfaction.\"",
        "\"Do something today that your future self will thank you for.\"",
        "\"Little things make big days.\"",
        "\"It’s going to be hard, but hard does not mean impossible.\"",
        "\"Don’t wait for opportunity. Create it.\"",
        "\"Sometimes we’re tested not to show our weaknesses, but to discover our strengths.\"",
        "\"The key to success is to focus on goals, not obstacles.\"",
        "\"Dream big, pray bigger.\"",
        "\"Don’t give up. The beginning is always the hardest.\"",
        "\"Success is what happens after you have survived all of your mistakes.\"",
        "\"Work hard in silence, let your success be your noise.\"",
        "\"Don’t be the same, be better.\"",
        "\"Every day is a second chance.\"",
        "\"It is not the length of life, but the depth of life.\"",
        "\"Live the life you’ve imagined.\"",
        "\"Everything you need is already inside you.\"",
        "\"The secret of your future is hidden in your daily routine.\"",
        "\"Believe in the power of yet.\"",
        "\"You are your only limit.\"",
        "\"Focus on the step in front of you, not the whole staircase.\"",
        "\"Start each day with a grateful heart.\"",
        "\"The harder you work for something, the greater you’ll feel when you achieve it.\"",
        "\"Don't wish for it. Work for it.\"",
        "\"Self-discipline is self-love.\"",
        "\"Your only competition is the person you were yesterday.\"",
        "\"The sun is new each day.\"",
        "\"Patience is the companion of wisdom.\"",
        "\"Great acts are made up of small deeds.\"",
        "\"Consistency is the key.\"",
        "\"Don't tell people your dreams. Show them.\"",
        "\"The only way to predict the future is to create it.\"",
        "\"Don't look back. You're not going that way.\"",
        "\"Confidence is silent. Insecurities are loud.\"",
        "\"Focus on your soul, not on your role.\"",
        "\"Do it with passion or not at all.\"",
        "\"Stay humble. Stay original.\"",
        "\"If it matters to you, you'll find a way.\"",
        "\"Be your own kind of beautiful.\"",
        "\"The more you give, the more you have.\"",
        "\"You get what you give.\"",
        "\"Think positive and positive things will happen.\"",
        "\"Life is a gift. Wake up every day and realize it.\"",
        "\"Be happy with what you have while working for what you want.\"",
        "\"Comparison is the thief of joy.\"",
        "\"Today is a perfect day to start.\"",
        "\"Happiness is a choice.\"",
        "\"Your vibration attracts your tribe.\"",
        "\"Mindset is everything.\"",
        "\"Do it for you.\"",
        "\"Make it happen.\"",
        "\"Choose kind.\"",
        "\"Radiate positivity.\"",
        "\"Dream without fear. Love without limits.\"",
        "\"Collect moments, not things.\"",
        "\"Enjoy the little things.\"",
        "\"Everything happens for a reason.\"",
        "\"Be the light.\"",
        "\"Grow through what you go through.\"",
        "\"Life is tough, but so are you.\"",
        "\"Choose happy.\"",
        "\"Stay curious.\"",
        "\"Less is more.\"",
        "\"One day at a time.\"",
        "\"Keep on keeping on.\"",
        "\"Be a voice, not an echo.\"",
        "\"Focus on your goals.\"",
        "\"You are enough.\"",
        "\"Believe in your inner magic.\"",
        "\"Find joy in the journey.\"",
        "\"Grateful for today.\"",
        "\"The best is yet to come.\"",
        "\"Trust the process.\"",
        "\"Keep shining.\"",
        "\"Make today amazing.\"",
        "\"Every moment matters.\"",
        "\"Do your best.\"",
        "\"Stay focused.\"",
        "\"Love yourself first.\"",
        "\"Believe in miracles.\"",
        "\"Be bold. Be brave.\"",
        "\"The world is yours.\"",
        "\"Just keep swimming.\"",
        "\"Seize the day.\"",
        "\"Listen to your heart.\"",
        "\"Follow your dreams.\"",
        "\"Stay wild.\"",
        "\"Choose love.\"",
        "\"Focus on yourself.\"",
        "\"Be kind to yourself.\"",
        "\"Spread your wings.\"",
        "\"The sky is the limit.\"",
        "\"Never give up.\"",
        "\"Keep the faith.\"",
        "\"Dream big.\"",
        "\"Be fearless.\"",
        "\"Stay strong.\"",
        "\"Work hard.\"",
        "\"Peace and love.\"",
        "\"Good vibes only.\"",
        "\"Make it simple.\"",
        "\"Live. Love. Laugh.\"",
        "\"Focus on the now.\"",
        "\"Life is beautiful.\"",
        "\"Be present.\"",
        "\"Stay positive.\"",
        "\"You got this.\"",
        "\"Go for it.\"",
        "\"Start now.\"",
        "\"Be grateful.\"",
        "\"Trust yourself.\"",
        "\"Kindness matters.\"",
        "\"Mindfulness matters.\"",
        "\"Stay humble.\"",
        "\"Dream on.\"",
        "\"Love life.\"",
        "\"Be true.\"",
        "\"You are brave.\"",
        "\"Stay gold.\"",
        "\"Keep moving.\"",
        "\"Believe in you.\"",
        "\"Be amazing.\"",
        "\"Sparkle on.\"",
        "\"Just breathe.\"",
        "\"Love wins.\"",
        "\"Hope is power.\"",
        "\"Make it count.\"",
        "\"Be extraordinary.\"",
        "\"Focus on progress.\"",
        "\"Keep it real.\"",
        "\"You are strong.\"",
        "\"Stay inspired.\"",
        "\"Think big.\"",
        "\"Be patient.\"",
        "\"Find your fire.\"",
        "\"Stay happy.\"",
        "\"Live your truth.\"",
        "\"Own your story.\"",
        "\"Be the energy.\"",
        "\"Focus on peace.\"",
        "\"You are worthy.\"",
        "\"Keep exploring.\"",
        "\"Stay balanced.\"",
        "\"Choose wisdom.\"",
        "\"Be the change.\"",
        "\"Find your balance.\"",
        "\"Stay grounded.\"",
        "\"Trust the timing.\"",
        "\"Believe in tomorrow.\"",
        "\"Keep your spark.\"",
        "\"Be the miracle.\"",
        "\"Focus on growth.\"",
        "\"Live with purpose.\"",
        "\"You are rare.\"",
        "\"Stay authentic.\"",
        "\"Follow your light.\"",
        "\"Be the reason.\"",
        "\"Keep growing.\"",
        "\"Stay mindful.\"",
        "\"Believe in magic.\"",
        "\"Make waves.\"",
        "\"Be the soul.\"",
        "\"Focus on vision.\"",
        "\"You are unique.\"",
        "\"Stay true.\"",
        "\"Keep climbing.\"",
        "\"Stay brave.\"",
        "\"Follow your bliss.\"",
        "\"Be the dream.\"",
        "\"Focus on joy.\"",
        "\"You are powerful.\"",
        "\"Stay kind.\"",
        "\"Keep reaching.\"",
        "\"Stay radiant.\"",
        "\"Choose grace.\"",
        "\"Be the silence.\"",
        "\"Focus on heart.\"",
        "\"You are enough.\"",
        "\"Stay bright.\"",
        "\"Keep dreaming.\"",
        "\"Stay limitless.\"",
        "\"Follow your path.\"",
        "\"Be the love.\"",
        "\"Focus on now.\"",
        "\"You are light.\"",
        "\"Stay magic.\"",
        "\"Keep thriving.\""
    )
    return quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]
}