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
    - [Installation](#installation)
    - [Configuration](#configuration)
- [Ports](#ports)
- [Endpoints](#endpoints)


## Technologies Used
- Java 17
- Spring Boot
- Spring Framework
- Spring Security
- JWT Based Authentication & Authorization
- Docker
- PostgreSQL
- Mockito
- JUnit 5
- JaCoCo
- Zipkin
- Resilience4J
- Redis
- RabbitMQ
- Log4j2


## Features
- Soft Deletion
- Pagination, Filtering & Sorting
- Role Based Authentication & Authorization
- Token Based Authentication & Authorization


## Getting Started

### Prerequisites

Ensure you have the following installed:

- Java 17
- Maven or Gradle
- Docker (optional, for running Redis and RabbitMQ locally)


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

## Endpoints

All requests should be made to the API-GATEWAY's url which is http://localhost:8080.

### Auth Service

| HTTP Method | Endpoint           | Description                        |
|-------------|--------------------|------------------------------------|
| POST        | /auth/login        | Authenticate user and generate JWT |
| POST        | /auth/refreshToken | Refresh JWT                        |

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
| PATCH       | /products/stock/reduce      | Reduce product stock                      |
| PATCH       | /products/stock/return      | Return product stock                      |

### Campaign Service

| HTTP Method | Endpoint                     | Description                                |
|-------------|------------------------------|--------------------------------------------|
| GET         | /campaigns/{id}              | Get campaign by ID                         |
| GET         | /campaigns                   | Get all campaigns                          |
| GET         | /campaigns/filteredAndSorted | Get all campaigns with filters and sorting |

### Sale Service

| HTTP Method | Endpoint                 | Description                            |
|-------------|--------------------------|----------------------------------------|
| GET         | /sales                   | Get all sales                          |
| GET         | /sales/filteredAndSorted | Get all sales with filters and sorting |
| POST        | /sales                   | Create a new sale                      |
| PUT         | /sales/{id}              | Update sale by ID                      |
| DELETE      | /sales/{id}              | Delete sale by ID                      |
