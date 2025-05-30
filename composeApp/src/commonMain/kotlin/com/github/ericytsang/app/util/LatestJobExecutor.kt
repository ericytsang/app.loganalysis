package com.github.ericytsang.app.util

import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

class LatestJobExecutor(
    scope:ImmutableCoroutineScope,
    coroutineDispatcher:CoroutineDispatcher,
)
{
    private val jobChannel = Channel<suspend ()->Unit>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init
    {
        scope.launch(coroutineDispatcher)
        {
            for (job in jobChannel)
            {
                job() // Execute the latest job
            }
        }
    }

    fun submit(job:suspend ()->Unit)
    {
        jobChannel.trySend(job).getOrThrow()
    }
}