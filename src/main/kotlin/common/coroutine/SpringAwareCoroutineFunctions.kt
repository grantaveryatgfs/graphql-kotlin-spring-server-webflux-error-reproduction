package example.common.coroutine

import example.common.web.RequestCoroutineContext
import example.springApplicationContext
import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a [CoroutineContext] that propagates Spring-centric contexts and uses a virtual thread executor.
 */
fun createSpringAwareCoroutineContext(
  excludeRequest: Boolean = false,
  excludeMDC: Boolean = false,
  excludeObservation: Boolean = false,
): CoroutineContext =
// Use Spring virtual thread dispatcher.
  // Otherwise, calls like Thread.sleep() will block the thread.
  Dispatchers.SpringAwareVirtual +
    // Ensure security context is propagated.
    (RequestCoroutineContext().takeUnless { excludeRequest } ?: EmptyCoroutineContext) +
    // Ensure logging MDC is propagated.
    (MDCContext().takeUnless { excludeMDC } ?: EmptyCoroutineContext) +
    // Propagate the current observation (if available).
    (
      springApplicationContext
        ?.getBean(ObservationRegistry::class.java)
        ?.asContextElement()
        .takeUnless { excludeObservation }
        ?: EmptyCoroutineContext
    )

/**
 * Run a coroutine with the Spring application context on the virtual thread executor.
 *
 * Propagates Spring-centric contexts, like security, logging MDC, and observation.
 *
 * NOTE: This function is only necessary to bridge into suspendable code from blocking code.
 * Blocking in this case means that the code execution can **block** (aka pause) the original stream of work
 * _interruptibly_, as needed, until its completion. The underlying thread is not blocked because
 * [runSpringAwareCoroutine] uses a virtual thread-based coroutine dispatcher.
 */
fun <T> runSpringAwareCoroutine(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> T,
): T {
  val combinedContext = createSpringAwareCoroutineContext() + context

  return runBlocking(combinedContext) {
    block()
  }
}
