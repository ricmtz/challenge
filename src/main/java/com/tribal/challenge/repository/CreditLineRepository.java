package com.tribal.challenge.repository;

import com.tribal.challenge.models.CreditRequestData;
import reactor.core.publisher.Mono;

public interface CreditLineRepository {
    Mono<CreditRequestData> retrieveCreditLine(String ip);
    Mono<Boolean> alreadyHasCreditLine(String ip);
    Mono<CreditRequestData> saveCreditRequest(CreditRequestData creditRequestData, String ip);
}
