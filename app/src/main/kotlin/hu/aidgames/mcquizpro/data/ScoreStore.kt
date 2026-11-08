package hu.aidgames.mcquizpro.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

const val DEFAULT_PLAYER_NAME = "Névtelen"
private const val MAX_NAME_LENGTH = 16
private const val PREFS = "mc_quiz_scores"
private const val KEY_SCORES = "scores"

/** Egy mentett eredmény. [timestamp] azonosítja a bejegyzést, így a név később is frissíthető. */
data class ScoreEntry(
    val name: String,
    val score: Int,
    val total: Int,
    val timestamp: Long,
)

/** Helyi tárolás SharedPreferences-ben, JSON tömbként. */
class ScoreStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Eredmények pontszám szerint csökkenően (döntetlennél a korábbi nyer). */
    fun load(): List<ScoreEntry> {
        val raw = prefs.getString(KEY_SCORES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length())
                .map { i ->
                    val o = array.getJSONObject(i)
                    ScoreEntry(
                        name = o.optString("name", DEFAULT_PLAYER_NAME),
                        score = o.getInt("score"),
                        total = o.getInt("total"),
                        timestamp = o.getLong("timestamp"),
                    )
                }
                .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenBy { it.timestamp })
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Beszúrja, vagy ha a [ScoreEntry.timestamp] már létezik, lecseréli az adott bejegyzést. */
    fun upsert(entry: ScoreEntry) {
        val clean = entry.copy(
            name = entry.name.trim().ifBlank { DEFAULT_PLAYER_NAME }.take(MAX_NAME_LENGTH),
        )
        val list = load().filterNot { it.timestamp == clean.timestamp } + clean
        val array = JSONArray()
        list.forEach { e ->
            array.put(
                JSONObject()
                    .put("name", e.name)
                    .put("score", e.score)
                    .put("total", e.total)
                    .put("timestamp", e.timestamp),
            )
        }
        prefs.edit().putString(KEY_SCORES, array.toString()).apply()
    }
}
