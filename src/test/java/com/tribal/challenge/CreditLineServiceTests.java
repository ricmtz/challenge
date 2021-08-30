package com.tribal.challenge;

import com.tribal.challenge.config.exceptions.BusinessExceptions;
import com.tribal.challenge.models.CreditRequestView;
import com.tribal.challenge.models.enums.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.services.CreditLineService;
import com.tribal.challenge.services.RateLimitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    private String clientIp = "127.0.0.1";

    @Test
    public void requestCreditLine_SMEFoundingType_CreditLineAccepted(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(100);

        var result= creditLineService.requestCreditLine(data, clientIp).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(100, result.getCreditLine());
    }

    @Test
    public void requestCreditLine_StartupFoundingTypeWithBiggerCashBalance_CreditLineAccepted(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(600);
        data.setRequestedCreditLine(200);

        var result= creditLineService.requestCreditLine(data, clientIp).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(200, result.getCreditLine());
    }

    @Test
    public void requestCreditLine_StartupFoundingTypeWithBiggerMonthlyRevenue_CreditLineAccepted(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);

        var result= creditLineService.requestCreditLine(data, clientIp).block();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(100, result.getCreditLine());
    }

    @Test
    public void requestCreditLine_retryAfterSuccess_ReturnSameCreditLine(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);

        var resultSuccess= creditLineService.requestCreditLine(data, clientIp).block();
        var retryRequest= creditLineService.requestCreditLine(data, clientIp).block();

        Assertions.assertNotNull(resultSuccess);
        Assertions.assertNotNull(retryRequest);
        Assertions.assertEquals(resultSuccess.getId(), retryRequest.getId());
        Assertions.assertEquals(resultSuccess.getCreditLine(), retryRequest.getCreditLine());
    }

    @Test
    public void requestCreditLine_retryAfterManyFails_ReturnWeWillContactYouMessage() throws InterruptedException {
        var invalidData = new CreditRequestData();
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.STARTUP.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);


        Mockito.when(rateLimitService.retrieveUserAttempts(Mockito.anyString()))
                        .thenReturn(Mono.just(3));

       Assertions.assertThrows(BusinessExceptions.class,
               () -> creditLineService.requestCreditLine(invalidData, clientIp)
                       .block()
       );
    }
}
