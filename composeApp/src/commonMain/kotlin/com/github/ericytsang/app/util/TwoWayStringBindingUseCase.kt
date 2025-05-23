package com.github.ericytsang.app.util

import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.KotlinDependencyProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class TwoWayStringBindingUseCase(
    uiScope:CoroutineScope,
    remoteStringFlow:Flow<String>,
    updateRemoteString:suspend (String) -> Unit,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
    private val nowFactory:NowFactory = NowFactoryImpl,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private val remoteStringUpdateRequests = Channel<String>(capacity = Channel.Factory.CONFLATED).also { channel ->
        channel
            .consumeAsFlow()
            .conflate()
            .onEach { newString -> updateRemoteString(newString) }
            .flowOn(dispatchers.io)
            .launchIn(uiScope)
    }

    private val twoWayStringBinding = MutableStateFlow(TwoWayStringBinding())

    private val collectToGetUpdatesFromRemoteSource = remoteStringFlow
        .conflate()
        .onEach { newString -> updateLocalStringViaDb(newString) }
        .shareIn(applicationScope, SharingStarted.Companion.WhileSubscribed(5.seconds), replay = 0)

    /**
     * combines the in-memory string and the remote string to get the latest string for displaying to the user.
     */
    val displayString:Flow<String> = twoWayStringBinding
        .combine(collectToGetUpdatesFromRemoteSource) { it,_ -> it }
        .map { twoWayBindingDbString -> twoWayBindingDbString.displayString }

    /**
     * updates the in-memory string and also asynchronously updates the remote string to the new string.
     */
    fun updateString(delimiterCharacters:String)
    {
        updateLocalStringViaUi(delimiterCharacters)
        enqueueRemoteStringUpdate(delimiterCharacters)
    }

    private fun enqueueRemoteStringUpdate(newString:String)
    {
        remoteStringUpdateRequests.trySend(newString).getOrThrow()
    }

    private fun updateLocalStringViaDb(newString:String)
    {
        println("updateLocalStringViaDb($newString)")
        twoWayStringBinding.value = twoWayStringBinding.value.updateViaRemote(newString, nowFactory.now())
    }

    private fun updateLocalStringViaUi(newString:String)
    {
        println("updateLocalStringViaUi($newString)")
        twoWayStringBinding.value = twoWayStringBinding.value.updateViaUi(newString, nowFactory.now())
    }

    private sealed class FetchedString
    {
        @OptIn(ExperimentalTime::class)
        data class Fetched(
            val string:String,
            val whenUpdated:Instant,
        ):FetchedString()

        data object Loading:FetchedString()
    }

    private data class TwoWayStringBinding(
        val remoteString:FetchedString = FetchedString.Loading,
        val memoryString:FetchedString = FetchedString.Loading,
    )
    {
        fun updateViaUi(
            newString:String,
            now:Instant,
        ):TwoWayStringBinding = copy(
            memoryString = FetchedString.Fetched(
                string = newString,
                whenUpdated = now,
            ),
        )

        fun updateViaRemote(
            newString:String,
            now:Instant,
        ):TwoWayStringBinding = copy(
            remoteString = FetchedString.Fetched(
                string = newString,
                whenUpdated = now,
            ),
        )

        val displayString get() = when (memoryString)
        {
            FetchedString.Loading -> remoteString.displayString
            is FetchedString.Fetched -> when (remoteString)
            {
                FetchedString.Loading -> "Loading..."
                is FetchedString.Fetched -> when (memoryString.whenUpdated > remoteString.whenUpdated)
                {
                    true -> memoryString.string
                    false -> remoteString.string
                }
            }
        }

        private val FetchedString.displayString get() = when (remoteString)
        {
            is FetchedString.Fetched -> remoteString.string
            FetchedString.Loading -> "Loading..."
        }
    }
}