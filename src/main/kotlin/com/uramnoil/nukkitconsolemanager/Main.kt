package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.coroutineScope
import java.io.File

suspend fun main(): Unit = coroutineScope {
    val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        inheritIO()
    }
    val server = Nukkit(processBuilder) {
    }
    server.start()
    server.joinWithClose()
}