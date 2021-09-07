package com.uramnoil.awesome_minecraft_console.consoled

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class Weaver(host: String, port: Int) : Closeable, CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {
    private val mutableSharedCommandFlow = MutableSharedFlow<String>()
    private val mutableSharedLineFlow = MutableSharedFlow<String>()
    private val mutableOperationFlow = MutableSharedFlow<Operation>()
    private val mutableNotificationFlow = MutableSharedFlow<String>()

    private val serverProcessManager: ServerProcessManager =
        ServerProcessManager(mutableSharedLineFlow, mutableSharedCommandFlow, shouldLoop = false)

    private val weaverClient = WeaverClient(
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
        launch(CoroutineExceptionHandler { _, throwable ->
        }) {
            weaverClient.connectConsole()
            weaverClient.connectManagement()
        }
        serverProcessManager.start()
        launch {
            mutableSharedLineFlow.collect {
                println(it)
            }
            mutableOperationFlow.collect {
                when (it) {
                    Operation.START -> serverProcessManager.start()
                }
            }
        }
        startRedirectConsoleInput()
    }

    suspend fun await() {
        serverProcessManager.await()
    }

    override fun close() {
        serverProcessManager.close()
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