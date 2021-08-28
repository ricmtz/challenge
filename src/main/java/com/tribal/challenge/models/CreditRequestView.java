package com.tribal.challenge.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditRequestView {
    private String id;
    private String status;
    private double creditLine;
    private LocalDateTime createdAt;

    public static CreditRequestView of(CreditRequestData creditRequestData){
        var auxCreditRequest = new CreditRequestView();
        auxCreditRequest.setId(UUID.randomUUID().toString());
        auxCreditRequest.setCreditLine(creditRequestData.getRequestedCreditLine());
        auxCreditRequest.setStatus("APPROVED");
        auxCreditRequest.setCreatedAt(creditRequestData.getRequestedDate());

        return auxCreditRequest;
    }
}
