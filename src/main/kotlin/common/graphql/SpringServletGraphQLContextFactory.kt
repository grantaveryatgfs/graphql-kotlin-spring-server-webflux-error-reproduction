package example.common.graphql

import com.apollographql.federation.graphqljava.tracing.FederatedTracingInstrumentation.FEDERATED_TRACING_HEADER_NAME
import com.expediagroup.graphql.generator.extensions.toGraphQLContext
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import example.common.coroutine.createSpringAwareCoroutineContext
import graphql.GraphQLContext
import org.springframework.web.servlet.function.ServerRequest
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of [GraphQLContextFactory] using servlet [ServerRequest].
 */
open class SpringServletGraphQLContextFactory : GraphQLContextFactory<ServerRequest> {
  override suspend fun generateContext(request: ServerRequest): GraphQLContext =
    mutableMapOf<Any, Any>().also { map ->
      request.headers().firstHeader(FEDERATED_TRACING_HEADER_NAME)?.let { headerValue ->
        map[FEDERATED_TRACING_HEADER_NAME] = headerValue
      }

      // Configure default coroutine context used for executing a GraphQL request.
      // This ensures data fetchers use our preferred virtual thread dispatcher and that important
      // Spring contexts like security are passed through.
      map[CoroutineContext::class] = createSpringAwareCoroutineContext()
    }
      .toGraphQLContext()
}
