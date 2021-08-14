package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class ServerProcessManager(
    private val mutableLineSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(),
    private val commandFlow: Flow<String>,
) : CoroutineScope, Closeable {
    var shouldLoop = true

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Job()

    private val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }

    var _process: ServerProcess? = null

    private fun runServer() = launch {
        assert(_process == null)
        _process = ServerProcess(processBuilder, mutableLineSharedFlow, commandFlow)
        _process!!.start()
        _process!!.await()
        _process = null
    }

    fun start() = launch {
        startRedirectConsoleInput()
        do {
            runServer().join()
        } while (shouldLoop)
        job.cancel()
    }

    suspend fun await() {
        job.join()
    }

    private fun startRedirectConsoleInput() = launch {
        val inputBufferedReader = System.`in`.bufferedReader()
        while (true) {
            val command = withContext(Dispatchers.IO) {
                inputBufferedReader.readLine()
            } ?: continue

            if (command.isAwesomeCommand()) {
                executeAwesomeCommand(command.split(' ').drop(1))
            } else {
                _process?.writeLine(command)
            }
        }
    }

    private fun executeAwesomeCommand(args: List<String>) {
        when (args.getOrNull(0)) {
            null -> {

            }
            "loopoff" -> {
                shouldLoop = false
                println("Server Loop: off")
            }
            "loopon" -> {
                shouldLoop = true
                println("Server Loop: on")
            }
            "forceshutdown" -> close()
        }
    }

    override fun close() {
        _process?.close()
        job.complete()
    }
}

fun String.isAwesomeCommand() = startsWith("awesome")