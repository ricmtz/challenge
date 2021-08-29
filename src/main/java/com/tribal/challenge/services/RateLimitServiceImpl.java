package com.tribal.challenge.services;

import com.tribal.challenge.config.exceptions.BusinessExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RateLimitServiceImpl implements  RateLimitService{

    private final int MAX_REQUEST_ATTEMPTS;

    private Map<String, List<LocalDateTime>> rateLimitPerIP;
    private Map<String, LocalDateTime> blockedUsersPerIP;
    private Map<String, Integer> failedAttemptsPerIp;

    public RateLimitServiceImpl(@Value("${configs.requests-attempts:3}") int maxRequestAttempts){
        this.rateLimitPerIP = new HashMap<>();
        this.blockedUsersPerIP = new HashMap<>();
        this.failedAttemptsPerIp = new HashMap<>();
        this.MAX_REQUEST_ATTEMPTS = maxRequestAttempts;
    }

    @Override
    public Mono<Boolean> checkRateLimit(String ip) {
        log.info("Check rate limit for user {}", ip);

        return userIsNotBlocked(ip)
                .filter(it -> it)
                .flatMap(it -> userIsBelowRateLimit(ip))
                .switchIfEmpty(Mono.just(false));
    }

    @Override
    public Mono<Boolean> blockUser(String ip) {
        log.info("Request failed, blocking user {} temporally", ip);

        var attempts = failedAttemptsPerIp.getOrDefault(ip, 0);

        failedAttemptsPerIp.put(ip, attempts + 1);
        blockedUsersPerIP.put(ip, LocalDateTime.now(ZoneOffset.UTC));

        if(failedAttemptsPerIp.get(ip) >= MAX_REQUEST_ATTEMPTS){
            return Mono.error(BusinessExceptions.MAX_ATTEMPTS_EXCEEDED);
        }

        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> resetUserAttempts(String ip) {
        log.info("Reset attempts for user {}", ip);

        failedAttemptsPerIp.remove(ip);
        return Mono.just(true);
    }

    @Override
    public Mono<Integer> retrieveUserAttempts(String ip) {
        log.info("Retrieving attempts for user {}", ip);

        return Mono.just(failedAttemptsPerIp.getOrDefault(ip, 0));
    }

    private Mono<Boolean> userIsNotBlocked(String ip){
        log.info("Check user {} status.", ip);

        var blockTime = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(30);

        if(!blockedUsersPerIP.containsKey(ip)){
            return Mono.just(true);
        }

        return Mono.just(blockedUsersPerIP.get(ip))
                .filter(it -> it.isBefore(blockTime))
                .map(it -> true)
                .switchIfEmpty(Mono.just(false));
    }

    private Mono<Boolean> userIsBelowRateLimit(String ip){
        log.info("Check user {} rate limit", ip);

        var lastRequests = rateLimitPerIP.getOrDefault(ip, List.of());

        if(lastRequests.isEmpty()) {
            return Mono.just(updateLimitCountForUser(ip, List.of()));
        }

        return Mono.just(cleanOldRequestCount(lastRequests))
                .filter(it -> it.size() < 3)
                .map(it -> updateLimitCountForUser(ip, it))
                .switchIfEmpty(Mono.just(false));
    }

    private List<LocalDateTime> cleanOldRequestCount(List<LocalDateTime> localDateTimes){
        log.info("Clean ol request for user {}", localDateTimes);

        var olderRequestAllowed = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(2L);

        return localDateTimes.stream()
                .filter(it -> it.isAfter(olderRequestAllowed))
                .collect(Collectors.toList());
    }

    private boolean updateLimitCountForUser(String ip, List<LocalDateTime> latestRequest){
        log.info("Updating limit request for user {} -> {}", ip, latestRequest);

        var aux = new ArrayList<>(latestRequest);

        aux.add(LocalDateTime.now(ZoneOffset.UTC));
        rateLimitPerIP.put(ip, aux);

        return true;
    }
}
