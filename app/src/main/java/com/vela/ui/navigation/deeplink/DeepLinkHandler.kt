package com.vela.ui.navigation.deeplink

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Singleton
class DeepLinkHandler @Inject constructor() {

    private val channel = Channel<DeepLink>(Channel.BUFFERED)
    val flow: Flow<DeepLink> = channel.receiveAsFlow()

    fun emit(deepLink: DeepLink) {
        channel.trySend(deepLink)
    }
}
