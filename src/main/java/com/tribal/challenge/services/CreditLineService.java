package com.tribal.challenge.services;

import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.models.CreditRequestView;
import reactor.core.publisher.Mono;

public interface CreditLineService {
    Mono<CreditRequestView> requestCreditLine(CreditRequestData requestData, String ip);
}
