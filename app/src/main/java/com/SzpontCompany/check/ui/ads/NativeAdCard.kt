package com.SzpontCompany.check.ui.ads

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.SzpontCompany.check.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    val titleColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    val badgeBgColor = MaterialTheme.colorScheme.secondaryContainer.toArgb()
    val badgeTextColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    LaunchedEffect(Unit) {
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110") // Testowe ID dla reklam natywnych
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (nativeAd != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val inflater = LayoutInflater.from(ctx)
                    val adView = inflater.inflate(R.layout.native_ad_habit_card, null) as NativeAdView

                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    val badgeView = adView.findViewById<TextView>(R.id.ad_badge)
                    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
                    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)

                    headlineView.setTextColor(titleColor)
                    bodyView.setTextColor(bodyColor)

                    badgeView.setTextColor(badgeTextColor)
                    badgeView.backgroundTintList = android.content.res.ColorStateList.valueOf(badgeBgColor)

                    ctaView.setTextColor(onPrimaryColor)
                    ctaView.backgroundTintList = android.content.res.ColorStateList.valueOf(primaryColor)

                    headlineView.text = nativeAd?.headline
                    adView.headlineView = headlineView

                    if (nativeAd?.body == null) {
                        bodyView.visibility = View.INVISIBLE
                    } else {
                        bodyView.visibility = View.VISIBLE
                        bodyView.text = nativeAd?.body
                    }
                    adView.bodyView = bodyView

                    if (nativeAd?.icon == null) {
                        iconView.visibility = View.GONE
                    } else {
                        iconView.setImageDrawable(nativeAd?.icon?.drawable)
                        iconView.visibility = View.VISIBLE
                    }
                    adView.iconView = iconView

                    if (nativeAd?.callToAction == null) {
                        ctaView.visibility = View.INVISIBLE
                    } else {
                        ctaView.visibility = View.VISIBLE
                        ctaView.text = nativeAd?.callToAction
                    }
                    adView.callToActionView = ctaView

                    adView.setNativeAd(nativeAd!!)
                    adView
                }
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.ad_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}