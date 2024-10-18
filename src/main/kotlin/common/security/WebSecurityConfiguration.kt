//package example.common.security
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.core.annotation.Order
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
//import org.springframework.security.config.annotation.web.invoke
//import org.springframework.security.web.SecurityFilterChain
//
///**
// * Spring web security configuration. Defines our security filter chains.
// */
//@Configuration
////@EnableWebSecurity
//class WebSecurityConfiguration(
//) {
//
//  @Bean
//  @Order(2)
//  fun basicAuthAndJwtSecurityWebFilterChain(
//    http: HttpSecurity,
//  ): SecurityFilterChain {
//    http {
//      securityMatcher(
//        "/**",
//      )
//      authorizeRequests {
//        authorize("/graphql", permitAll)
//        authorize("/playground", permitAll)
//        authorize(anyRequest, permitAll)
//      }
//    }
//
//    return http.build()
//  }
//}