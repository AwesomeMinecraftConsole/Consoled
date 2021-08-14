package com.uramnoil.awesome_minecraft_console.consoled

import com.uramnoil.awesome_minecraft_console.protocols.ConsoleGrpcKt
import com.uramnoil.awesome_minecraft_console.protocols.ConsoleOuterClass
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class OctopassClientImpl(
    channel: ManagedChannel,
    private val lineFlow: Flow<String>,
    private val mutableCommandFlow: MutableSharedFlow<String> = MutableSharedFlow(),
    private val notificationFlow: Flow<String>,
    private val mutableOperationSharedFlow: MutableSharedFlow<Operation> = MutableSharedFlow()
) : Closeable, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + CoroutineExceptionHandler { coroutineContext, throwable ->
            job.complete()
        }

    private val stub = ConsoleGrpcKt.ConsoleCoroutineStub(channel)

    override fun close() {
        job.complete()
    }

    suspend fun connectConsole() {
        stub.console(lineFlow.map { ConsoleOuterClass.LineRequest.newBuilder().setLine(it).build() }).collect {
            mutableCommandFlow.emit(it.command)
        }
    }

    suspend fun connectManagement() {
        stub.management(notificationFlow.map {
            ConsoleOuterClass.NotificationRequest.newBuilder().setMessage(it).build()
        }).collect {
            when (it.typeCase.number) {
                1 -> mutableOperationSharedFlow.emit(Operation.START)
                else -> {
                }
            }
        }
    }

    fun start() {
        launch {
            connectConsole()
            connectManagement()
        }
    }
}