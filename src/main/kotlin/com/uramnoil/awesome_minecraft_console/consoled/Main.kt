package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    Consoled("127.0.0.1", 50021).use {
        it.start()
        it.await()
    }
}