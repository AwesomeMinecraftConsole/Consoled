package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    val serverManager = ServerManager()
    serverManager.startRedirectConsoleInput()
    serverManager.launchLoop()
    serverManager.join()
}