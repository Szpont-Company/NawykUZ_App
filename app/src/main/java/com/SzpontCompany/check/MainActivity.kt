package com.SzpontCompany.check

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import com.SzpontCompany.check.ui.auth.LoginScreen
import com.SzpontCompany.check.ui.theme.Cactus
import com.SzpontCompany.check.ui.theme.CheckTheme


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


            CheckTheme(darkTheme = darkTheme, accent = Cactus) {
                LoginScreen()
            }
        }
    }
}