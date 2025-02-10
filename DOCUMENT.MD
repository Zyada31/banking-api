# Banking api documentation

## Purpose
The banking api is design to provide banking operations for bank employees. such as
- Creation of customer accounts and deposit initial above the lower limit amount.
- Transfer money between customer accounts and different customers.
- Retrieval of account balances.
- Transaction history tracking.

Built using Java 17, Spring boot, PostgresDB, and docker.

## Design
#### Overview
The Banking APi follows Layer approach
- Controller layer - handles Rest api requests for both transaction and account.
- Service layer - contains business logic.
- Repository layer - manages db interface using JPA.
- Database layer - uses Postgress with flyway for schema.

#### Technology used
- Java 17, Spring boot, data jpa, flyway
- Database: Postgres
- Testing: Junit 5, Mockito and testContainers
- CI/CD: Github actions, Docker and mvn
- dependency check: OWASP

#### Relationship Diagram

` Customer (1) ----> (M) BankAccount (1) ----> (M) Transaction `
Customer - Can have single/multiple bank accounts.
BankAccount - Belongs to one customer, can have multiple transactions.
Transaction - Records deposits and transfers (Captures success/fail transactions for tracking along with fail reasons).



Prerequisites:  
Before running the application, ensure the following are installed:

- Java 17
- postgress
- Maven
- Docker Compose

## Developer Notes: How to Run the Banking API
Two ways to run this application:

1. Running Locally (without Docker, using mvn spring-boot:run).
2. Running with Docker Compose (database + application in containers).

### Running Locally (Without Docker)
Set Up PostgreSQL Database
- if not running already.
  ```shell
      - `docker run --name postgres -e POSTGRES_USER=bankuser -e POSTGRES_PASSWORD=bankpass -p 5432:5432 -d postgres`  
      
      - `docker exec -it {REPLACEMEWITH-CONTAINER-ID} psql -U postgres `
          
          - `CREATE DATABASE bankdb;`
  ```
- if otherwise
  ```roomsql
  CREATE DATABASE bankdb;
  CREATE USER bankuser WITH PASSWORD 'bankpass';
  GRANT ALL PRIVILEGES ON DATABASE bankdb TO bankuser;
  ```
- Configure application.yml
  ```yaml
  datasource:
    url: REPLACEME/bankdb
    username: REPLACEME
    password: REPLACEME
    driver-class-name: org.postgresql.Driver
  ...
  flyway:
    ...
    locations: classpath:db/migration
    url: REPLACEME/bankdb
    user: REPLACEME
    password: REPLACEME
    ...
    ```
- Run unit, integration and dependency check:
  ```shell
  mvn clean verify dependency-check:check
  ```
- Build and run the application:
  ```shell
  mvn clean spring-boot:run
  ```
NOTE:
Your API should now be running at http://localhost:8080

- Verify Everything Works
    - [check APP healthcheck] http://localhost:8080/actuator/health
      response:
  ```json
    // http://localhost:8080/actuator/health
    {
    "status": "UP"
    }
    ```
- API Documentation (Swagger)
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - OpenAPI JSON: http://localhost:8080/v3/api-docs


### Running with Docker Compose
NOTE:
To run the Banking API and PostgreSQL in Docker, your system should have: Docker install and Daemon Running

- Build the Application

`mvn clean package`

This will generate a JAR file inside target/.

- Run with  Docker Compose

```shell 
docker-compose up --build
```

This will:

1. Start a PostgreSQL container (bank_postgres).
2. Build and run the Spring Boot app container (bank_app).
3. Automatically connect Spring Boot to the database.

- Verify That Everything is Running
  Check Running Containers
```shell
docker ps
```
Note: You should see two running containers:
```shell
> bank_postgres (PostgreSQL database)
> bank_app (Spring Boot application)
``` 
- Verify Everything Works
    - [check APP healthcheck] http://localhost:8080/actuator/health
      response:
  ```json
    // http://localhost:8080/actuator/health
    {
    "status": "UP"
    }
    ```
- API Documentation (Swagger)
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - OpenAPI JSON: http://localhost:8080/v3/api-docs


- Stopping the Application:
    ```shell
    docker-compose down
    ```

### App CI/CD Pipeline setup and triggered
Thi application have github action for ci/cd and with jobs of
- Job 1: Dependency-Check.
    - Checks vulnerabilities in banking-api-knkcnw using OWASP
- Job 2: build-and-test
    - Builds the project & runs unit and integration tests.
    - Uploads test results as an artifact.
    - Build/package application

- Job 3: Build & Push Docker Image (docker):
    - NOTE: Build will fail at this stage due to docker hub creds
        - Logs in to Docker Hub (uses GitHub secrets for security).
        - Builds a Docker image from the latest code.
        - publish the image to Docker Hub.
- Job 4: Deploy to Server (deploy)->
    - NOTE: future use once prod env is setup, this stage will deploy
        - Connects to the production server via SSH.
        - Pulls the latest Docker image.
        - Runs the new container.

NOTE: to properly run the actions, need to set Up GitHub Secrets
Go to

GitHub Repository → Settings → Secrets and Variables → Actions → New Repository Secret and add:
```
DOCKER_USERNAME	: Your Docker Hub username
DOCKER_PASSWORD	: Your Docker Hub password
SERVER_HOST	: IP address of your production server
SERVER_USER	: SSH username for your server
SSH_PRIVATE_KEY	: Private SSH key for server access
```
Running the Pipeline:
- Automatically runs when push to main branch or pull request is created for main.
- Manually via github actions.

### Manual test cases: Example API Requests
Note: use/import bank-api.postman_collection.json

1. create account with deposit:
- Request
```curl
POST http://localhost:8080/accounts?customerName=semere&initialDeposit=150
```
- response
```json
{
"success": true,
"message": "Account created or deposit successful",
"data": {
        "id": 5,
        "accountNumber": "ACC-C27D30FA",
        "balance": 150
    },
"timestamp": "2025-02-10T14:53:37.416265972"
}
```

2. create a different account for an existing customer and deposit
```
POST http://localhost:8080/accounts?customerName=semere&initialDeposit=150&accountNumber=ACC-1FA62431
```
- response
```json
{
"success": true,
"message": "Account created or deposit successful",
"data": {
        "id": 5,
        "accountNumber": "ACC-1FA62431",
        "balance": 150
    },
"timestamp": "2025-02-10T14:53:37.416265972"
}
```
3. Get account balance
```curl
http://localhost:8080/accounts/ACC-D7A5E027/balance
```
- response
```json
{
  "success": true,
  "message": "Account balance retrieved successfully",
  "data": 400.00,
  "timestamp": "2025-02-10T15:30:31.824976719"
}

```
4. transaction
```curl
http://localhost:8080/transactions/transfer
BODY:
{
    "fromAccount": "ACC-1FA62430",
    "toAccount": "ACC-F179EEB2",
    "amount": 100.00
}
```
- response
```json
{
    "success": true,
    "message": "Transfer successful",
    "data": "Funds transferred successfully",
    "timestamp": "2025-02-10T15:31:04.633916512"
}
```
5. Account transaction history
```curl
http://localhost:8080/transactions/ACC-1FA62430
```
- Response
```json
{
    "success": true,
    "message": "Transaction history retrieved",
    "data": [
        {
            "id": 3,
            "fromAccount": "ACC-1FA62430",
            "toAccount": "ACC-F179EEB2",
            "amount": 1000.00,
            "failureReason": "Insufficient funds in sender's account.",
            "status": "FAILED",
            "timestamp": "2025-02-10T14:18:39.930051"
        },
        {
            "id": 4,
            "fromAccount": "ACC-1FA62430",
            "toAccount": "ACC-F179EEB2",
            "amount": 1000.00,
            "failureReason": "Insufficient funds in sender's account. Current balance: 450.00.",
            "status": "FAILED",
            "timestamp": "2025-02-10T14:47:14.781245"
        }
    ],
  "timestamp": "2025-02-10T15:31:40.470241084"
}
```

### future work
- Enhance customer info
- Add future/scheduled transfer functionality
- Authentication & Role-based Access Control.
- Monitoring