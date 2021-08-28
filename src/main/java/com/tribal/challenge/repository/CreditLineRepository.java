package com.tribal.challenge.repository;

import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.models.CreditRequestView;
import reactor.core.publisher.Mono;

public interface CreditLineRepository {
    Mono<CreditRequestView> retrieveCreditLine(String ip);
    Mono<Boolean> alreadyHasCreditLine(String ip);
    Mono<CreditRequestView> saveCreditRequest(CreditRequestData creditRequestData, String ip);
}
