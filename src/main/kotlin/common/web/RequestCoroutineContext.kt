package example.common.web

import kotlinx.coroutines.ThreadContextElement
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine context element that sets and clears the [RequestAttributes] in [RequestContextHolder]
 * for the duration of a coroutine.
 */
class RequestCoroutineContext(
  private val requestAttributes: RequestAttributes? = RequestContextHolder.getRequestAttributes(),
) : ThreadContextElement<RequestAttributes?> {
  companion object Key : CoroutineContext.Key<RequestCoroutineContext>

  override val key: CoroutineContext.Key<RequestCoroutineContext> get() = Key

  override fun updateThreadContext(context: CoroutineContext): RequestAttributes? {
    val previousRequestAttributes = RequestContextHolder.getRequestAttributes()

    RequestContextHolder.setRequestAttributes(requestAttributes)

    return previousRequestAttributes
  }

  override fun restoreThreadContext(
    context: CoroutineContext,
    oldState: RequestAttributes?,
  ) {
    if (oldState == null) {
      RequestContextHolder.resetRequestAttributes()
    } else {
      RequestContextHolder.setRequestAttributes(oldState)
    }
  }
}
