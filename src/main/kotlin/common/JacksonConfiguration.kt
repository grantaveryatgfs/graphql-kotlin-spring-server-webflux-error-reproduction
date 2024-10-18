package example.common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfiguration {
    /**
     * This defines a global ObjectMapper which is used by Spring to serialize and deserialize JSON.
     *
     * The below explicitly configures the ObjectMapper to what Spring would auto create if no bean is
     * defined, to make it easier to understand the configuration, and possibly override/extend in the future.
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper =
        jacksonMapperBuilder()
            .addModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .build()
}
