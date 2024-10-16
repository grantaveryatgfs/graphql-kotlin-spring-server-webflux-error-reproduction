# graphql-kotlin-spring-server-webflux-error-reproduction

Reproduction steps:
1. Setup repo in IntelliJ IDEA with Java 21
2. Run `./gradlew bootRun`
3. See the exception:

```txt
Error creating bean with name 'org.springframework.web.reactive.config.DelegatingWebFluxConfiguration': The Java/XML config for Spring MVC and Spring WebFlux cannot both be enabled, e.g. via @EnableWebMvc and @EnableWebFlux, in the same application.
```

4. If you want to see the exception with `spring.main.allow-bean-definition-overriding=false` in the [application.yml](./src/main/resources/application.yml), change that and run `./gradlew bootRun` again. See the exception:

```txt
Description:

The bean 'requestMappingHandlerMapping', defined in class path resource [org/springframework/web/reactive/config/DelegatingWebFluxConfiguration.class], could not be registered. A bean with that name has already been defined in class path resource [org/springframework/web/servlet/config/annotation/DelegatingWebMvcConfiguration.class] and overriding is disabled.

Action:

Consider renaming one of the beans or enabling overriding by setting spring.main.allow-bean-definition-overriding=true
```

5. If you revert the `com.expediagroup:graphql-kotlin-spring-server:8.1.0` library in the [build.gradle.kts](build.gradle.kts) file to `com.expediagroup:graphql-kotlin-spring-server:8.0.0`, the application will run without exceptions.

```txt
Started ExampleApplicationKt in 1.634 seconds
```