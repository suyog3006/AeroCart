# AeroCart: Distributed Order Processing & E-Commerce System

A production-grade, highly available, distributed e-commerce architecture built with **Java (Spring Boot 3)**, **Apache Kafka**, **Redis**, **Sharded PostgreSQL**, **Docker**, **Kubernetes (EKS/GKE)**, and **Prometheus/Grafana**.

---

## Architectural Highlights

- **Microservices Ecosystem**: User Service, Inventory Service, Order Service, Notification Service, and API Gateway.
- **Redis Distributed Locking**: Implemented in Inventory Service using Redisson & Lua scripts to handle ultra-high volume checkouts with zero overselling.
- **Saga Transaction Pattern**: Orchestrated in Order Service to manage distributed multi-service transactions with compensating actions (`Release Inventory`, `Cancel Order`) upon payment or stock failure.
- **Database Sharding**: Order database horizontally partitioned into `shard0` and `shard1` using `userId.hashCode() % 2` routing strategy.
- **Kafka Event Streaming**: Exactly-once processing with transactional producers and Redis-backed consumer message deduplication.

---

## Directory Structure

```
AeroCart/
├── docker-compose.yml
├── pom.xml
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── services/
│   ├── gateway-service/
│   ├── user-service/
│   ├── inventory-service/      # Redis Distributed Lock domain logic
│   ├── order-service/          # Saga Orchestrator & Sharded DB logic
│   └── notification-service/   # Idempotent Kafka consumer
├── k8s/                        # Production Kubernetes Manifests & HPA
├── tests/                      # Concurrency integration test suite (10k requests)
└── monitoring/                 # Prometheus config, Grafana dashboard & Helm values
```

---

## Quickstart & Local Execution

### 1. Launch Infrastructure & Microservices via Docker Compose
```bash
docker-compose up -d --build
```

### 2. Run High-Volume Concurrency Integration Test
Simulate 10,000 concurrent checkout requests against the system:
```bash
mvn test -pl tests -Dtest=ConcurrentCheckoutIntegrationTest
```

### 3. Deploy to Kubernetes
```bash
kubectl apply -f k8s/
```

### 4. Deploy Prometheus & Grafana Observability via Helm
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install aerocart-monitoring prometheus-community/kube-prometheus-stack -f monitoring/helm-values.yaml
```
