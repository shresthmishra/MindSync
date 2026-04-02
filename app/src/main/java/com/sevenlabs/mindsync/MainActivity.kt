package com.sevenlabs.mindsync

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.work.*
import com.sevenlabs.mindsync.data.AppDatabase
import com.sevenlabs.mindsync.data.JournalEntry
import com.sevenlabs.mindsync.ui.theme.MindSyncTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import java.io.File

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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    var isWritingFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val database = remember { AppDatabase.getDatabase(context) }
    val journalDao = database.journalDao()
    val entries by journalDao.getAllEntries().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var classifier by remember { mutableStateOf<EmotionClassifierHelper?>(null) }
    var deepReflectionHelper by remember { mutableStateOf<DeepReflectionHelper?>(null) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                classifier = EmotionClassifierHelper(context)
            } catch (e: Exception) {
                android.util.Log.e("MindSyncAI", "Classifier Error: ${e.message}")
            }

            try {
                val modelFile = File(context.filesDir, "gemma-2b-it-cpu-int4.bin")
                if (modelFile.exists() && modelFile.length() > 100_000_000) {
                    deepReflectionHelper = DeepReflectionHelper(context, modelFile.absolutePath)
                    Log.d("MindSyncAI", "Gemma successfully initialized")
                }
            } catch (e: Exception) {
                android.util.Log.e("MindSyncAI", "Gemma Setup Error: ${e.message}")
            }
        }
    }

    val greeting: String = remember { getGreeting() }
    val dailyQuote: String = remember { getDailyQuote(context) }
    val timelineDays = remember { getPastTimelineDays() }
    val todayLabel = remember { SimpleDateFormat("MMMM dd", Locale.getDefault()).format(Date()) }

    // logic to ensure the pager always has a "Today" page if no entry exists
    val pagerEntries = remember(entries, todayLabel) {
        val hasTodayEntry = entries.any { it.date.contains(todayLabel) }
        if (!hasTodayEntry) {
            listOf(null) + entries
        } else {
            entries
        }
    }

    val verticalListState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = verticalListState)
    val mainGradient = Brush.verticalGradient(listOf(Color(0xFFFDE4E6), Color(0xFFFFDAB9), Color(0xFF8A9AF8)))

    val snackbarHostState = remember { SnackbarHostState() }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    val focusDimAlpha by animateFloatAsState(targetValue = if (isWritingFocused) 0.10f else 1f, label = "dim")
    val focusBlurRadius by animateDpAsState(targetValue = if (isWritingFocused) 14.dp else 0.dp, label = "blur")

    val pagerState = rememberPagerState(pageCount = { pagerEntries.size })
    val calendarListState = rememberLazyListState()

    DisposableEffect(Unit) {
        onDispose {
            classifier?.close()
            deepReflectionHelper?.close()
            deepReflectionHelper = null
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerEntries.isNotEmpty()) {
            val currentItem = pagerEntries[pagerState.currentPage]
            val dateToSync = if (currentItem == null) {
                todayLabel
            } else {
                SimpleDateFormat("MMMM dd", Locale.getDefault()).format(
                    SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).parse(currentItem.date) ?: Date()
                )
            }
            val calIndex = timelineDays.indexOfFirst {
                SimpleDateFormat("MMMM dd", Locale.getDefault()).format(it) == dateToSync
            }
            if (calIndex != -1) calendarListState.animateScrollToItem(calIndex)
        }
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
                    snackbarHostState.showSnackbar("Press back again to exit.", withDismissAction = true)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) scheduleNotification(context)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else { scheduleNotification(context) }
        } else { scheduleNotification(context) }
    }

    Box(modifier = Modifier.fillMaxSize().background(mainGradient)) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->

            val scrollProgress by remember {
                derivedStateOf {
                    if (verticalListState.layoutInfo.visibleItemsInfo.isEmpty()) 0f
                    else {
                        val info = verticalListState.layoutInfo.visibleItemsInfo[0]
                        ((info.offset.toFloat() / info.size.toFloat()) * -1).coerceIn(0f, 1f)
                    }
                }
            }

            if (showSuccessDialog) {
                AlertDialog(onDismissRequest = { showSuccessDialog = false }, confirmButton = { TextButton(onClick = { showSuccessDialog = false }) { Text("Great!", color = MindSyncBlue, fontWeight = FontWeight.Bold) } }, title = { Text("Done", color = MindSyncBlue, fontWeight = FontWeight.Bold) }, text = { Text("Entry saved successfully!", color = SlateText) }, shape = RoundedCornerShape(24.dp), containerColor = Color.White)
            }
            if (showEmptyDialog) {
                AlertDialog(onDismissRequest = { showEmptyDialog = false }, confirmButton = { TextButton(onClick = { showEmptyDialog = false }) { Text("I'll write something", color = MindSyncBlue, fontWeight = FontWeight.Bold) } }, title = { Text("Pause for a moment", color = MindSyncBlue, fontWeight = FontWeight.Bold) }, text = { Text("It looks like your entry is empty. Your thoughts and feelings matter.", color = SlateText) }, shape = RoundedCornerShape(24.dp), containerColor = Color.White)
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirm = false
                        entryToDelete = null
                    },
                    title = { Text("Delete Entry?", color = MindSyncBlue, fontWeight = FontWeight.Bold) },
                    text = { Text("This will permanently remove this reflection. Are you sure?", color = SlateText) },
                    confirmButton = {
                        TextButton(onClick = {
                            entryToDelete?.let { entry ->
                                scope.launch { journalDao.deleteEntry(entry.id) }
                            }
                            showDeleteConfirm = false
                            entryToDelete = null
                        }) { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            entryToDelete = null
                        }) { Text("Cancel", color = SlateText) }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White
                )
            }

            LazyColumn(
                state = verticalListState,
                flingBehavior = snapBehavior,
                userScrollEnabled = !isWritingFocused,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize().blur(radius = (scrollProgress * 20).dp + focusBlurRadius).alpha(focusDimAlpha)) {
                            Box(modifier = Modifier.size(280.dp).offset(x = (-50).dp, y = 100.dp).alpha(0.3f).clip(CircleShape).background(Color.White))
                            Box(modifier = Modifier.size(200.dp).offset(x = 200.dp, y = 300.dp).alpha(0.2f).clip(CircleShape).background(Color.White))
                        }

                        Column(
                            modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding().padding(horizontal = 24.dp).padding(top = 12.dp).verticalScroll(rememberScrollState())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(focusDimAlpha).blur(focusBlurRadius)) {
                                Icon(Icons.Default.Favorite, null, tint = MindSyncBlue, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "MindSync", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MindSyncBlue)
                            }
                            Spacer(modifier = Modifier.height(42.dp))
                            Text(text = greeting, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = MindSyncBlue, modifier = Modifier.alpha(focusDimAlpha).blur(focusBlurRadius))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = dailyQuote, color = SkyBlueAccent, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.alpha(focusDimAlpha).blur(focusBlurRadius))

                            Spacer(modifier = Modifier.height(32.dp))

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

                                                val detectedEmotion = classifier?.classify(entryText) ?: "Neutral"
                                                val smarterInsight = ReflectionEngine.getReflection(entryText, detectedEmotion)

                                                journalDao.insertEntry(JournalEntry(
                                                    date = date,
                                                    time = time,
                                                    content = entryText,
                                                    aiInsight = smarterInsight
                                                ))
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
                            Spacer(modifier = Modifier.height(if (isWritingFocused) 80.dp else 40.dp))
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
                                Spacer(modifier = Modifier.weight(1f))

                                AnimatedVisibility(
                                    visible = pagerState.currentPage >= 2,
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(0) } }) {
                                        Icon(Icons.Default.Today, null, tint = MindSyncBlue)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyRow(
                                state = calendarListState,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(timelineDays) { dateObj ->
                                    val dateLabel = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(dateObj)
                                    val isToday = dateLabel == todayLabel
                                    val currentEntryDate = if (pagerEntries.isNotEmpty() && pagerState.currentPage < pagerEntries.size) {
                                        val item = pagerEntries[pagerState.currentPage]
                                        if (item == null) todayLabel else SimpleDateFormat("MMMM dd", Locale.getDefault()).format(
                                            SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).parse(item.date) ?: Date()
                                        )
                                    } else null

                                    val isSelected = currentEntryDate == dateLabel
                                    val hasEntries = entries.any { it.date.contains(dateLabel) }

                                    Box(
                                        modifier = Modifier
                                            .width(68.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                when {
                                                    isSelected -> MindSyncBlue
                                                    isToday -> MindSyncBlue.copy(alpha = 0.15f)
                                                    hasEntries -> SkyBlueAccent.copy(alpha = 0.25f)
                                                    else -> Color.White.copy(alpha = 0.3f)
                                                }
                                            )
                                            .clickable {
                                                if (isToday || hasEntries) {
                                                    scope.launch {
                                                        val entryIndex = pagerEntries.indexOfFirst {
                                                            it?.date?.contains(dateLabel) == true || (it == null && isToday)
                                                        }
                                                        if (entryIndex != -1) pagerState.animateScrollToPage(entryIndex)
                                                    }
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        if (isToday) {
                                            Icon(
                                                Icons.Default.Star, null,
                                                tint = if (isSelected) Color.White else MindSyncBlue,
                                                modifier = Modifier.size(10.dp).align(Alignment.TopStart).offset(x = 6.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = SimpleDateFormat("MMM", Locale.getDefault()).format(dateObj).uppercase(), color = if (isSelected) Color.White.copy(alpha = 0.7f) else MindSyncBlue.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(text = SimpleDateFormat("dd", Locale.getDefault()).format(dateObj), color = if (isSelected) Color.White else MindSyncBlue, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                            Text(text = SimpleDateFormat("EEE", Locale.getDefault()).format(dateObj).uppercase(), color = if (isSelected) Color.White.copy(alpha = 0.7f) else MindSyncBlue.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            Text(text = SimpleDateFormat("yyyy", Locale.getDefault()).format(dateObj), color = if (isSelected) Color.White.copy(alpha = 0.5f) else MindSyncBlue.copy(alpha = 0.3f), fontSize = 7.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                HorizontalPager(
                                    state = pagerState,
                                    contentPadding = PaddingValues(horizontal = 42.dp),
                                    pageSpacing = 16.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) { page ->
                                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                    val entry = pagerEntries[page]

                                    Box(
                                        modifier = Modifier.graphicsLayer {
                                            val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                                        }
                                    ) {
                                        if (entry != null) {
                                            PreviousEntryCard(
                                                entry = entry,
                                                onDelete = {
                                                    entryToDelete = entry
                                                    showDeleteConfirm = true
                                                },
                                                onDeepReflection = {
                                                    if (deepReflectionHelper != null) {
                                                        val customInsight = deepReflectionHelper?.generateDeepReflection(entry.content)
                                                        if (customInsight != null) {
                                                            journalDao.insertEntry(entry.copy(aiInsight = customInsight))
                                                            snackbarHostState.showSnackbar("Deep Reflection updated.")
                                                        }
                                                    } else {
                                                        snackbarHostState.showSnackbar("AI model loading. Please wait.")
                                                    }
                                                }
                                            )
                                        } else {
                                            // Logic when there are no entries for the current day yet
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.EditNote, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(64.dp))
                                                Spacer(Modifier.height(16.dp))
                                                Text("Anything significant happen today?", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, textAlign = TextAlign.Center)
                                                Spacer(Modifier.height(8.dp))
                                                Text("Write about it.", color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp, textAlign = TextAlign.Center)
                                                Spacer(Modifier.height(32.dp))
                                                Button(
                                                    onClick = { scope.launch { verticalListState.animateScrollToItem(0) } },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MindSyncBlue),
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.height(54.dp).fillMaxWidth(0.7f)
                                                ) { Text("Write Entry", fontWeight = FontWeight.ExtraBold) }
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
fun PreviousEntryCard(entry: JournalEntry, onDelete: () -> Unit, onDeepReflection: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var isThinking by remember { mutableStateOf(false) }

    val scrollTrap = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                return available
            }
        }
    }
    Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().height(440.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.date, color = MindSyncBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp)
                    Text(entry.time, color = MindSyncBlue.copy(alpha = 0.5f), fontSize = 9.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).alpha(0.3f)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                }
            }

            Column(modifier = Modifier.fillMaxSize().nestedScroll(scrollTrap).padding(start = 24.dp, end = 24.dp, bottom = 24.dp).verticalScroll(rememberScrollState())) {
                Text(text = entry.content, color = SlateText, fontSize = 16.sp, lineHeight = 22.sp)

                AnimatedVisibility(
                    visible = entry.aiInsight != null,
                    enter = fadeIn(animationSpec = tween(600)) + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    entry.aiInsight?.let { insight ->
                        Column {
                            Spacer(modifier = Modifier.height(20.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = InsightBg),
                                border = BorderStroke(1.dp, MindSyncBlue.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = MindSyncBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Insights", color = MindSyncBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(insight)) }, modifier = Modifier.size(20.dp).alpha(0.3f)) {
                                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Crossfade(targetState = insight, animationSpec = tween(500), label = "insight_cross") { text ->
                                        Text(text, fontSize = 13.sp, color = SlateText.copy(alpha = 0.8f), lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        if (!isThinking) {
                            scope.launch {
                                isThinking = true
                                onDeepReflection()
                                isThinking = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MindSyncBlue.copy(alpha = 0.3f))
                ) {
                    if (isThinking) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MindSyncBlue, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Gemma is thinking...", color = MindSyncBlue)
                    } else {
                        Icon(Icons.Default.Psychology, null, tint = MindSyncBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Deep Reflection", color = MindSyncBlue)
                    }
                }
            }
        }
    }
}

fun scheduleNotification(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(calculateDelay(), TimeUnit.MILLISECONDS)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork("journal_reminder", ExistingPeriodicWorkPolicy.KEEP, workRequest)
}

fun calculateDelay(): Long {
    val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 21); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
    if (calendar.timeInMillis <= System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_YEAR, 1)
    return calendar.timeInMillis - System.currentTimeMillis()
}

fun getPastTimelineDays(): List<Date> {
    val days = mutableListOf<Date>()
    val cal = Calendar.getInstance()
    for (i in 0 until 90) { days.add(cal.time); cal.add(Calendar.DAY_OF_YEAR, -1) }
    return days
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return if (hour < 12) "Good morning!" else if (hour < 17) "Good afternoon!" else "Good evening!"
}

fun getDailyQuote(context: Context): String {
    val quotes = context.resources.getStringArray(R.array.daily_quotes)
    return quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]
}