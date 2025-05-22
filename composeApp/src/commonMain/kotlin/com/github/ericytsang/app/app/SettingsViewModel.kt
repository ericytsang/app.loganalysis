package com.github.ericytsang.app.app

import com.github.ericytsang.domain.repo.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.SettingsRepository
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

interface SettingsViewModel:MutableThemeViewModel
{
    val delimiterCharacters:Flow<String>
    fun setDelimiterCharacters(delimiterCharacters:String)

    companion object
    {
        fun create(
            uiScope:CoroutineScope,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
            settingsRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
        ):SettingsViewModel = SettingsViewModelImpl(
            uiScope = uiScope,
            kotlinDependencyProvider = kotlinDependencyProvider,
            settingsRepository = settingsRepository,
        )
    }
}

private class SettingsViewModelImpl(
    uiScope:CoroutineScope,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
    private val settingsRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
    private val nowFactory:NowFactory = NowFactoryImpl,
):SettingsViewModel,
    MutableThemeViewModel by MutableThemeViewModel.create(),
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val updateRequests = Channel<String>(capacity = Channel.CONFLATED).also { channel ->
        channel
            .consumeAsFlow()
            .conflate()
            .onEach { newString -> settingsRepository.setDelimiter(newString) }
            .flowOn(dispatchers.io)
            .launchIn(uiScope)
    }

    private val _delimiterCharacters = MutableStateFlow(TwoWayBindingString())

    private val delimiterCharactersFromDb = settingsRepository
        .getDelimiterFlow()
        .conflate()
        .onEach { newString -> updateInMemoryDelimiterCharactersViaDb(newString) }
        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5.seconds), replay = 0)

    override val delimiterCharacters:Flow<String> = _delimiterCharacters
        .combine(delimiterCharactersFromDb) { it,_ -> it }
        .map { twoWayBindingDbString -> twoWayBindingDbString.displayString }

    override fun setDelimiterCharacters(delimiterCharacters:String)
    {
        updateInMemoryDelimiterCharactersViaUi(delimiterCharacters)
        updateDbDelimiterCharacters(delimiterCharacters)
    }

    private fun updateDbDelimiterCharacters(newString:String)
    {
        updateRequests.trySend(newString).getOrThrow()
    }

    private fun updateInMemoryDelimiterCharactersViaDb(newString:String)
    {
        println("update via db: $newString")
        _delimiterCharacters.value = _delimiterCharacters.value.updateViaRemote(newString, nowFactory.now())
    }

    private fun updateInMemoryDelimiterCharactersViaUi(newString:String)
    {
        println("update via ui: $newString")
        _delimiterCharacters.value = _delimiterCharacters.value.updateViaUi(newString, nowFactory.now())
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

    private data class TwoWayBindingString(
        val remoteString:FetchedString = FetchedString.Loading,
        val memoryString:FetchedString = FetchedString.Loading,
    )
    {
        fun updateViaUi(
            newString:String,
            now:Instant,
        ):TwoWayBindingString = copy(
            memoryString = FetchedString.Fetched(
                string = newString,
                whenUpdated = now,
            ),
        )

        fun updateViaRemote(
            newString:String,
            now:Instant,
        ):TwoWayBindingString = copy(
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
