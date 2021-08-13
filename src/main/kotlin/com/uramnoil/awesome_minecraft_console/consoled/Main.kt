package com.uramnoil.awesome_minecraft_console.consoled

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051).build()
    Consoled(channel).use {
        it.start()
        it.await()
    }
}