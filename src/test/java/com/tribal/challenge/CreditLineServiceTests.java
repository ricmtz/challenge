package com.tribal.challenge;

import com.tribal.challenge.config.exceptions.BusinessException;
import com.tribal.challenge.models.enums.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.services.CreditLineService;
import com.tribal.challenge.services.RateLimitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

@SpringBootTest
public class CreditLineServiceTests {

    @Autowired
    private CreditLineService creditLineService;

    @MockBean
    private RateLimitService rateLimitService;

    @Test
    public void requestCreditLine_SMEFoundingType_CreditLineAccepted(){
        var ip = "127.0.0.1";
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(100);

        Mockito.when(rateLimitService.retrieveUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(0));
        Mockito.when(rateLimitService.resetUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(true));
        Mockito.when(rateLimitService.blockUser(Mockito.anyString()))
                .thenReturn(Mono.just(true));

        var result= creditLineService.requestCreditLine(data, ip).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(100, result.getCreditLine());
    }

    @Test
    public void requestCreditLine_StartupFoundingTypeWithBiggerCashBalance_CreditLineAccepted(){
        var ip = "127.0.0.2";
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(600);
        data.setRequestedCreditLine(200);

        Mockito.when(rateLimitService.retrieveUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(0));
        Mockito.when(rateLimitService.resetUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(true));
        Mockito.when(rateLimitService.blockUser(Mockito.anyString()))
                .thenReturn(Mono.just(true));

        var result= creditLineService.requestCreditLine(data, ip).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(200, result.getCreditLine());
    }

    @Test
    public void requestCreditLine_StartupFoundingTypeWithBiggerMonthlyRevenue_CreditLineAccepted(){
        var ip = "127.0.0.3";
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);

        Mockito.when(rateLimitService.retrieveUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(0));
        Mockito.when(rateLimitService.resetUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(true));
        Mockito.when(rateLimitService.blockUser(Mockito.anyString()))
                .thenReturn(Mono.just(true));

        var result= creditLineService.requestCreditLine(data, ip).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(100, result.getCreditLine());
    }

    @Test
    public void requestCreditLine_retryAfterSuccess_ReturnSameCreditLine(){
        var ip = "127.0.0.4";
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);

        Mockito.when(rateLimitService.retrieveUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(0));
        Mockito.when(rateLimitService.resetUserAttempts(Mockito.anyString()))
                .thenReturn(Mono.just(true));
        Mockito.when(rateLimitService.blockUser(Mockito.anyString()))
                .thenReturn(Mono.just(true));

        var resultSuccess= creditLineService.requestCreditLine(data, ip).block();
        var retryRequest= creditLineService.requestCreditLine(data, ip).block();

        Assertions.assertNotNull(resultSuccess);
        Assertions.assertNotNull(retryRequest);
        Assertions.assertEquals(resultSuccess.getId(), retryRequest.getId());
        Assertions.assertEquals(resultSuccess.getCreditLine(), retryRequest.getCreditLine());
    }

    @Test
    public void requestCreditLine_retryAfterManyFails_ReturnWeWillContactYouMessage() throws InterruptedException {
        var ip = "127.0.0.5";
        var invalidData = new CreditRequestData();
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);

        Mockito.when(rateLimitService.retrieveUserAttempts(Mockito.anyString()))
                        .thenReturn(Mono.just(3));

       Assertions.assertThrows(BusinessException.class,
               () -> creditLineService.requestCreditLine(invalidData, ip).block()
       );
    }
}
