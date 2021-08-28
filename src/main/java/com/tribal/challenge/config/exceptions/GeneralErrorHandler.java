package com.tribal.challenge.config.exceptions;

import com.tribal.challenge.models.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class GeneralErrorHandler {

    public Mono<ServerResponse> errorResponse(Throwable throwable){
        if(throwable instanceof  BusinessExceptions){
            var exception = (BusinessExceptions) throwable;

            return ServerResponse
                    .badRequest()
                    .body(BodyInserters.fromValue(errorBody(exception.getErrorCode(), exception.getMessage())));
        } else if(throwable instanceof IllegalArgumentException) {
            var exception = (IllegalArgumentException) throwable;

            return ServerResponse
                    .badRequest()
                    .body(BodyInserters.fromValue(errorBody(ErrorCode.VALIDATION, exception.getMessage())));

        } else if(throwable instanceof RuntimeException) {
            var exception = (RuntimeException) throwable;

            return ServerResponse
                    .badRequest()
                    .body(BodyInserters.fromValue(errorBody(ErrorCode.GENERAL_ERROR, exception.getMessage())));
        } else {
            return ServerResponse
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BodyInserters.fromValue(generalErrorBody("General Error please try later....")));
        }
    }

    private Map<String, Object> errorBody(ErrorCode errorCode, String message){
        return Map.of(
                "errorCode", errorCode.name(),
                "message", message
        );
    }

    private Map<String, Object> generalErrorBody(String message){
        return this.errorBody(ErrorCode.INTERNAL_ERROR, message);
    }
}
