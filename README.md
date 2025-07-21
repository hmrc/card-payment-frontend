
# card-payment-frontend

---
# Purpose and form

This repository contains the frontend microservice for card payments.
It is built using Scala (2.13) and the Play framework (2.8). We use linting tools such as WartRemover and Sclariform.
This microservice allows users to make card payments for any and all supported tax regimes.

---

# Running card-payment-frontend

To enable the frontend (this service) to successfully talk to the backend microservice (card-payment) you need to enable an internal auth token.
Make sure you have the internal auth service running, e.g.:
```
sm2 --start INTERNAL_AUTH INTERNAL_AUTH_FRONTEND --appendArgs '{"INTERNAL_AUTH": ["-Dapplication.router=testOnlyDoNotUseInAppConf.Routes"], "INTERNAL_AUTH_FRONTEND": ["-Dapplication.router=testOnlyDoNotUseInAppConf.Routes"]}'
```

And then insert a token:
```
curl --request POST \
  --url http://localhost:8470/test-only/token \
  --header 'content-type: application/json' \
  --data '{
    "token": "123456",
    "principal": "card-payment-frontend",
    "permissions": [{
        "resourceType": "card-payment",
        "resourceLocation": "card-payment/*",
        "actions": ["READ", "WRITE"]
    }]
}'
```
`$ sbt clean compile run`

To run unit and integration tests locally:

`$ sbt clean compile test`

This service runs on port **10155**

# During transition

We currently have a toggle which is limiting the flow of customers to this new service.
After beginning a journey via pay-frontend, you can access the card-payments journey via hitting the card-fees page:
http://localhost:10155/pay-by-card/card-fees
From here you can continue the journey as normal.

### License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").