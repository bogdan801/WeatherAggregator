package com.bogdan801.weatheraggregator.presentation.composables.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AdaptivePager(
    modifier: Modifier = Modifier,
    count: Int,
    state: PagerState,
    isHorizontal: Boolean = true,
    content: @Composable (PagerScope.(page: Int) -> Unit)
) {
    if(isHorizontal) {
        HorizontalPager(
            count = count,
            modifier = modifier,
            state = state,
            content = content
        )
    }
    else {
        VerticalPager(
            count = count,
            modifier = modifier,
            state = state,
            content = content
        )
    }
}