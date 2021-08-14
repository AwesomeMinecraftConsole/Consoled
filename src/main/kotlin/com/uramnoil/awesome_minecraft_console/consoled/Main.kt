package com.uramnoil.awesome_minecraft_console.consoled

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    Consoled().use {
        it.start()
        it.await()
    }
}