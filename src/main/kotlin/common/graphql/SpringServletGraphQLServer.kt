package example.common.graphql

import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.execution.GraphQLServer
import org.springframework.web.servlet.function.ServerRequest

/**
 * Implementation of [GraphQLServer] using servlet [ServerRequest].
 */
open class SpringServletGraphQLServer(
  requestParser: SpringServletGraphQLRequestParser,
  contextFactory: SpringServletGraphQLContextFactory,
  requestHandler: GraphQLRequestHandler,
) : GraphQLServer<ServerRequest>(
    requestParser,
    contextFactory,
    requestHandler,
  )
