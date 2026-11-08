package hu.aidgames.mcquizpro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.aidgames.mcquizpro.data.ScoreEntry
import hu.aidgames.mcquizpro.ui.theme.McButton
import hu.aidgames.mcquizpro.ui.theme.McCard
import hu.aidgames.mcquizpro.ui.theme.McColors
import hu.aidgames.mcquizpro.ui.theme.PixelFont

/** Végeredmény. Az eredmény már automatikusan mentve van; a név szerkesztése azonnal frissíti a mentést. */
@Composable
fun ResultScreen(
    entry: ScoreEntry,
    timedOut: Boolean,
    onNameChange: (String) -> Unit,
    onPlayAgain: () -> Unit,
    onShowScores: () -> Unit,
) {
    var name by rememberSaveable(entry.timestamp) { mutableStateOf("") }
    val percent = if (entry.total == 0) 0 else entry.score * 100 / entry.total

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (timedOut) "LEJÁRT AZ IDŐ!" else "KVÍZ VÉGE!",
            color = if (timedOut) McColors.Redstone else McColors.Gold,
            fontFamily = PixelFont,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        McCard(Modifier.fillMaxWidth()) {
            Text(
                text = "${entry.score} / ${entry.total}",
                color = McColors.Diamond,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Black,
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Találati arány: $percent%",
                color = McColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(16)
                    onNameChange(name)
                },
                label = { Text("Játékos neve", fontFamily = PixelFont) },
                placeholder = { Text(entry.name, color = McColors.TextSecondary) },
                singleLine = true,
                textStyle = TextStyle(color = McColors.TextPrimary, fontFamily = PixelFont),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = McColors.TextPrimary,
                    unfocusedTextColor = McColors.TextPrimary,
                    focusedContainerColor = McColors.StoneDark,
                    unfocusedContainerColor = McColors.StoneDark,
                    focusedBorderColor = McColors.Diamond,
                    unfocusedBorderColor = McColors.BevelDark,
                    focusedLabelColor = McColors.Diamond,
                    unfocusedLabelColor = McColors.TextSecondary,
                    cursorColor = McColors.Gold,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Az eredmény automatikusan mentésre került.",
                color = McColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(24.dp))
        McButton(
            text = "ÚJ JÁTÉK",
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
            color = McColors.Grass,
        )
        Spacer(Modifier.height(12.dp))
        McButton(
            text = "RANGLISTA",
            onClick = onShowScores,
            modifier = Modifier.fillMaxWidth(),
            color = McColors.Dirt,
        )
    }
}
