package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import kotlin.coroutines.CoroutineContext


val file = File("/Users/UramnOIL/IdeaProjects/NukkitConsoleManager/nukkit/np")
val bufferedReader = file.bufferedReader()

suspend fun main(): Unit = coroutineScope {
    val console = NukkitConsoleReader(bufferedReader)
    val server = Server(console)
    server.start()
    server.join()
}

class Server(private val nukkitConsoleReader: NukkitConsoleReader) : CoroutineScope {
    private val job = SupervisorJob()
    private var childJobs = listOf<Job>()
    override val coroutineContext: CoroutineContext
        get() = job

    fun start() {
        childJobs = listOf(startShowLines(), startApiServer())
    }

    private fun startShowLines() = launch {
        while (true) {
            val message = nukkitConsoleReader.readLine() ?: continue
            if(message.trim() == ">") continue

            println(message)
        }
    }

    private fun startApiServer() = launch {

    }

    fun shutdown() {
        job.cancel()
    }

    suspend fun join() {
        childJobs.joinAll()
    }
}

class NukkitConsoleReader(private val bufferedReader: BufferedReader) {
    suspend fun readLine() = withContext(Dispatchers.IO) {
        bufferedReader.readLine()
    }
}