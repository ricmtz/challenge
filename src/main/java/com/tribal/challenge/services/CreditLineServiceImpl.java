package com.tribal.challenge.services;

import com.tribal.challenge.config.exceptions.BusinessException;
import com.tribal.challenge.models.CreditRequestView;
import com.tribal.challenge.models.enums.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.models.enums.ErrorCode;
import com.tribal.challenge.repository.CreditLineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CreditLineServiceImpl implements CreditLineService {

    private final int MAX_REQUEST_ATTEMPTS;
    private final double CASH_BALANCE_RATIO;
    private final double MONTHLY_REVENUE_RATIO;

    private final RateLimitService rateLimitService;
    private final CreditLineRepository creditLineRepository;

    public CreditLineServiceImpl(RateLimitService rateLimitService,
                                 CreditLineRepository creditLineRepository,
                                 @Value("${configs.limits.requests-attempts:3}") int maxRequestAttempts,
                                 @Value("${configs.ratios.cash-balance:3}") double cashBalanceRatio,
                                 @Value("${configs.ratios.monthly-ratio:5}") double monthlyRevenueRatio
                                 ) {

        this.rateLimitService = rateLimitService;
        this.creditLineRepository = creditLineRepository;
        this.MAX_REQUEST_ATTEMPTS = maxRequestAttempts;
        this.CASH_BALANCE_RATIO = cashBalanceRatio;
        this.MONTHLY_REVENUE_RATIO = monthlyRevenueRatio;
    }

    @Override
    public Mono<CreditRequestView> requestCreditLine(CreditRequestData requestData, String ip) {
        log.info("Requesting credit line.");

        return rateLimitService.retrieveUserAttempts(ip)
                .filter(currentAttempts -> currentAttempts < MAX_REQUEST_ATTEMPTS)
                .switchIfEmpty(Mono.error(BusinessException.MAX_ATTEMPTS_EXCEEDED))
                .flatMap(it -> creditLineRepository.retrieveCreditLine(ip))
                .doOnNext(it -> log.info("Credit for your the user {} already exists.", ip))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Previous credit not found, proceeding to create a new one for user {}.", ip);

                    return requestData.validate()
                            .flatMap(this::checkCreditLineRequest)
                            .flatMap(creditData -> creditLineRepository.saveCreditRequest(creditData, ip))
                            .flatMap(creditRequestView -> rateLimitService.resetUserAttempts(ip)
                                    .map(it -> creditRequestView)
                            )
                            .onErrorResume(ex -> rateLimitService.blockUser(ip)
                                    .flatMap(it -> Mono.error(ex))
                            );
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
                .filter(recommendedCredit -> recommendedCredit >= requestData.getRequestedCreditLine())
                .map(it -> requestData)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.REJECTED,
                        "The credit Line could not be approved"))
                );
    }

    private Mono<Double> calcRecommendedCreditForSME(CreditRequestData requestData){
        log.info("asda sme");
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
        return cashBalance * (1/CASH_BALANCE_RATIO);
    }

    private double calcMonthlyRevenue(double monthlyRevenue){
        return monthlyRevenue * (1F/ MONTHLY_REVENUE_RATIO);
    }
}
