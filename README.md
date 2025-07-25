# Card Payment Microservice (frontend)

## Overview

This repository contains the frontend microservice for card payments.
It is built using Scala (2.13) and the Play framework (3.0). We are using linting tools such as WartRemover and Sclariform.
It also has SbtUpdates (dependencyUpdates) which enables us to keep the service up-to-date.
This microservice allows users to make card payments for any and all supported tax regimes.

## Tech Stack

Scala 2.13
Play Framework 3.0
WartRemover, Scalariform
- **Port**: 10155

## Setup and Running

### Prerequisites

To enable the frontend (this service) to successfully talk to the backend microservice `card-payment` you need to enable an internal auth token.
Make sure you have the internal auth service running, e.g.:

```bash
sm2 --start INTERNAL_AUTH INTERNAL_AUTH_FRONTEND --appendArgs '{"INTERNAL_AUTH": ["-Dapplication.router=testOnlyDoNotUseInAppConf.Routes"], "INTERNAL_AUTH_FRONTEND": ["-Dapplication.router=testOnlyDoNotUseInAppConf.Routes"]}'
```

And then insert a token:
```bash
curl -X POST http://localhost:8470/test-only/token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "123456",
    "principal": "card-payment-frontend",
    "permissions": [
      {
        "resourceType": "card-payment",
        "resourceLocation": "card-payment/*",
        "actions": ["READ", "WRITE"]
      }
    ]
  }'
```

Start `pay-frontend` with the transitionary toggle off so all journey's go via `card-payment-frontend`:

```bash
sm2 -start OPS_ACCEPTANCE --appendArgs '{"PAY_FRONTEND" : ["-Dfeature.percentage-of-users-to-go-use-soap=0"]}'
```

### Running the Service

Compile and run the service:

```bash
sbt clean compile run
```

### Running Tests

Execute unit and integration tests:

```bash
sbt clean compile test
```

---

## During transition

We currently have a toggle which is limiting the flow of customers to this new service.
After beginning a journey via pay-frontend and making your way to the choose-a-way-to-pay page, select card and then you can access the card-payments journey via hitting the card-fees page here:
http://localhost:10155/pay-by-card/card-fees
From here you can continue the journey as normal.

---

## License

This project is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
