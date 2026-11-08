package hu.aidgames.mcquizpro.data

import android.content.Context
import org.json.JSONArray

/** Nehézségi szint. A nagyobb szint a könnyebb kérdéseket is tartalmazza, és rövidebb időt ad. */
enum class Difficulty(val label: String, val timeLimitSeconds: Int, val description: String) {
    EASY("KÖNNYŰ", 90, "Csak a könnyű kérdések"),
    MEDIUM("KÖZEPES", 60, "Könnyű és közepes kérdések"),
    HARD("NEHÉZ", 45, "Minden kérdés");

    companion object {
        /** A kulcs (pl. "HARD") alapján keres; ismeretlen vagy hiányzó érték esetén KÖZEPES. */
        fun fromKey(key: String?): Difficulty =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: MEDIUM
    }
}

/**
 * Egy kérdés az assets/questions.json fájlból.
 * [image] a drawable erőforrás neve kiterjesztés nélkül (pl. "mc_creeper"), vagy null.
 * [correct] a helyes opció indexe az [options] listában.
 * [difficulty] a legalacsonyabb szint, amelytől a kérdés szerepel a kvízben.
 */
data class QuizQuestion(
    val question: String,
    val image: String?,
    val options: List<String>,
    val correct: Int,
    val difficulty: Difficulty = Difficulty.MEDIUM,
)

/** Az adott nehézségi szinthez tartozó kérdések. */
fun List<QuizQuestion>.forDifficulty(level: Difficulty): List<QuizQuestion> =
    filter { it.difficulty.ordinal <= level.ordinal }

/** Véletlen sorrendbe keveri az opciókat, és követi a helyes válasz új indexét. */
fun QuizQuestion.withShuffledOptions(): QuizQuestion {
    val order = options.indices.shuffled()
    return copy(
        options = order.map { options[it] },
        correct = order.indexOf(correct),
    )
}

/** Betölti és ellenőrzi a kérdéseket. A hibás bejegyzéseket kihagyja. */
fun loadQuestions(context: Context, fileName: String = "questions.json"): List<QuizQuestion> {
    val raw = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
    val array = JSONArray(raw)
    val result = ArrayList<QuizQuestion>(array.length())
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val optionsJson = obj.optJSONArray("options") ?: continue
        val options = List(optionsJson.length()) { optionsJson.getString(it) }
        val question = obj.optString("question")
        val correct = obj.optInt("correct", -1)
        if (question.isBlank() || options.size < 2 || correct !in options.indices) continue
        val image = obj.optString("image", "").takeIf { it.isNotBlank() }
        val difficulty = Difficulty.fromKey(obj.optString("difficulty", ""))
        result += QuizQuestion(question, image, options, correct, difficulty)
    }
    return result
}
