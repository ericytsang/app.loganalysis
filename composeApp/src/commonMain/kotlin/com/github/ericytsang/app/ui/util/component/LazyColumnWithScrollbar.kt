package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LazyColumnWithScrollbar(
    modifier:Modifier = Modifier,
    enableHorizontalScroll:Boolean,
    lazyListState:LazyListState = rememberLazyListState(),
    horizontalScrollState:ScrollState = rememberScrollState(),
    content:LazyListScope.()->Unit,
)
{
    Box(
        modifier = modifier,
    )
    {
        val modifier = if (enableHorizontalScroll) Modifier.horizontalScroll(horizontalScrollState) else Modifier

        LazyColumn(
            modifier = modifier.matchParentSize(),
            state = lazyListState,
            content = content,
        )

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(lazyListState)
        )

        if (enableHorizontalScroll)
        {
            HorizontalScrollbar(
                modifier = Modifier.align(Alignment.BottomStart),
                adapter = rememberScrollbarAdapter(horizontalScrollState)
            )
        }
    }
}
