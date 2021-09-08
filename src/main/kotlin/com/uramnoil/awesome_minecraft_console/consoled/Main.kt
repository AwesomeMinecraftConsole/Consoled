package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    Weaver(System.getenv("WEAVER_SERVER_HOST"), System.getenv("WEAVER_SERVER_PORT").toInt()).use {
        it.start()
        it.await()
    }
}