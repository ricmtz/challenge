package com.tribal.challenge.services;

import reactor.core.publisher.Mono;

public interface RateLimitService {

    Mono<Boolean> checkRateLimit(String ip);
    Mono<Boolean> blockUser(String ip);
    Mono<Boolean> resetUserAttempts(String ip);
    Mono<Integer> retrieveUserAttempts(String ip);
}
