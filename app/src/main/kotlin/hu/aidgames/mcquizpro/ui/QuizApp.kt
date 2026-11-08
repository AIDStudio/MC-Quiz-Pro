package hu.aidgames.mcquizpro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.aidgames.mcquizpro.data.*
import hu.aidgames.mcquizpro.ui.theme.*

private enum class Tab { PLAY, SCORES }

/** A teljes alkalmazás: menü, kvíz, eredmény és ranglista. */
@Composable
fun QuizApp() {
    val context = LocalContext.current
    val questions: List<QuizQuestion> = remember {
        runCatching { loadQuestions(context) }.getOrDefault(emptyList())
    }
    val store = remember { ScoreStore(context) }

    var tab by rememberSaveable { mutableStateOf(Tab.PLAY) }
    var playing by rememberSaveable { mutableStateOf(false) }
    var round by rememberSaveable { mutableIntStateOf(0) }
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    var scores by remember { mutableStateOf(store.load()) }
    var result by remember { mutableStateOf<ScoreEntry?>(null) }
    var timedOut by remember { mutableStateOf(false) }

    // A kiválasztott nehézséghez tartozó kérdések; a kvíz ezekkel indul.
    val levelQuestions = remember(questions, difficulty) { questions.forDifficulty(difficulty) }

    Scaffold(
        containerColor = McColors.Background,
        bottomBar = {
            if (!playing && result == null) {
                BottomTabs(selected = tab, onSelect = { tab = it })
            }
        },
    ) { padding ->
        McBackground(modifier = Modifier.padding(padding)) {
            val finished = result
            when {
                tab == Tab.SCORES -> LeaderboardScreen(scores = scores)

                finished != null -> ResultScreen(
                    entry = finished,
                    timedOut = timedOut,
                    onNameChange = { name ->
                        val updated = finished.copy(name = name)
                        store.upsert(updated)
                        scores = store.load()
                        result = updated
                    },
                    onPlayAgain = {
                        result = null
                        round++
                        playing = true
                    },
                    onShowScores = {
                        result = null
                        tab = Tab.SCORES
                    },
                )

                questions.isEmpty() -> EmptyState()

                playing -> key(round) {
                    QuizScreen(
                        questions = levelQuestions,
                        timeLimitSeconds = difficulty.timeLimitSeconds,
                        difficultyLabel = difficulty.label,
                        onFinished = { correct, total, isTimeout ->
                            val entry = ScoreEntry(
                                name = DEFAULT_PLAYER_NAME,
                                score = correct,
                                total = total,
                                timestamp = System.currentTimeMillis(),
                            )
                            // Automatikus mentés: az eredmény azonnal eltárolódik, a név később frissül.
                            store.upsert(entry)
                            scores = store.load()
                            timedOut = isTimeout
                            playing = false
                            result = entry
                        },
                    )
                }

                else -> MenuScreen(
                    difficulty = difficulty,
                    onDifficulty = { difficulty = it },
                    questionCountFor = { level -> questions.forDifficulty(level).size },
                    best = scores.firstOrNull(),
                    onStart = { playing = true },
                )
            }
        }
    }
}

/** Nehézségi szintek gombjainak színe: könnyű = fű, közepes = fa, nehéz = redstone. */
private fun Difficulty.buttonColor() = when (this) {
    Difficulty.EASY -> McColors.Grass
    Difficulty.MEDIUM -> McColors.Wood
    Difficulty.HARD -> McColors.Redstone
}

@Composable
private fun MenuScreen(
    difficulty: Difficulty,
    onDifficulty: (Difficulty) -> Unit,
    questionCountFor: (Difficulty) -> Int,
    best: ScoreEntry?,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Címsáv: fűves felső réteg, alatta földblokk, benne a játék neve.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .mcBevel(McColors.DirtDark)
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(McColors.Grass),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "MC QUIZ PRO",
                color = McColors.Gold,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Teszteld a Minecraft-tudásod!",
                color = McColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(24.dp))

        McCard(Modifier.fillMaxWidth()) {
            Text("VÁLASSZ NEHÉZSÉGET", color = McColors.TextSecondary, fontFamily = PixelFont, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Difficulty.entries.forEach { level ->
                val selected = level == difficulty
                McButton(
                    text = "${level.label}  ·  ${questionCountFor(level)} KÉRDÉS  ·  ${level.timeLimitSeconds} MP",
                    onClick = { onDifficulty(level) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (selected) level.buttonColor() else McColors.StoneLight,
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(difficulty.description, color = McColors.TextPrimary, fontFamily = PixelFont, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            val bestText = best?.let { "${it.name}: ${it.score} / ${it.total}" } ?: "még nincs"
            Text("Rekord: $bestText", color = McColors.Diamond, fontFamily = PixelFont)
        }

        Spacer(Modifier.height(24.dp))
        McButton(
            text = "KVÍZ INDÍTÁSA",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            color = McColors.Grass,
        )
    }
}

@Composable
private fun BottomTabs(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        modifier = Modifier
        .fillMaxWidth()
        .mcBevel(McColors.StoneDark)
        .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        McButton(
            text = "JÁTÉK",
            onClick = { onSelect(Tab.PLAY) },
            modifier = Modifier.weight(1f),
            color = if (selected == Tab.PLAY) McColors.Grass else McColors.StoneLight,
        )
        McButton(
            text = "RANGLISTA",
            onClick = { onSelect(Tab.SCORES) },
            modifier = Modifier.weight(1f),
            color = if (selected == Tab.SCORES) McColors.Dirt else McColors.StoneLight,
        )
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        McCard {
            Text(
                text = "Nem található kérdés. Ellenőrizd az assets/questions.json fájlt.",
                color = McColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
