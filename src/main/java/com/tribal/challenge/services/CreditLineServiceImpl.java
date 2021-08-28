package com.tribal.challenge.services;

import com.tribal.challenge.models.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.repository.CreditLineRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Service
@AllArgsConstructor
public class CreditLineServiceImpl implements CreditLineService {

    private final CreditLineRepository creditLineRepository;

    @Override
    public Mono<CreditRequestData> requestCreditLine(CreditRequestData requestData, String ip) {
        log.info("requesting credit line");
        return creditLineRepository.retrieveCreditLine(ip)
                .doOnNext(it -> log.info("credit found finish"))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Generiting credit {}", requestData);
                    return requestData.validate()
                                    .flatMap(this::checkCreditLineRequest)
                                    .doOnNext(it -> log.info("credit aproved"))
                                    .flatMap(it -> creditLineRepository.saveCreditRequest(it, ip));
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
