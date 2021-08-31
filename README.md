# Tribal Coding Challenge

## Main aspect of the code.
- The request take as owner the client IP address, this is used to check the request limit, and validate if the client have already a credit.
- Various properties are configurable through properties files:
    - configs.limits.requests-attempts
        - For the number of attempts allowed before show the message "A sales person...".
        - Default: 3
    - configs.ratios.cash-balance
        - For the ratio used to calculate the recommended credit line in base of the cash balance.
        - Default: 3
    - configs.ratios.monthly-ratio
        - For the ratio used to calculate the recommended credit line in base of the monthly revenue.
        - Default: 5
    - configs.limits.requests-allowed
        - To configure the number of request a user can do in a period of time.
        - Default: 3
    - configs.limits.requests-threshold.minutes
        - The threshold of time to considered for the configuration `configs.limits.requests-allowed`
        - Default: 2
    - configs.limits.block-time.seconds
        - Defines the time that the user have to wait to send another request after fail a request.
        - Default: 30
- Restful was used to design this micro-service.
- Use of Spring boot and Project Reactor.


## How to run the application.

Requirements:
- Java JDK 11 or newer

Run in terminal:
```bash
./gradlew bootRun
```

How to perform the request:
For now, we just have a endpoint `/v1/credits`, so an example or request is like the following:
```bash
curl --request POST \
  --url http://localhost:8080/v1/credits \
  --header 'Content-Type: application/json' \
  --data '{
	"foundingType":"startup",
	"cashBalance": 4003,
	"monthlyRevenue": 30,
	"requestedCreditLine": 1334.3,
	"requestedDate": "2021-08-31T04:34:57.024Z"
}'
```