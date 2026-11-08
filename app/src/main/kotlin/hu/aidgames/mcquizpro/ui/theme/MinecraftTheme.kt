package hu.aidgames.mcquizpro.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

/** Minecraft-inspirált paletta: kő, fű, föld, deszka és arany, a blokkok bevel-szélével. */
object McColors {
    val Background = Color(0xFF1B1C20)
    val BackgroundTop = Color(0xFF23252A)
    val StoneDarkest = Color(0xFF111215)
    val Stone = Color(0xFF2B2D31)
    val StoneDark = Color(0xFF1F2024)
    val StoneLight = Color(0xFF4A4D55)
    val Panel = Color(0xFF3A3C42)
    val BevelLight = Color(0xFF8A8C92)
    val BevelDark = Color(0xFF111215)
    val Grass = Color(0xFF5DAA3A)
    val GrassDark = Color(0xFF3B7A24)
    val Dirt = Color(0xFF8B5A2B)
    val DirtDark = Color(0xFF5E3B1B)
    val Wood = Color(0xFFA9773F)
    val Gold = Color(0xFFFFD24A)
    val Diamond = Color(0xFF4AEDD9)
    val Redstone = Color(0xFFC0392B)
    val TextPrimary = Color(0xFFF2F2F2)
    val TextSecondary = Color(0xFFB0B3B8)
}

/** Pixeles hangulat a feliratokhoz (beépített monospace betűtípus). */
val PixelFont: FontFamily = FontFamily.Monospace

private val McColorScheme = darkColorScheme(
    primary = McColors.Grass,
    onPrimary = Color.White,
    secondary = McColors.Gold,
    onSecondary = Color.Black,
    tertiary = McColors.Diamond,
    background = McColors.Background,
    onBackground = McColors.TextPrimary,
    surface = McColors.Stone,
    onSurface = McColors.TextPrimary,
    surfaceVariant = McColors.StoneDark,
    onSurfaceVariant = McColors.TextSecondary,
    error = McColors.Redstone,
)

@Composable
fun MinecraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = McColorScheme, content = content)
}

/**
 * Minecraft-os blokk-keret: a [face] háttér, világos él fent-balra, sötét él lent-jobbra,
 * fekete külső kontúr. [raised] = false esetén az élek fordítva vannak (benyomott hatás).
 */
fun Modifier.mcBevel(face: Color, raised: Boolean = true): Modifier = this.drawBehind {
    val edge = 4.dp.toPx()
    val (light, dark) = if (raised) {
        McColors.BevelLight to McColors.BevelDark
    } else {
        McColors.BevelDark to McColors.BevelLight
    }
    drawRect(color = face)
    drawRect(color = light, topLeft = Offset.Zero, size = Size(size.width, edge))
    drawRect(color = light, topLeft = Offset.Zero, size = Size(edge, size.height))
    drawRect(color = dark, topLeft = Offset(0f, size.height - edge), size = Size(size.width, edge))
    drawRect(color = dark, topLeft = Offset(size.width - edge, 0f), size = Size(edge, size.height))
    drawRect(color = Color.Black, style = Stroke(width = 2.dp.toPx()))
}

/** Kőtégla-padló: sötét rács, enyhén változó árnyalatú kőlapokkal. */
@Composable
fun McBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawStoneBricks() },
        content = content,
    )
}

private val StoneShades = listOf(
    Color(0xFF2B2D31),
    Color(0xFF2E3035),
    Color(0xFF292B2F),
    Color(0xFF303237),
)

private fun DrawScope.drawStoneBricks() {
    val tile = 48.dp.toPx()
    val grout = 2.dp.toPx()
    val cols = ceil(size.width / tile).toInt()
    val rows = ceil(size.height / tile).toInt()
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val shade = StoneShades[((r * 73856093) xor (c * 19349663)) and 3]
            val origin = Offset(c * tile, r * tile)
            drawRect(color = shade, topLeft = origin, size = Size(tile, tile))
            drawRect(
                color = McColors.StoneDarkest,
                topLeft = origin,
                size = Size(tile, tile),
                style = Stroke(width = grout),
            )
        }
    }
}

/** Kőpanel: bevel-keret, enyhén áttetsző kőszín, hogy a háttérben lévő effekt is látszódjon. */
@Composable
fun McCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .mcBevel(McColors.Panel.copy(alpha = 0.88f))
            .padding(16.dp),
        content = content,
    )
}

/** Minecraft-gomb: lenyomásra az élek megfordulnak és a felirat lefelé mozdul. */
@Composable
fun McButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = McColors.Grass,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier = modifier
            .mcBevel(color, raised = !pressed)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = Color.White,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(3f, 3f)),
            ),
            modifier = Modifier.padding(top = if (pressed) 3.dp else 0.dp),
        )
    }
}
