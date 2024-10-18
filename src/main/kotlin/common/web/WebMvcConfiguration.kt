package example.common.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.seconds

@EnableWebMvc
@Configuration
class WebMvcConfiguration(
) : WebMvcConfigurer
