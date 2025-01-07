package ace.charitan.gatewayuser.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("project-service")
                .pathsToMatch("/project/**")
                .build();
    }

    @Bean
    public GroupedOpenApi donationApi() {
        return GroupedOpenApi.builder()
                .group("donation-service")
                .pathsToMatch("/donation/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("notification-service")
                .pathsToMatch("/notification/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("payment-service")
                .pathsToMatch("/payment/**")
                .build();
    }
}
