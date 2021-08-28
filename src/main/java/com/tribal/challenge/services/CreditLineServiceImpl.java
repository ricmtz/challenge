package com.tribal.challenge.services;

import com.tribal.challenge.models.CreditRequestView;
import com.tribal.challenge.models.enums.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.repository.CreditLineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CreditLineServiceImpl implements CreditLineService {

    private final int MAX_REQUEST_ATTEMPTS;

    private final RateLimitService rateLimitService;
    private final CreditLineRepository creditLineRepository;

    public CreditLineServiceImpl(RateLimitService rateLimitService,
                                 CreditLineRepository creditLineRepository,
                                 @Value("${configs.requests-attempts:3}") int maxRequestAttempts) {

        this.rateLimitService = rateLimitService;
        this.creditLineRepository = creditLineRepository;
        this.MAX_REQUEST_ATTEMPTS = maxRequestAttempts;
    }

    @Override
    public Mono<CreditRequestView> requestCreditLine(CreditRequestData requestData, String ip) {
        log.info("requesting credit line attemps {}", MAX_REQUEST_ATTEMPTS);
        return rateLimitService.retrieveUserAttempts(ip)
                .filter(it -> it < MAX_REQUEST_ATTEMPTS)
                .switchIfEmpty(Mono.error(new RuntimeException("A Sales Agent will contact you")))
                .flatMap(it -> creditLineRepository.retrieveCreditLine(ip))
                .doOnNext(it -> log.info("credit found finish"))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Generiting credit {}", requestData);
                    return requestData.validate()
                            .flatMap(this::checkCreditLineRequest)
                            .flatMap(it -> creditLineRepository.saveCreditRequest(it, ip))
                            .flatMap(it -> rateLimitService.resetUserAttempts(ip)
                                    .map(reset -> it)
                            )
                            .onErrorResume(ex -> {
                                return rateLimitService.blockUser(ip)
                                        .flatMap(it -> Mono.error(ex));
                            });
                        })
                );
    }

    private Mono<CreditRequestData> checkCreditLineRequest(CreditRequestData requestData){
        Mono<Double> calcRecommendedCredit;

        switch (BusinessType.of(requestData.getFoundingType())){
            case SME:
                calcRecommendedCredit = calcRecommendedCreditForSME(requestData);
            break;
            case STARTUP:
                calcRecommendedCredit = calcRecommendedCreditStartup(requestData);
            break;
            default:
                calcRecommendedCredit = Mono.error(new RuntimeException("Not valid businessType handler found"));
        }

        return calcRecommendedCredit
                .filter(recommendedCredit -> recommendedCredit > requestData.getRequestedCreditLine())
                .map(it -> requestData)
                .switchIfEmpty(Mono.error(new RuntimeException("The credit Line could not be approved")));
    }

    private Mono<Double> calcRecommendedCreditForSME(CreditRequestData requestData){
        return Mono.just(calcMonthlyRevenue(requestData.getMonthlyRevenue()));
    }

    private Mono<Double> calcRecommendedCreditStartup(CreditRequestData requestData){
        var cashBalance = calcCashBalance(requestData.getCashBalance());
        var monthlyRevenue = calcMonthlyRevenue(requestData.getMonthlyRevenue());

        if(cashBalance > monthlyRevenue){
            return Mono.just(cashBalance);
        }else {
            return Mono.just(monthlyRevenue);
        }
    }

    private double calcCashBalance(double cashBalance){
        return cashBalance * (1F/3F);
    }

    private double calcMonthlyRevenue(double monthlyRevenue){
        return monthlyRevenue * (1F/5F);
    }
}
