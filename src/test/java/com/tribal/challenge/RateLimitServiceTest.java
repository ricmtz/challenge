package com.tribal.challenge;

import com.tribal.challenge.config.exceptions.BusinessException;
import com.tribal.challenge.services.RateLimitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RateLimitServiceTest {

    @Autowired
    private RateLimitService rateLimitService;

    @Test
    public void checkRateLimit_UserUnderLimit_ReturnsTrue(){
        var result = rateLimitService.checkRateLimit("127.0.0.1-UserUnderLimit").block();

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result);
    }

    @Test
    public void checkRateLimit_UserOverLimit_ReturnsFalse(){
        var ip = "127.0.1.1";

        rateLimitService.checkRateLimit(ip).block();
        rateLimitService.checkRateLimit(ip).block();
        rateLimitService.checkRateLimit(ip).block();

        var result = rateLimitService.checkRateLimit(ip).block();

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result);
    }


    @Test
    public void checkRateLimit_AfterBeingBlocked_ReturnsFalse(){
        var ip =  "127.0.1.2";
        rateLimitService.blockUser(ip).block();

        var result = rateLimitService.checkRateLimit(ip).block();

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result);
    }

    @Test
    public void blockUser_AfterBeingBlockedManyTimes_ThrowError(){
        var ip = "127.0.1.3";

        rateLimitService.blockUser(ip).block();
        rateLimitService.blockUser(ip).block();

        Assertions.assertThrows(BusinessException.class,
                () -> rateLimitService.blockUser(ip).block());
    }

    @Test
    public void resetUserAttempts_ResetAfterBeingBlock_NotThrowsError(){
        var ip = "127.0.1.4";
        rateLimitService.blockUser(ip).block();
        rateLimitService.blockUser(ip).block();

        rateLimitService.resetUserAttempts(ip).block();

        Assertions.assertDoesNotThrow(() -> rateLimitService.blockUser(ip).block());
    }

    @Test
    public void retrieveUserAttempts_AfterBeingBlocked_ReturnsAttempts(){
        var ip = "127.0.1.5";

        rateLimitService.blockUser(ip).block();
        rateLimitService.blockUser(ip).block();

        var results = rateLimitService.retrieveUserAttempts(ip).block();

        Assertions.assertNotNull(results);
        Assertions.assertEquals(2, results);
    }
}
