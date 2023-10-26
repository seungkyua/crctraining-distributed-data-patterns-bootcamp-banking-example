package net.chrisrichardson.bankingexample.apigateway.apigateway.proxies;

import net.chrisrichardson.bankingexample.apigateway.apigateway.ApiGatewayConfiguration;
import net.chrisrichardson.bankingexample.apigateway.apigateway.ApiGatewayDestinations;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
@Import({ApiGatewayConfiguration.class})
public class ProxyConfiguration {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${apigateway.timeout.millis}")
  private long apiGatewayTimeoutMillis;

  @Bean
  public AccountServiceProxy accountServiceProxy(ApiGatewayDestinations apiGatewayDestinations, WebClient client, CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
    return new AccountServiceProxy(apiGatewayDestinations, client, circuitBreakerRegistry, timeLimiterRegistry);
  }

  @Bean
  public CustomerServiceProxy customerServiceProxy(ApiGatewayDestinations apiGatewayDestinations, WebClient client, CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
    return new CustomerServiceProxy(client, circuitBreakerRegistry, apiGatewayDestinations.getCustomerServiceUrl(), timeLimiterRegistry);
  }

  @Bean
  public TimeLimiterRegistry timeLimiterRegistry() {
    logger.info("apiGatewayTimeoutMillis={}", apiGatewayTimeoutMillis);
    return TimeLimiterRegistry.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(apiGatewayTimeoutMillis)).build());
  }

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(apiGatewayTimeoutMillis)).build()).build());
  }
}
