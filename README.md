# AeroCart: Distributed Order Processing & E‑Commerce System

A production‑grade, highly available microservices architecture built with **Java 17 (Spring Boot 3)**, **Kafka**, **Redis**, **Sharded PostgreSQL**, **Docker**, **Kubernetes**, and **GitHub Actions**. The system implements a saga pattern for order processing, distributed locking for inventory, and event‑driven communication.

---

## Directory Structure
```
AeroCart/
├─ docker-compose.yml               # Local dev stack
├─ .github/workflows/ci-cd.yml      # CI/CD pipeline
├─ services/
│   ├─ gateway-service/            # API gateway (port 8080)
│   ├─ user-service/               # User management (port 8081)
│   ├─ inventory-service/          # Inventory with Redis lock (port 8084)
│   ├─ order-service/              # Saga orchestrator (port 8083)
│   └─ notification-service/       # Idempotent Kafka consumer (port 8085)
├─ k8s/                            # Production Kubernetes manifests
├─ monitoring/                     # Prometheus & Grafana configs
└─ README.md                       # You are reading it!
```

---

## Quick Start (Docker Compose)
```bash
# From the repository root
docker compose up --build -d
```
The compose file brings up:
- PostgreSQL (sharded or single instance as defined in the project)
- Redis
- Kafka + ZooKeeper
- All five Spring Boot services

The services are exposed on the following ports:
- **Gateway** – `8080`
- **User** – `8081`
- **Inventory** – `8084`
- **Order** – `8083`
- **Notification** – `8085`

You can verify they are running with `docker compose ps` and view logs with `docker compose logs -f`.

---

## CI/CD Pipeline
The GitHub Actions workflow (`.github/workflows/ci-cd.yml`) performs:
1. **Lint & Test** – Maven clean test across all modules.
2. **Docker Build & Push** – Builds each service image and pushes to Docker Hub (requires `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` secrets).
3. **Deploy** – Currently deploys to a Kubernetes cluster (EKS/GKE) using the manifests in `k8s/`. The workflow can be extended to deploy to **Oracle Cloud** by swapping the deployment job with OCI CLI steps.

---

## Deployment Options
- **Kubernetes** – Apply the manifests in `k8s/` to any K8s cluster (`kubectl apply -f k8s/`).
- **Oracle Cloud** – Replace the `deploy-to-k8s` job in the CI workflow with OCI commands to provision Container Instances or OCI OKE. Secrets such as `OCI_TENANCY`, `OCI_USER`, `OCI_FINGERPRINT`, and `OCI_PRIVATE_KEY` should be stored as GitHub secrets.

---

## Running Tests
```bash
# Run the concurrency integration test suite
mvn test -pl tests -Dtest=ConcurrentCheckoutIntegrationTest
```
This simulates 10 k concurrent checkout requests against the running stack.

---

## Contributing
Feel free to open issues or pull requests. Follow the standard GitHub workflow:
1. Fork the repo.
2. Create a feature branch.
3. Commit your changes.
4. Open a PR.

All contributions will be automatically validated by the CI pipeline.

---

## License
This project is licensed under the MIT License.
