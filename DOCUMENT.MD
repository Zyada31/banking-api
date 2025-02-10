# Developer Documentation
Version: 1.0

Technologies:  
Java 17,    
Spring Boot,   
PostgreSQL,   
Docker, 
Swagger/OpenAPI,   
Junit
Mockito

Purpose: Provides banking operations for bank employees. such as account creation, money transfers, and transaction history retrieval.

Prerequisites:  
Before running the application, ensure the following are installed:

Requirement	Check Command   
Java 17  
Maver	    
Docker Compose

## Developer Notes: How to Run the Banking API
How to run the banking API in two ways:

1. Running Locally (without Docker, using mvn spring-boot:run).
2. Running with Docker Compose (database + application in containers).

### Running Locally (Without Docker)
1. Prerequisites
   - Before running the app locally, ensure you have:
   - Java 17 installed (java -version).
   - Maven installed (mvn -version).
   - PostgreSQL database installed and running.
     
#### Step 1: Set Up PostgreSQL Database
- PostgreSQL:
  - if not running already.
    - `docker run --name postgres -e POSTGRES_USER=bankuser -e POSTGRES_PASSWORD=bankpass -p 5432:5432 -d postgres`  
    
    - `docker exec -it {REPLACEMEWITH-CONTAINER-ID} psql -U postgres `
        
        - `CREATE DATABASE bankdb;`
  ```
  
- if already exist.
  - Update creds in application.yml
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

#### Step 2: Configure application.properties (Optional)
- If your PostgreSQL setup is different, update src/main/resources/application.properties:
    ```yaml
    spring.datasource.url=jdbc:postgresql://localhost:5432/bankdb
    spring.datasource.username=bankuser
    spring.datasource.password=bankpass
    spring.jpa.hibernate.ddl-auto=update
    ```

Note: Ensure localhost:5432 matches your PostgreSQL configuration.

#### Step 3: Build & Run the Application
Run the application using Maven:

`mvn clean spring-boot:run`   

Your API should now be running at http://localhost:8080 

#### Step 4: Verify Everything Works
[check APP healthcheck] http://localhost:8080/actuator/health   
[check APP spec] http://localhost:8080/swagger-ui.html


## Running with Docker Compose
System Requirements: 
To run the Banking API and PostgreSQL in Docker, your system should have:

- Docker install and Daemon Running

#### Step 1: Build the Application
First, build the Spring Boot JAR file:

`mvn clean package`

This will generate a JAR file inside target/.

#### Step 2: Start Everything Using Docker Compose
Run the following command to start PostgreSQL and the Spring Boot app:

`docker-compose up --build`
This will:

1. Start a PostgreSQL container (bank_postgres).
2. Build and run the Spring Boot app container (bank_app).
3. Automatically connect Spring Boot to the database.

#### Step 3: Verify That Everything is Running
Check Running Containers

`docker ps`
Note: You should see two running containers:
```shell
> bank_postgres (PostgreSQL database)
> bank_app (Spring Boot application)
```
Check API Health : http://localhost:8080/actuator/health 

If successful, this should return { "status": "UP" }.

#### Step 4: Stopping the Application
To stop the application:
`docker-compose down`

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

## App CI/CD Pipeline
Thi application have gitaction for ci/cd and with jobs of 
- Job 1: Build & Test (build-and-test)
         Sets up JDK 17.
         Caches Maven dependencies for faster builds.
         Builds the project & runs tests.
         Uploads test results as an artifact.
- Job 2: Build & Push Docker Image (docker)
         Logs in to Docker Hub (uses GitHub secrets for security).
         Builds a Docker image from the latest code.
         Pushes the image to Docker Hub.
- Job 3: Deploy to Server (deploy)
         Connects to the production server via SSH.
         Pulls the latest Docker image.
         Stops and removes the old container (if running).
         Runs the new container on port 8080.

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

### future work
- Enhance customer info 
- Add future/scheduled  transfer functionality 