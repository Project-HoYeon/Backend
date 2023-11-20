package dev.hoyeon.socket

import dev.hoyeon.socket.packet.Packet
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

object PacketHandler: CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default
    val logger = KotlinLogging.logger {  }

    private var eventPublisher: MutableSharedFlow<Packet> =
        MutableSharedFlow(extraBufferCapacity = Channel.UNLIMITED)
    val eventFlow: SharedFlow<Packet>
        get() = eventPublisher.asSharedFlow()

    suspend fun fireEvent(event: Packet) =
        eventPublisher.emit(event)

    fun fireEventWithScope(event: Packet, scope: CoroutineScope = this) =
        scope.launch { eventPublisher.emit(event) }
}

fun packetHandlerScope(): CoroutineScope =
    CoroutineScope(PacketHandler.coroutineContext)

inline fun <reified T: Packet> PacketHandler.on(
    scope: CoroutineScope = this,
    noinline callback: suspend T.() -> Unit
): Job = eventFlow
    .buffer(Channel.UNLIMITED)
    .filterIsInstance<T>()
    .onEach { packet ->
        scope.launch(packet.coroutineContext) {
            runCatching {
                callback(packet)
            }.onFailure { logger.error(it) { "Failed to handle event: " } }
        }
    }
    .launchIn(scope)