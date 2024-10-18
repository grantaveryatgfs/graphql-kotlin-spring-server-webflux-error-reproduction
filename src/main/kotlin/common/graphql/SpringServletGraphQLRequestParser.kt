package example.common.graphql

import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLRequestParser
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.body

internal const val REQUEST_PARAM_QUERY = "query"
internal const val REQUEST_PARAM_OPERATION_NAME = "operationName"
internal const val REQUEST_PARAM_VARIABLES = "variables"
internal val graphQLMediaType = MediaType("application", "graphql")

/**
 * Implementation of [SpringGraphQLRequestParser] using servlet [ServerRequest].
 */
open class SpringServletGraphQLRequestParser(
  private val objectMapper: ObjectMapper,
) : GraphQLRequestParser<ServerRequest> {
  private val mapTypeReference: MapType =
    TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

  override suspend fun parseRequest(request: ServerRequest): GraphQLServerRequest? =
    when {
      request.param(REQUEST_PARAM_QUERY).isPresent -> {
        getRequestFromGet(request)
      }
      request.method() == HttpMethod.POST -> {
        getRequestFromPost(request)
      }
      else -> null
    }

  private fun getRequestFromGet(serverRequest: ServerRequest): GraphQLServerRequest {
    val query = serverRequest.param(REQUEST_PARAM_QUERY).get()
    val operationName: String? = serverRequest.param(REQUEST_PARAM_OPERATION_NAME).orElseGet { null }
    val variables: String? = serverRequest.param(REQUEST_PARAM_VARIABLES).orElseGet { null }
    val graphQLVariables: Map<String, Any>? =
      variables?.let {
        objectMapper.readValue(it, mapTypeReference)
      }

    return GraphQLRequest(query = query, operationName = operationName, variables = graphQLVariables)
  }

  private fun getRequestFromPost(serverRequest: ServerRequest): GraphQLServerRequest? {
    val contentType = serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON)
    return when {
      contentType.includes(MediaType.APPLICATION_JSON) -> serverRequest.body<GraphQLServerRequest>()
      contentType.includes(graphQLMediaType) -> GraphQLRequest(query = serverRequest.body())
      else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Content-Type is not specified")
    }
  }
}
