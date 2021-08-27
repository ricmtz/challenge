package com.tribal.challenge.config;

import com.tribal.challenge.services.RateLimitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class RateLimitHandlerFilter implements WebFilter {

    private final RateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        return rateLimitService.checkRateLimit(clientIp)
                .filter(it -> it)
                .flatMap(it -> chain.filter(exchange))
                .switchIfEmpty(Mono.defer(() -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return response.setComplete();
                }));

    }
}
