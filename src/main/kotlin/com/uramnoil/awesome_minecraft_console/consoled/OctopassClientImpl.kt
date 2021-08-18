package com.uramnoil.awesome_minecraft_console.consoled

import awesome_minecraft_console.weaver.WeaverGrpcKt
import awesome_minecraft_console.weaver.WeaverOuterClass

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
            throwable.printStackTrace()
        }

    private val stub = WeaverGrpcKt.WeaverCoroutineStub(channel)

    override fun close() {
        job.complete()
    }

    suspend fun connectConsole() {
        stub.console(lineFlow.map { WeaverOuterClass.Line.newBuilder().setLine(it).build() }).collect {
            mutableCommandFlow.emit(it.command)
        }
    }

    suspend fun connectManagement() {
        stub.management(notificationFlow.map {
            WeaverOuterClass.Notification.newBuilder().setNotification(it).build()
        }).collect {
            when (it.operation) {
                WeaverOuterClass.Operation.Type.OPERATION_START -> mutableOperationSharedFlow.emit(Operation.START)
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