# Cash Register Project

## Project Description

<div style="display: flex; align-items: center;">
  <div style="flex: 1;">
    This project is made for 32bit's Backend Competition. The cash register project is designed to facilitate the process of a cashier handling sales transactions.
  </div>
  <div style="flex: 1; text-align: right;">
    <img src="~images/32bit.png" alt="Project Logo" style="max-width: 200px;">
  </div>
</div>

## Project Architecture

![Project Logo](~images/architecture.png)

## Table of Contents

- [Technologies Used](#technologies-used)
- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Usage](#usage)
- [Ports](#ports)
- [Endpoints](#endpoints)
- [Required Roles](#required-roles)
- [Endpoints](#endpoints)

## Technologies Used
- Java 17
- Spring Boot
- Spring Framework
- Spring Security
- JWT Based Authentication & Authorization
- Docker
- PostgreSQL
- Spring Cloud
- Netflix Eureka
- Mockito
- JUnit 5
- JaCoCo
- Zipkin
- Resilience4J
- Redis
- RabbitMQ
- Google Jib
- Log4j2


## Features
- Soft Deletion
- Pagination, Filtering & Sorting
- Role Based Authentication & Authorization
- Token Based Authentication & Authorization
- Microservices
- Unit Testing
- Caching
- Token Storing
- Circuit Breaking
- Message Queueing
- Logging
- JavaDocs
- Load Balancing
- Gateway


## Getting Started

### Prerequisites

Ensure you have the following installed:

- Java 17
- Maven or Gradle
- Docker 

### Usage
To start all services defined in docker-compose.yml, use the following command:

```bash
docker-compose up -d
```
To stop all running services, use:

```bash
docker-compose down
```

## Services

## Ports

| Container        | Port |
|------------------|------|
| API Gateway      | 8080 | 
| Service Registry | 8761 | 
| Redis            | 6380 | 
| RabbitMQ         | 5672 | 
| Zipkin           | 9411 | 
| Product DB       | 5433 |
| Sale DB          | 5434 |
| User DB          | 5435 |
| Auth DB          | 5436 |


## Required Roles

To make requests to the relevant service, you need to have the following role.

| Service         | Role    |
|-----------------|---------|
| Auth Service    |         | 
| User Service    | ADMIN   | 
| Product Service |         | 
| Sale Service    | CASHIER | 
| Report Service  | MANAGER | 


## Default Users

To make requests to the relevant service, you need to have the following role.

| User         | Username | Password | Roles                   |
|--------------|----------|----------|-------------------------|
| SUPER User   | super    | super    | CASHIER, MANAGER, ADMIN |
| CASHIER User | cashier  | cashier  | CASHIER                 |
| MANAGER User | manager  | manager  | MANAGER                 |
| ADMIN User   | admin    | admin    | ADMIN                   |


## Authentication and Authorization

> **⚠️Important:** All endpoints require a **JSON Web Token (JWT)** for authentication except auth/login, so **you need to log in first**.
To access the endpoints, include the JWT in the Authorization header of your HTTP requests as a **Bearer token**.

An example of login request:

```json
{
  "username": "super",
  "password": "super"
}
```


## Endpoints

All requests should be made to the **API Gateway's url** which is **http://localhost:8080**.

### Auth Service

| HTTP Method | Endpoint      | Description                        |
|-------------|---------------|------------------------------------|
| GET         | /auth/refresh | Refresh JWT                        | 
| POST        | /auth/login   | Authenticate user and generate JWT |
| POST        | /auth/logout  | logout and terminate JWT           | 

### User Service

| HTTP Method | Endpoint                 | Description                            |
|-------------|--------------------------|----------------------------------------|
| GET         | /users/{id}              | Get user by ID                         |
| GET         | /users                   | Get all users                          |
| GET         | /users/deleted           | Get all deleted users                  |
| GET         | /users/filteredAndSorted | Get all users with filters and sorting |
| POST        | /users                   | Create a new user                      |
| PUT         | /users/{id}              | Update user by ID                      |
| PUT         | /users/restore/{id}      | Restore soft deleted user              |
| DELETE      | /users/{id}              | Soft delete user by ID                 |
| DELETE      | /users/permanent/{id}    | Permanently delete user by ID          |

### Product Service

| HTTP Method | Endpoint                    | Description                               |
|-------------|-----------------------------|-------------------------------------------|
| GET         | /products/{id}              | Get product by ID                         |
| GET         | /products                   | Get all products                          |
| GET         | /products/deleted           | Get all deleted products                  |
| GET         | /products/filteredAndSorted | Get all products with filters and sorting |
| POST        | /products                   | Create a new product                      |
| PUT         | /products/{id}              | Update product by ID                      |
| PUT         | /products/restore/{id}      | Restore soft deleted product              |
| DELETE      | /products/{id}              | Soft delete product by ID                 |
| DELETE      | /products/permanent/{id}    | Permanently delete product by ID          |

### Sale Service

| HTTP Method | Endpoint                     | Description                                |
|-------------|------------------------------|--------------------------------------------|
| GET         | /campaigns/{id}              | Get campaign by ID                         |
| GET         | /campaigns                   | Get all campaigns                          |
| GET         | /campaigns/filteredAndSorted | Get all campaigns with filters and sorting |
| POST        | /sales                       | Create a new sale                          |
| PUT         | /sales/{id}                  | Update sale by ID                          |
| PUT         | /sales/cancel/{id}           | Cancel sale by ID                          |
| PUT         | /sales/restore/{id}          | Restore sale by ID                         |
| DELETE      | /sales/{id}                  | Soft delete sale by ID                     |
| DELETE      | /sales/permanent/{id}        | Permanently delete sale by ID              |

### Report Service

| HTTP Method | Endpoint                   | Description                            |
|-------------|----------------------------|----------------------------------------|
| GET         | /reports/{id}              | Get sale by ID                         |
| GET         | /reports                   | Get all sales                          |
| GET         | /reports/deleted           | Get all deleted sales                  |
| GET         | /reports/filteredAndSorted | Get all sales with filters and sorting |
| GET         | /reports/receipt/{id}      | Get receipt by sale ID                 |


## Request Body Examples

### Auth Service

**Endpoint:**
POST /auth/login
```json
{
  "username": "super",
  "password": "super"
}
```

### User Service

**Endpoints:**
- POST /users
- PUT /users/{id}

```json
{
  "name": "John Doe",
  "username": "johndoe",
  "email": "johndoe@gmail.com",
  "password": "johndoe54",
  "roles": ["ADMIN", "CASHIER"]
}
```


### Product Service

**Endpoints:**
- POST /products
- PUT /products/{id}

```json
{
  "name": "product",
  "description": "a product",
  "stockQuantity": "20",
  "price": 150
}
```


### Sale Service

**Endpoints:**
- POST /sales 
- PUT /sales/{id}
```json
{
  "cashier": "Jack",
  "paymentMethod": "mixed",
  "campaignIds": [2,3],
  "products": [
    {
      "id": 12,
      "quantity": 2
    },
    {
      "id": 10,
      "quantity": 1
    }
  ],
  "cash": null,
  "mixedPayment": {
    "cashAmount": 1500.00,
    "creditCardAmount": 1200.00
  }
}
```

> **⚠️ Note:** Since there are **4 types of payment methods**, this JSON object can vary. Below is also valid.
```json
{
  "cashier": "Jack",
  "paymentMethod": "cash",
  "campaignIds": [2,3],
  "products": [
    {
      "id": 12,
      "quantity": 2
    },
    {
      "id": 10,
      "quantity": 1
    }
  ],
  "cash": 2500,
  "mixedPayment": null
}
```














