package ace.charitan.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("project-service", r -> r.path("/project/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://PROJECT"))
                .route("donation-service", r -> r.path("/donation/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://DONATION"))
                .route("notification-service", r -> r.path("/notification/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://NOTIFICATION"))
                .route("notification-service-websocket", r -> r.path("/notification/ws/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .rewritePath("/ws/(?<remaining>.*)", "/ws/${remaining}")
                                .filter((exchange, chain) -> {
                                    exchange.getResponse().getHeaders().remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                                    exchange.getResponse().getHeaders().remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
                                    return chain.filter(exchange);
                                })
                        )
                        .uri("lb:ws://NOTIFICATION"))
                .route("payment-service", r -> r.path("/payment/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://PAYMENT"))
                .route("email-service", r -> r.path("/email/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://EMAIL"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:3000");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/project/**", corsConfiguration);
        source.registerCorsConfiguration("/donation/**", corsConfiguration);
        source.registerCorsConfiguration("/payment/**", corsConfiguration);
        source.registerCorsConfiguration("/email/**", corsConfiguration);

        return new CorsWebFilter(source);
    }

}
