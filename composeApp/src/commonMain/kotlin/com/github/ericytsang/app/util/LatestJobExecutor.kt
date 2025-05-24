package com.github.ericytsang.app.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class LatestJobExecutor(
    scope:CoroutineScope,
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