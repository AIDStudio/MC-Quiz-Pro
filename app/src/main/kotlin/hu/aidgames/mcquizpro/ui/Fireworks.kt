package hu.aidgames.mcquizpro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.aidgames.mcquizpro.ui.theme.McColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val BURSTS = 4
private const val BURST_INTERVAL_SEC = 0.25f
private const val PARTICLES_PER_BURST = 36
private const val GRAVITY = 0.35f
private const val SPARK_LIFE_SEC = 1.2f

private val FireworkPalette = listOf(
    McColors.Gold,
    McColors.Diamond,
    McColors.Grass,
    McColors.Redstone,
    Color.White,
    Color(0xFFB36BFF),
)

/** Egy szikra. Koordináták a képernyő SZÉLESSÉGÉHEZ viszonyítva, így a kitörések nem nyúlnak el. */
private class Spark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color,
)

private fun randomBurst(): List<Spark> {
    val cx = Random.nextFloat() * 0.8f + 0.1f
    val cy = Random.nextFloat() * 0.9f + 0.15f
    val first = FireworkPalette.random()
    val second = FireworkPalette.random()
    val rotation = Random.nextFloat() * 2f * PI.toFloat()
    return List(PARTICLES_PER_BURST) { i ->
        val angle = rotation + 2f * PI.toFloat() * i / PARTICLES_PER_BURST
        val speed = 0.25f + Random.nextFloat() * 0.3f
        Spark(
            x = cx,
            y = cy,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            life = 1f,
            color = if (i % 2 == 0) first else second,
        )
    }
}

/**
 * Tűzijáték-réteg a tartalom mögött. A [trigger] minden növelésekor új kitörés-sorozat indul.
 * Pixeles (négyzet alakú) szikrákat rajzol, Minecraft-hangulatban.
 */
@Composable
fun FireworksOverlay(trigger: Int, modifier: Modifier = Modifier) {
    val sparks = remember { mutableStateListOf<Spark>() }
    var frame by remember { mutableLongStateOf(0L) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        var launched = 0
        var elapsed = 0f
        var last = -1L
        while (launched < BURSTS || sparks.isNotEmpty()) {
            withFrameNanos { now ->
                if (last < 0L) last = now
                val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                elapsed += dt
                if (launched < BURSTS && elapsed >= launched * BURST_INTERVAL_SEC) {
                    sparks += randomBurst()
                    launched++
                }
                sparks.forEach { s ->
                    s.vy += GRAVITY * dt
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.life -= dt / SPARK_LIFE_SEC
                }
                sparks.removeAll { it.life <= 0f }
                frame++
            }
        }
    }

    Canvas(modifier = modifier) {
        // A frame olvasása jelzi a Compose-nak, hogy minden képkockánál újra kell rajzolni.
        if (frame < 0L) return@Canvas
        val unit = size.width
        val half = 3.dp.toPx()
        sparks.forEach { s ->
            drawRect(
                color = s.color.copy(alpha = s.life.coerceIn(0f, 1f)),
                topLeft = Offset(s.x * unit - half, s.y * unit - half),
                size = Size(half * 2, half * 2),
            )
        }
    }
}
