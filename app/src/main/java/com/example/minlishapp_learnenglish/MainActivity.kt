package com.example.minlishapp_learnenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.minlishapp_learnenglish.core.AppContainer
import com.example.minlishapp_learnenglish.ui.MinLishApp
import com.example.minlishapp_learnenglish.ui.theme.MinLishAppLearnEnglishTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinLishAppLearnEnglishTheme {
                MinLishApp(appContainer = appContainer)
            }
        }
    }
}
