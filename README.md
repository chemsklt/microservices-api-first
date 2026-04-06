# Spring Boot Microservices – API-First Architecture
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen)
![Build](https://img.shields.io/badge/build-Maven-blue)
![Docker](https://img.shields.io/badge/docker-supported-blue)
![Coverage](https://img.shields.io/badge/coverage-60+%25-brightgreen)
![CI](https://github.com/chemsklt/devices-management-api/actions/workflows/ci.yml/badge.svg)

A **production-style microservices system** built with **Spring Boot**, following an **API-first design** using OpenAPI.

This project demonstrates:
- Clean service boundaries
- Synchronous + asynchronous communication
- Resilience patterns
- Full observability (metrics, logs, tracing)

---

# Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#-services)
- [Tech Stack](#tech-stack)
- [Communication Patterns](#communication-patterns)
- [Event-Driven Architecture (Kafka)](#2-asynchronous-event-driven)
- [Observability](#-observability)
- [Security](#-security)
- [Testing Strategy](#-testing-strategy)
- [Running the Project](#-running-the-project)
- [API Documentation](#-api-documentation)
- [Example Request](#-example-request)
- [Key Design Decisions](#-key-design-decisions)
- [Future Improvements](#-future-improvements)

---

# Architecture Overview

This system follows **API-first and domain-driven principles**, where each service is responsible for a well-defined business capability.

### Key characteristics:

- API Gateway as single entry point
- Strong service isolation
- Event-driven communication for decoupling
- Observability and tracing

---

# 🧩 Services

| Service | Responsibility | Tech |
|--------|----------------|------|
| **API Gateway** | Routing, security, aggregation | Spring Cloud Gateway |
| **Product Service** | Product catalog management | MongoDB |
| **Order Service** | Order processing & orchestration | MySQL |
| **Inventory Service** | Stock validation | MySQL |
| **Notification Service** | Async event consumer (email) | Kafka |

---
# Tech Stack

## Core
- Java 21
- Spring Boot

## Data
- MongoDB (Product)
- MySQL (Order, Inventory)

## Messaging
- Apache Kafka
- Avro + Schema Registry

## Resilience
- Resilience4j (Circuit Breaker)

## API Design
- OpenAPI (API-first)
- OpenAPI Generator

## Observability
- Micrometer
- Prometheus
- Grafana
- Loki (logs)
- Tempo (tracing)

## Security
- Keycloak (OAuth2 / JWT)

## Testing
- JUnit 5
- Mockito
- Testcontainers
- RestAssured
- JaCoCo (coverage)

---

# Communication Patterns

## 1. Synchronous (REST)

Order Service → Inventory Service

- Used for real-time validation
- Protected with Resilience4j

---

## 2. Asynchronous (Event-Driven)
Order Service → Kafka → Notification Service

- Decouples services
- Improves scalability
- Enables eventual consistency

---

# Event-Driven Architecture (Kafka)

### Topic
order-placed

### Avro Schema

```json
{
  "orderNumber": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string"
}
```

### Benefits
- Strong typing via Schema Registry
- Backward compatibility
- Loose coupling between services

# Testing Strategy
### Unit Testing
- Service layer isolation
- Mockito for mocking
### Integration Testing
- Testcontainers (MySQL, Kafka)
- Stub Runner / WireMock 
### API Testing
- RestAssured
### Code Coverage
JaCoCo (target ≥ 80%)

# API Documentation

Swagger UI (via Gateway): http://localhost:9000/swagger-ui.html

### Example Request
--> Place Order
```json
POST /api/order
```
```json
{
"skuCode": "iphone_15",
"price": 999.99,
"quantity": 1,
"userDetails": {
"email": "test@email.com",
"firstName": "Chems",
"lastName": "Keltoum"
}
}
```