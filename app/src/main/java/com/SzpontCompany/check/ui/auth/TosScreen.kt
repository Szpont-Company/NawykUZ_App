package com.SzpontCompany.check.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.util.loadLocalizedTos
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText

@Composable
fun TosScreen(
    onAccept: () -> Unit = {},
) {

    val context = LocalContext.current

    val accent = MaterialTheme.colorScheme.primary

    val scrollState = rememberScrollState()

    val isAtBottom by remember {
        derivedStateOf {
            scrollState.value >= scrollState.maxValue
        }
    }

    val markdown = remember {
        loadLocalizedTos(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        LogInHeader(
            accent = accent
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isAtBottom)
                stringResource(R.string.tos_success)
            else
                stringResource(R.string.tos_scroll),
            style = MaterialTheme.typography.bodySmall,
            color = accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        ProvideTextStyle(
            value = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            )
        ) {

            Material3RichText {

                Markdown(markdown)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onAccept,
                enabled = isAtBottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.tos_accept))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}