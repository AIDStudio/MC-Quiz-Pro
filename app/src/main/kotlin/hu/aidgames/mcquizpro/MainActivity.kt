package hu.aidgames.mcquizpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import hu.aidgames.mcquizpro.ui.QuizApp
import hu.aidgames.mcquizpro.ui.theme.MinecraftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinecraftTheme {
                QuizApp()
            }
        }
    }
}
