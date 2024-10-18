package example.common.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.core.task.VirtualThreadTaskExecutor
import java.util.concurrent.Executors

/**
 * A [CoroutineDispatcher] that uses Java's virtual thread executor.
 */
val Dispatchers.Virtual: CoroutineDispatcher
  get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

/**
 * A [CoroutineDispatcher] that uses Spring's virtual thread executor.
 */
val Dispatchers.SpringAwareVirtual: CoroutineDispatcher
  get() =
    VirtualThreadTaskExecutor("virtual-")
      .asCoroutineDispatcher()
