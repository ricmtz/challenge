package com.tribal.challenge.services;

import com.tribal.challenge.models.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CreditLineServiceImpl implements CreditLineService {

    @Override
    public Mono<CreditRequestData> requestCreditLine(CreditRequestData requestData) {
        return requestData.validate()
                .flatMap(this::checkCreditLineRequest);
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
