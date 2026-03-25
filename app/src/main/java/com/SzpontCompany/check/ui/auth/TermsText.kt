package com.SzpontCompany.check.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.SzpontCompany.check.R

enum class TermsType {
    SIGN_UP,
    LOGIN
}

@Composable
fun TermsText(
    onTosClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    type: TermsType
) {
    val annotatedString = buildAnnotatedString {
        val firstPart = when(type) {
            TermsType.SIGN_UP -> stringResource(R.string.register_notice1)
            TermsType.LOGIN -> stringResource(R.string.login_privacy_1)
        }
        append(firstPart)
        append(" ")
        withLink(LinkAnnotation.Clickable("tos") { onTosClick() }) {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(stringResource(R.string.login_privacy_2))
            }
        }
        append(" ")
        append(stringResource(R.string.login_privacy_3))
        append(" ")
        withLink(LinkAnnotation.Clickable("privacy") { onPrivacyClick() }) {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(stringResource(R.string.login_privacy_4))
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text=annotatedString,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

}