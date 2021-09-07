package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    Weaver("127.0.0.1", 50052).use {
        it.start()
        it.await()
    }
}