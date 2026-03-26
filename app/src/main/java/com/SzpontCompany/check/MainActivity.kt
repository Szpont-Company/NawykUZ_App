package com.SzpontCompany.check

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.SzpontCompany.check.ui.auth.LoginScreen
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.auth.AnimatedSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.SzpontCompany.check.ui.profile.ProfileScreen
import com.SzpontCompany.check.ui.settings.SettingsScreen
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.ui.theme.Mint


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme = true
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            var showSplash by remember { mutableStateOf(true) }

            CheckTheme(darkTheme = darkTheme, accent = Mint) {
                if (showSplash) {
                    AnimatedSplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else {
                    gitLoginScreen()
                }
            }
        }
    }
}