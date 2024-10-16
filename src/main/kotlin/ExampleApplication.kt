package example

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
class ExampleApplication

var springApplicationContext: ApplicationContext? = null

fun main(args: Array<String>) {
    springApplicationContext =
        runApplication<ExampleApplication>(*args) {
            // Ensure we're a servlet application, not a reactive one.
            webApplicationType = WebApplicationType.SERVLET
        }
}
