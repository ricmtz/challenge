package com.tribal.challenge.controllers;

import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.services.CreditLineService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@AllArgsConstructor
public class CreditLineController {

    private final CreditLineService creditLineService;

    @Bean
    public RouterFunction<ServerResponse> creditLineRoutes(){
        return RouterFunctions.route()
                .POST("/v1/credits", this::requestCreditLine)
                .build();
    }

    private Mono<ServerResponse> requestCreditLine(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreditRequestData.class)
                .flatMap(creditLineService::requestCreditLine)
                .flatMap(it -> ServerResponse.created(URI.create("/v1/credits"))
                        .body(BodyInserters.fromValue(it))
                );
    }
}
