package com.tribal.challenge.services;

import com.tribal.challenge.models.CreditRequestData;
import reactor.core.publisher.Mono;

public interface CreditLineService {
    Mono<CreditRequestData> requestCreditLine(CreditRequestData requestData, String ip);
}
