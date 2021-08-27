package com.tribal.challenge.services;

import reactor.core.publisher.Mono;

public interface RateLimitService {

    Mono<Boolean> checkRateLimit(String ip);
    Mono<Boolean> blockUser(String ip);
}
