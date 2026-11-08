package hu.aidgames.mcquizpro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.aidgames.mcquizpro.data.ScoreEntry
import hu.aidgames.mcquizpro.ui.theme.McCard
import hu.aidgames.mcquizpro.ui.theme.McColors
import hu.aidgames.mcquizpro.ui.theme.PixelFont
import java.text.DateFormat
import java.util.Date

/** Dicsőségfal: a mentett eredmények pontszám szerint rendezve. */
@Composable
fun LeaderboardScreen(scores: List<ScoreEntry>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "DICSŐSÉGFAL",
            color = McColors.Gold,
            fontFamily = PixelFont,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
        )
        Spacer(Modifier.height(12.dp))
        if (scores.isEmpty()) {
            McCard(Modifier.fillMaxWidth()) {
                Text("Még nincs mentett eredmény. Játssz egy kvízt!", color = McColors.TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(scores) { index, entry ->
                    ScoreRow(rank = index + 1, entry = entry)
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(rank: Int, entry: ScoreEntry) {
    val dateText = remember(entry.timestamp) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(entry.timestamp))
    }
    val badgeColor = when (rank) {
        1 -> McColors.Gold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> McColors.StoneLight
    }
    McCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$rank",
                    color = Color.Black,
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Black,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    color = McColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Text(text = dateText, color = McColors.TextSecondary, fontSize = 12.sp)
            }
            Text(
                text = "${entry.score} / ${entry.total}",
                color = McColors.Diamond,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
