package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }

    val server = Server(processBuilder) {

    }.use {
        it.start()
        it.join()
    }
}