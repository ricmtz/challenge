package com.tribal.challenge.services;

import lombok.extern.slf4j.Slf4j;
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

    private Map<String, List<LocalDateTime>> rateLimitPerIP;
    private Map<String, LocalDateTime> blockedUsersPerIP;
    private Map<String, Integer> failedAttemptsPerIp;

    public RateLimitServiceImpl(){
        this.rateLimitPerIP = new HashMap<>();
        this.blockedUsersPerIP = new HashMap<>();
        this.failedAttemptsPerIp = new HashMap<>();
    }

    @Override
    public Mono<Boolean> checkRateLimit(String ip) {
        return userIsNotBlocked(ip)
                .filter(it -> it)
                .flatMap(it -> userIsBelowRateLimit(ip))
                .switchIfEmpty(Mono.just(false));
    }

    @Override
    public Mono<Boolean> blockUser(String ip) {
        log.info("Blocking temporally users");
        var attempts = failedAttemptsPerIp.getOrDefault(ip, 0);

        failedAttemptsPerIp.put(ip, attempts + 1);
        blockedUsersPerIP.put(ip, LocalDateTime.now(ZoneOffset.UTC));

        log.info("atemps {} ", failedAttemptsPerIp.get(ip));
        if(failedAttemptsPerIp.get(ip) >= 3){
            return Mono.error(new RuntimeException("A sales person will contact you"));
        }

        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> resetUserAttempts(String ip) {
        failedAttemptsPerIp.remove(ip);
        log.info("atteps {}", failedAttemptsPerIp.getOrDefault(ip,0));
        return Mono.just(true);
    }

    @Override
    public Mono<Integer> retrieveUserAttempts(String ip) {
        return Mono.just(failedAttemptsPerIp.getOrDefault(ip, 0));
    }

    private Mono<Boolean> userIsNotBlocked(String ip){
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
        var lastRequests = rateLimitPerIP.getOrDefault(ip, List.of());

        if(lastRequests.isEmpty()){
            log.info("Is new request");
            return Mono.just(updateLimitCountForUser(ip, List.of()));
        }

        return Mono.just(cleanOldRequestCount(lastRequests))
                .filter(it -> it.size() < 3)
                .map(it -> updateLimitCountForUser(ip, it))
                .switchIfEmpty(Mono.just(false));
    }

    private List<LocalDateTime> cleanOldRequestCount(List<LocalDateTime> localDateTimes){
        log.info("Clean request: {}", localDateTimes);
        var olderRequestAllowed = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(2L);

        return localDateTimes.stream()
                .filter(it -> it.isAfter(olderRequestAllowed))
                .collect(Collectors.toList());
    }

    private boolean updateLimitCountForUser(String ip, List<LocalDateTime> latestRequest){
        log.info("Updating, {} -> {}", ip, latestRequest);
        var aux = new ArrayList<>(latestRequest);

        aux.add(LocalDateTime.now(ZoneOffset.UTC));
        rateLimitPerIP.put(ip, aux);

        return true;
    }
}
