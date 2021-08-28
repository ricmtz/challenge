package com.tribal.challenge.repository;

import com.tribal.challenge.models.CreditRequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class CreditLineRepositoryInMemoryImpl implements CreditLineRepository{

    private final Map<String, CreditRequestData> storage;

    public CreditLineRepositoryInMemoryImpl() {
        this.storage = new HashMap<>();
    }

    @Override
    public Mono<CreditRequestData> retrieveCreditLine(String ip) {
        log.info("Ip {}", ip);
        if(!storage.containsKey(ip)){
            log.info("Payment not found");
            return Mono.empty();
        }
        log.info("creditr found");
        return Mono.just(storage.get(ip));
    }

    @Override
    public Mono<Boolean> alreadyHasCreditLine(String ip) {
        return Mono.just(storage.containsKey(ip));
    }

    @Override
    public Mono<CreditRequestData> saveCreditRequest(CreditRequestData creditRequestData, String ip) {
        log.info("asdasd {}  --- {}", creditRequestData, ip);
        storage.put(ip, creditRequestData);
        return Mono.just(creditRequestData);
    }
}
