package example

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

@Component
class ExampleGraphQLQuery : Query {
    @GraphQLDescription("Example.")
    @Suppress("unused")
    fun exampleGet(): String = "Hello, World!"
}
