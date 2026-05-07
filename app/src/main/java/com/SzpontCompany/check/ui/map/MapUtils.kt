package com.SzpontCompany.check.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.SzpontCompany.check.ui.theme.getColorByName
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Funkcja zamieniająca DP na Pixele
fun dpToPx(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}

// Funkcja dynamicznie tworząca BitmapDescriptor (awatar)
fun createAvatarMarker(context: Context, name: String, emoji: String, bgColorName: String): BitmapDescriptor {
    val sizeDp = 48f
    val sizePx = dpToPx(context, sizeDp).toInt()

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val color = getColorByName(bgColorName)
    val colorArgb = color.toArgb()

    // Rysujemy tło kółka
    val circlePaint = Paint().apply {
        isAntiAlias = true
        this.color = colorArgb
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, circlePaint)

    // Rysujemy białą obwódkę wokół kółka
    val borderPaint = Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(context, 2f)
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - borderPaint.strokeWidth / 2f, borderPaint)

    // Logika wyboru: własne Emoji czy Inicjały
    val isEmojiValid = emoji.isNotEmpty() && emoji != "👤"

    val textToDraw = if (isEmojiValid) {
        emoji
    } else {
        // Generowanie inicjałów z nazwy
        name.trim().split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    // Ustawienia pędzla dla tekstu
    val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        if (isEmojiValid) {
            textSize = dpToPx(context, 26f) // Rozmiar dla Emoji
        } else {
            textSize = dpToPx(context, 18f) // Rozmiar dla Inicjałów
            this.color = android.graphics.Color.WHITE // Biały kolor liter
            typeface = Typeface.DEFAULT_BOLD // Pogrubienie
        }
    }

    // Wyśrodkowanie tekstu w pionie i poziomie
    val xPos = canvas.width / 2f
    val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(textToDraw, xPos, yPos, textPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}