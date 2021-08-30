package com.tribal.challenge;

import com.tribal.challenge.models.CreditRequestData;
import com.tribal.challenge.models.enums.BusinessType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CreditRequestDataTest {
    @Test
    public void validate_MinimumRequiredFieldsForSME_success(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(100);

        Assertions.assertDoesNotThrow(data::validate);
    }

    @Test
    public void validate_MinimumRequiredFieldsForStartup_success(){
        var data = new CreditRequestData();
        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(500);
        data.setCashBalance(500);
        data.setRequestedCreditLine(100);

        Assertions.assertDoesNotThrow(data::validate);
    }

    @Test
    public void validate_NoValidFoundingType_ThrowException(){
        var data = new CreditRequestData();

        data.setFoundingType("");
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(100);

        Assertions.assertThrows(IllegalArgumentException.class, data::validate);
    }

    @Test
    public void validate_NegativeMonthlyRevenue_ThrowException(){
        var data = new CreditRequestData();

        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(-500);
        data.setRequestedCreditLine(100);

        Assertions.assertThrows(IllegalArgumentException.class, data::validate);
    }

    @Test
    public void validate_NegativeCashBalance_ThrowException(){
        var data = new CreditRequestData();

        data.setFoundingType(BusinessType.STARTUP.name());
        data.setCashBalance(-1213);
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(100);

        Assertions.assertThrows(IllegalArgumentException.class, data::validate);
    }

    @Test
    public void validate_NegativeCreditLine_ThrowException(){
        var data = new CreditRequestData();

        data.setFoundingType(BusinessType.SME.name());
        data.setMonthlyRevenue(500);
        data.setRequestedCreditLine(-100);

        Assertions.assertThrows(IllegalArgumentException.class, data::validate);
    }
}
