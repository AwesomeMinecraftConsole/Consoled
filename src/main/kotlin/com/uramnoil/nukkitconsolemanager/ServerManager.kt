package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ServerManager() : CoroutineScope {
    val canLoop = true

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Job()

    private val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }

    private var server: Server? = null

    private suspend fun runServer() {
        assert(server == null)
        server = Server(processBuilder)
        server!!.start()
        server!!.join()
        server!!.close()
        server = null
    }

    fun launchOnce() = launch {
        runServer()
    }

    fun launchLoop() = launch {
        while (canLoop) {
            runServer()
            delay(3000)
        }
    }

    suspend fun join() {
        job.join()
    }

    fun startRedirectConsoleInput() = launch {
        val inputBufferedReader = System.`in`.bufferedReader()
        while (true) {
            val message = withContext(Dispatchers.IO) {
                inputBufferedReader.readLine()
            } ?: continue
            server?.writeLine(message)
        }
    }
}