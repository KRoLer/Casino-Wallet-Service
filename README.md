# Simple webservice for emulating casino's player wallet

# Operations
[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.getpostman.com/collections/85eaf2296d4318e7c3e0)

## Main features
* Register player
* Deposit money
* Withdraw money
* Check balance

## Languages and technologies 
* Scala
* Akka Actors, Akka HTTP, Akka Persistance
* Cassandra
* Docker
    
## Usage
The best way to run this webservice is to build and run docker image.

### Installation
* Validate docker installation or install it ([Download here](https://www.docker.com/community-edition))
* Download Cassandra image using docker: `docker pull cassandra`   
* Run cassandra container with alias name _cassandra_db_: `docker run --name cassandra_db cassandra:latest`
* Clone this repository, navigate to the root folder and run: `sbt docker:publishLocal`
* Run newly created image: `docker run -p "8080:8080" --name wallet --link cassandra_db:cassandra wallet-webservice:0.1`

### Validation
To validate this service locally we recommend to use [Postman](https://www.getpostman.com/apps).
After installation open the [collection link]((https://www.getpostman.com/collections/85eaf2296d4318e7c3e0)) to import predefined basic calls.

**cURL basic queries**
* Registration
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/register \
  --header 'Content-Type: application/json' \
  --data '{ 
  "playerId": 1 
}'
```
* Deposit
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/deposit \
  --header 'Content-Type: application/json' \
  --data '{
  "playerId": 1,
  "amount": 100
}'
```
* Balance
```bash
curl --request GET \
  --url http://localhost:8080/api/v1/balance/1
```
* Withdrawal
```bash
curl --request POST \
  --url http://localhost:8080/api/v1/withdraw \
  --header 'Content-Type: application/json' \
  --data '{
  "playerId": 1,
  "amount": 50
}'
```


