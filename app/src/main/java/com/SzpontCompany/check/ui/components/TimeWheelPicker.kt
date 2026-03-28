package com.SzpontCompany.check.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    val infinitePageCount = Int.MAX_VALUE
    val centerPage = infinitePageCount / 2

    val startHourPage = centerPage - (centerPage % hours.size) + initialHour
    val startMinutePage = centerPage - (centerPage % minutes.size) + initialMinute

    val hoursPagerState = rememberPagerState(
        initialPage = startHourPage,
        pageCount = { infinitePageCount }
    )
    val minutesPagerState = rememberPagerState(
        initialPage = startMinutePage,
        pageCount = { infinitePageCount }
    )

    LaunchedEffect(hoursPagerState.currentPage, minutesPagerState.currentPage) {
        val selectedHour = hoursPagerState.currentPage % hours.size
        val selectedMinute = minutesPagerState.currentPage % minutes.size
        onTimeSelected(selectedHour, selectedMinute)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            VerticalWheelPicker(
                state = hoursPagerState,
                items = hours.map { String.format(Locale.getDefault(), "%02d", it) },
                modifier = Modifier.width(80.dp)
            )
        }

        Text(
            text = ":",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            VerticalWheelPicker(
                state = minutesPagerState,
                items = minutes.map { String.format(Locale.getDefault(), "%02d", it) },
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalWheelPicker(
    state: androidx.compose.foundation.pager.PagerState,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )

        VerticalPager(
            state = state,
            pageSize = PageSize.Fixed(56.dp),
            flingBehavior = PagerDefaults.flingBehavior(
                state = state,
                pagerSnapDistance = PagerSnapDistance.atMost(60)
            ),
            contentPadding = PaddingValues(vertical = 72.dp),
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val pageOffset = (state.currentPage - page + state.currentPageOffsetFraction)
            val alpha = 1f - (Math.min(Math.abs(pageOffset), 1f) * 0.6f)
            val scale = 1f - (Math.min(Math.abs(pageOffset), 1f) * 0.2f)

            val isSelected = page == state.currentPage
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

            val actualIndex = page % items.size

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                        this.scaleX = scale
                        this.scaleY = scale
                    }

                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (!isSelected) {
                                coroutineScope.launch {
                                    state.animateScrollToPage(page)
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = items[actualIndex],
                    color = color,
                    fontSize = 32.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}