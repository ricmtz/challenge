package com.tribal.challenge.models;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Data
public class CreditRequestData {
    private String foundingType;
    private double cashBalance;
    private double monthlyRevenue;
    private double requestedCreditLine;
    private LocalDateTime requestedDate;


    public Mono<CreditRequestData> validate(){
        foundingType = Strings.isNullOrEmpty(foundingType)? "" : foundingType.trim();
        requestedDate = Objects.isNull(requestedDate)? LocalDateTime.now(ZoneOffset.UTC): requestedDate;


        Preconditions.checkArgument(!Strings.isNullOrEmpty(foundingType), "foundingType is required");
        Preconditions.checkArgument(BusinessType.isValid(foundingType), "foundingType is not a valid type");
        Preconditions.checkArgument(monthlyRevenue >= 0, "monthlyRevenue should be grater or equals to 0");
        Preconditions.checkArgument(requestedCreditLine > 0, "requestedCreditLine should be greater that 0");

        // For Startup, cashBalance and monthlyRevenue are required.
        if(BusinessType.STARTUP.name.equalsIgnoreCase(foundingType)){
            Preconditions.checkArgument(cashBalance >= 0, "cashBalance should be grater or equals to 0");
        }

        return Mono.just(this);
    }
}
