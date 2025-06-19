package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LazyColumnWithScrollbar(
    modifier:Modifier = Modifier,
    lazyListState:LazyListState = rememberLazyListState(),
    content:LazyListScope.()->Unit,
)
{
    Box(
        modifier = modifier,
    )
    {
        LazyColumn(
            state = lazyListState,content = content,
        )

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(lazyListState)
        )
    }
}
