package example.common.graphql

import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.extensions.getOrDefault
import com.expediagroup.graphql.generator.extensions.print
import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.spring.GraphQLConfigurationProperties
import com.expediagroup.graphql.server.spring.GraphQLRoutesConfiguration
import com.expediagroup.graphql.server.spring.PlaygroundRouteConfiguration
import com.expediagroup.graphql.server.spring.SdlRouteConfiguration
import com.expediagroup.graphql.server.spring.execution.SpringDataFetcher
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLRequestParser
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLServer
import com.expediagroup.graphql.server.spring.execution.SpringKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import example.common.coroutine.runSpringAwareCoroutine
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.router
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.instanceParameter

/**
 * Configuration for the GraphQL server.
 *
 * This configuration does three things:
 *
 * 1) Adds our GraphQL servlet components to our application context. These beans are a copy of the
 * Expedia Kotlin GraphQL Spring Server components but adjusted to use Servlet APIs in Spring.
 *
 * 2) Adds GraphQL routes to our application context. These routes are a copy of the
 * Expedia Kotlin GraphQL Spring Server routes but adjusted to use Servlet APIs in Spring.
 *
 * 3) ensures that all [SpringDataFetcher] calls run asynchronously,
 * regardless of whether the underlying function is a suspend function or not.
 */
@Configuration
class GraphQLConfiguration(
  private val config: GraphQLConfigurationProperties,
  @Value("classpath:/graphql-playground.html") private val html: Resource,
  @Value("\${server.context-path:#{null}}") private val contextPath: String?,
) {
  /**
   * GraphQL request parser.
   *
   * Servlet equivalent of [SpringGraphQLRequestParser].
   */
  @Bean
  fun springServletGraphQLRequestParser(objectMapper: ObjectMapper) = SpringServletGraphQLRequestParser(objectMapper)

  /**
   * GraphQL context factory.
   *
   * Servlet equivalent of [SpringGraphQLContextFactory].
   */
  @Bean
  fun springServletGraphQLContextFactory() = SpringServletGraphQLContextFactory()

  /**
   * GraphQL server.
   *
   * Servlet equivalent of [SpringGraphQLServer].
   */
  @Bean
  fun springServletGraphQLServer(
    requestParser: SpringServletGraphQLRequestParser,
    contextFactory: SpringServletGraphQLContextFactory,
    requestHandler: GraphQLRequestHandler,
  ) = SpringServletGraphQLServer(
    requestParser,
    contextFactory,
    requestHandler,
  )

  /**
   * GraphQL Playground router, default of `/playground`.
   *
   * Servlet equivalent of [PlaygroundRouteConfiguration].
   */
  @Bean
  fun graphQlPlaygroundRouter() =
    router {
      val graphQLEndpoint =
        if (contextPath.isNullOrBlank()) {
          config.endpoint
        } else {
          "$contextPath/${config.endpoint}"
        }

      val subscriptionsEndpoint =
        if (contextPath.isNullOrBlank()) {
          config.subscriptions.endpoint
        } else {
          "$contextPath/${config.subscriptions.endpoint}"
        }

      GET(config.playground.endpoint) {
        ok().contentType(MediaType.TEXT_HTML).body(
          html.inputStream.bufferedReader().use { reader ->
            reader.readText()
              .replace("\${graphQLEndpoint}", graphQLEndpoint)
              .replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
          },
        )
      }
    }

  /**
   * GraphQL server router, default of `/graphql`.
   *
   * Servlet equivalent of [GraphQLRoutesConfiguration].
   */
  @Bean
  fun graphQlServerRouter(graphQLServer: SpringServletGraphQLServer) =
    router {
      (GET(config.endpoint) or POST(config.endpoint)) { request ->
        val graphQLResponse =
          runSpringAwareCoroutine {
            graphQLServer.execute(request)
          }

        val acceptMediaType =
          request.headers().accept()
            .find { it != MediaType.ALL && it == MediaType.APPLICATION_GRAPHQL_RESPONSE }
            ?.let { MediaType.APPLICATION_GRAPHQL_RESPONSE }
            ?: MediaType.APPLICATION_JSON

        if (graphQLResponse != null) {
          ok().contentType(acceptMediaType).body(graphQLResponse)
        } else {
          badRequest().build()
        }
      }
    }

  /**
   * GraphQL SDL router, default of `/sdl`.
   *
   * Servlet equivalent of [SdlRouteConfiguration].
   */
  @Configuration
  class SdlConfiguration(
    private val config: GraphQLConfigurationProperties,
    schema: GraphQLSchema,
  ) {
    private val sdl = schema.print()

    @Bean
    fun sdlRouter() =
      router {
        GET(config.sdl.endpoint) {
          ok().contentType(MediaType.TEXT_PLAIN).body(sdl)
        }
      }
  }

  /**
   * Custom [SpringDataFetcher] that overrides the [runBlockingFunction] method to wrap the result in a
   * [CompletableFuture].
   *
   * See https://github.com/ExpediaGroup/graphql-kotlin/issues/1965 for more information.
   *
   * @see [SpringDataFetcher]
   * @see [runBlockingFunction]
   */
  class CustomSpringDataFetcher(
    private val target: Any?,
    private val fn: KFunction<*>,
    applicationContext: ApplicationContext,
  ) : SpringDataFetcher(target, fn, applicationContext) {
    /**
     * Custom implementation of [SpringDataFetcher.get]
     * that wraps blocking functions in a [CompletableFuture],
     * so they're handled asynchronously similarly to suspend functions.
     */
    override fun get(environment: DataFetchingEnvironment): Any? {
      val instance: Any? = target ?: environment.getSource<Any?>()
      val instanceParameter = fn.instanceParameter

      return if (instance != null && instanceParameter != null) {
        val parameterValues =
          getParameters(fn, environment)
            .plus(instanceParameter to instance)

        if (fn.isSuspend) {
          runSuspendingFunction(environment, parameterValues)
        } else {
          runBlockingFunctionWrappedAsAsync(environment, parameterValues)
        }
      } else {
        null
      }
    }

    /**
     * Wraps the result of the [runBlockingFunction] in a [CompletableFuture] which is configured to execute as a
     * coroutine using the context provided by the [graphql.GraphQLContext].
     */
    private fun runBlockingFunctionWrappedAsAsync(
      environment: DataFetchingEnvironment,
      parameterValues: Map<KParameter, Any?>,
    ) = environment.graphQlContext.getOrDefault(CoroutineScope(EmptyCoroutineContext)).future {
      try {
        runBlockingFunction(parameterValues)
      } catch (exception: InvocationTargetException) {
        throw exception.cause ?: exception
      }
    }
  }

  /**
   * Custom [KotlinDataFetcherFactoryProvider] that overrides the [functionDataFetcherFactory] method to provide
   * a [DataFetcherFactory] which itself provides a [CustomSpringDataFetcher].
   *
   * @see [SpringKotlinDataFetcherFactoryProvider]
   * @see [CustomSpringDataFetcher]
   */
  class CustomSpringKotlinDataFetcherFactoryProvider(private val applicationContext: ApplicationContext) :
    SpringKotlinDataFetcherFactoryProvider(applicationContext) {
    override fun functionDataFetcherFactory(
      target: Any?,
      kClass: KClass<*>,
      kFunction: KFunction<*>,
    ): DataFetcherFactory<Any?> = DataFetcherFactory { CustomSpringDataFetcher(target, kFunction, applicationContext) }
  }

  /**
   * Provides a custom [KotlinDataFetcherFactoryProvider] implemented as [CustomSpringKotlinDataFetcherFactoryProvider]
   * that ultimately solves an issue with the [com.expediagroup.graphql.generator.execution.FunctionDataFetcher]
   * not being able to handle a non-suspend function throwing a [Error] (see https://github.com/ExpediaGroup/graphql-kotlin/issues/1965).
   *
   * @see [CustomSpringDataFetcher]
   */
  @Bean
  fun dataFetcherFactoryProvider(applicationContext: ApplicationContext): KotlinDataFetcherFactoryProvider =
    CustomSpringKotlinDataFetcherFactoryProvider(applicationContext)
}
