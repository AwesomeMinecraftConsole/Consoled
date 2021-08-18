package com.uramnoil.awesome_minecraft_console.consoled

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class Consoled(host: String, port: Int) : Closeable, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + serverProcessManager.coroutineContext

    private val mutableSharedCommandFlow = MutableSharedFlow<String>()
    private val mutableSharedLineFlow = MutableSharedFlow<String>()
    private val mutableOperationFlow = MutableSharedFlow<Operation>()
    private val mutableNotificationFlow = MutableSharedFlow<String>()

    private val serverProcessManager: ServerProcessManager =
        ServerProcessManager(mutableSharedLineFlow, mutableSharedCommandFlow, shouldLoop = false)

    private val octopassClient = OctopassClientImpl(
        ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .keepAliveTime(1000, TimeUnit.MILLISECONDS)
            .keepAliveTimeout(5000, TimeUnit.MILLISECONDS)
            .build(),
        mutableSharedLineFlow,
        mutableSharedCommandFlow,
        mutableNotificationFlow,
        mutableOperationFlow
    )

    fun start() {
        octopassClient.start()
        serverProcessManager.start()
        launch {
            mutableSharedLineFlow.collect {
                println(it)
            }
        }
        startRedirectConsoleInput()
    }

    suspend fun await() {
        serverProcessManager.await()
    }

    override fun close() {
        serverProcessManager.close()
        octopassClient.close()
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
                mutableSharedCommandFlow.emit(command)
            }
        }
    }

    private fun executeAwesomeCommand(args: List<String>) {
        when (args.getOrNull(0)) {
            null -> {

            }
            "loopoff" -> {
                serverProcessManager.shouldLoop = false
                println("Server Loop: off")
            }
            "loopon" -> {
                serverProcessManager.shouldLoop = true
                println("Server Loop: on")
            }
            "forceshutdown" -> close()
            "connect" -> {

            }
        }
    }
}


fun String.isAwesomeCommand() = startsWith("awesome")