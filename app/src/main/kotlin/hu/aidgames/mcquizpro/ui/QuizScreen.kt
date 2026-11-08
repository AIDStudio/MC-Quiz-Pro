package hu.aidgames.mcquizpro.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.aidgames.mcquizpro.data.QuizQuestion
import hu.aidgames.mcquizpro.data.withShuffledOptions
import hu.aidgames.mcquizpro.ui.theme.McButton
import hu.aidgames.mcquizpro.ui.theme.McCard
import hu.aidgames.mcquizpro.ui.theme.McColors
import hu.aidgames.mcquizpro.ui.theme.PixelFont
import hu.aidgames.mcquizpro.ui.theme.mcBevel
import kotlinx.coroutines.delay

// A helyes válasz után a tűzijáték látszódjon, ezért hosszabb a visszajelzés.
private const val ANSWER_FEEDBACK_MS = 1400L
private const val DANGER_SECONDS = 10

/**
 * A kvíz képernyő. A kérdések sorrendje kvízindításkor, az opciók sorrendje minden kérdésnél újra keveredik.
 * [onFinished] a helyes válaszok számát, az összes kérdés számát és azt kapja, hogy lejárt-e az idő.
 */
@Composable
fun QuizScreen(
    questions: List<QuizQuestion>,
    timeLimitSeconds: Int,
    difficultyLabel: String,
    onFinished: (correct: Int, total: Int, timedOut: Boolean) -> Unit,
) {
    val rounds = remember { questions.shuffled() }
    val total = rounds.size

    var index by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var remaining by remember { mutableIntStateOf(timeLimitSeconds) }
    var ended by remember { mutableStateOf(false) }
    var celebrations by remember { mutableIntStateOf(0) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    fun finish(timedOut: Boolean) {
        if (ended) return
        ended = true
        currentOnFinished(correct, total, timedOut)
    }

    // Teljes játékidő: másodpercenként csökken, lejáratkor a végeredmény következik.
    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000L)
            remaining--
        }
        finish(timedOut = true)
    }

    // Válasz után rövid visszajelzés, majd a következő kérdés vagy a vég.
    LaunchedEffect(selected) {
        if (selected != -1) {
            delay(ANSWER_FEEDBACK_MS)
            if (index + 1 < total) {
                index++
                selected = -1
            } else {
                finish(timedOut = false)
            }
        }
    }

    // Minden kérdésnél újra keveredik az opciók sorrendje (és a helyes válasz pozíciója).
    val current = remember(index) { rounds[index].withShuffledOptions() }

    fun choose(option: Int) {
        if (selected != -1 || ended) return
        selected = option
        if (option == current.correct) {
            correct++
            celebrations++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Háttérben: a tűzijáték a kártyák és gombok mögött rajzol.
        FireworksOverlay(trigger = celebrations, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TimerBar(remaining = remaining, total = timeLimitSeconds)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$difficultyLabel · Kérdés ${index + 1} / $total",
                    color = McColors.TextSecondary,
                    fontFamily = PixelFont,
                )
                Text(
                    text = "Pont: $correct",
                    color = McColors.Gold,
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                )
            }

            McCard(Modifier.fillMaxWidth()) {
                current.image?.let { QuestionImage(name = it) }
                if (current.image != null) Spacer(Modifier.height(12.dp))
                Text(
                    text = current.question,
                    color = McColors.TextPrimary,
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            current.options.forEachIndexed { i, option ->
                val color = when {
                    selected == -1 -> McColors.Dirt
                    i == current.correct -> McColors.Grass
                    i == selected -> McColors.Redstone
                    else -> McColors.StoneDark
                }
                McButton(
                    text = "${('A' + i)}) $option",
                    onClick = { choose(i) },
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    enabled = selected == -1,
                )
            }
        }
    }
}

@Composable
private fun TimerBar(remaining: Int, total: Int) {
    val danger = remaining <= DANGER_SECONDS
    val progress by animateFloatAsState(
        targetValue = remaining.toFloat() / total,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "timerProgress",
    )
    val pulse by rememberInfiniteTransition(label = "timerPulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "timerScale",
    )
    val barColor = if (danger) McColors.Redstone else McColors.Grass

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "IDŐ", color = McColors.TextSecondary, fontFamily = PixelFont)
            Text(
                text = "$remaining mp",
                color = if (danger) McColors.Redstone else McColors.Gold,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                modifier = Modifier.graphicsLayer {
                    val s = if (danger) pulse else 1f
                    scaleX = s
                    scaleY = s
                },
            )
        }
        Spacer(Modifier.height(6.dp))
        // Blokk-jellegű idősáv: benyomott bevel-keret, benne a színes töltés.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .mcBevel(McColors.StoneDark, raised = false)
                .padding(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun QuestionImage(name: String) {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .mcBevel(McColors.StoneDarkest, raised = false),
        contentAlignment = Alignment.Center,
    ) {
        if (resId != 0) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            // Ha a drawable hiányzik, a kártya jelzi, melyik fájlt kell bemásolni.
            Text(
                text = "Hiányzó kép\nMásold ide: res/drawable/$name.png",
                color = McColors.TextSecondary,
                textAlign = TextAlign.Center,
                fontFamily = PixelFont,
                fontSize = 12.sp,
            )
        }
    }
}
