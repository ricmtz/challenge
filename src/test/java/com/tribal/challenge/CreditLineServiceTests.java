package com.tribal.challenge;

import com.tribal.challenge.models.enums.BusinessType;
import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.services.CreditLineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CreditLineServiceTests {

    @Autowired
    private CreditLineService creditLineService;

    private String clientIp = "127.0.0.1";

    @Test
    public void requestCreditLine_SMEFoundingType_CreditLineAccepted(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(100);

        var result =creditLineService.requestCreditLine(data, clientIp).block();

        Assertions.assertEquals(100, result.getRequestedCreditLine());
    }

}
