package ace.charitan.gatewayuser.internal.filter;


import ace.charitan.gatewayuser.internal.jwt.JwtInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JweDecryptPreFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(JweDecryptPreFilter.class);

    private final String authenticationCookieKey;
    private final JwtInternalService jwtService;

    public JweDecryptPreFilter(
            @Value("${auth.cookie.name:charitan}") String authenticationCookieKey,
            JwtInternalService jwtService
    ) {
        this.authenticationCookieKey = authenticationCookieKey;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Retrieve the list of cookies by the configured key
        var authCookies = exchange.getRequest().getCookies().get(authenticationCookieKey);
        if (authCookies == null || authCookies.isEmpty()) {
            // If there's no cookie with that name, just continue
            return chain.filter(exchange);
        }

        // We only need the first cookie for decryption
        var authCookie = authCookies.getFirst();
        if (authCookie == null) {
        // If there's no cookie with that name, just continue
        return chain.filter(exchange);
        }
        logger.debug("Parsing {} cookie encrypted content", authCookie.getName());

        try {
            // Remove the cookie name prefix, e.g. "charitan="
            String jwe = authCookie.getValue().replaceFirst("^" + authenticationCookieKey + "=", "");
            // Decrypt the JWE into a JWS
            String jws = jwtService.parseJweContent(jwe);

            // Mutate the request to include a new Cookie header with the decrypted value
            var mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("Cookie", authenticationCookieKey + "=" + jws)
                    .build();

            var mutatedExchange = exchange
                    .mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            logger.error("Error parsing JWE content: {}", e.getMessage(), e);
        }

        // If anything fails, just continue without modifying the request
        return chain.filter(exchange);
    }
}
