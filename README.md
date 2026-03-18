# EventCart - Event-Driven Checkout Microservices

An event-driven microservices architecture for handling event checkout processes, built with Spring Boot and Apache Kafka.

## 🏗️ Architecture

This project implements a microservices-based event-driven architecture for managing event checkouts. The system consists of four independent microservices that communicate asynchronously via Kafka events.

### Microservices (Saga-based)

1. **Order Service** (`order-service`)
   - Creates orders and stores in-memory state
   - Publishes `order.created` and tracks Saga status (PENDING → RESERVED → CONFIRMED / FAILED)
   - Endpoint: `/orders`

2. **Inventory Service** (`inventory-service`)
   - Reserves or fails inventory on `order.created`
   - Publishes `inventory.reserved`, `inventory.failed`, and compensating `inventory.released`
   - No public HTTP API for the Saga demo

3. **Payment Service** (`payment-service`)
   - Listens to `inventory.reserved`
   - Publishes `payment.succeeded` or `payment.failed` (simulated failures)
   - No public HTTP API for the Saga demo

4. **Notification Service** (`notification-service`)
   - Listens to final Saga topics: `payment.succeeded`, `inventory.failed`, `payment.failed`
   - Logs concise “Order confirmed/failed” messages
   - No public HTTP API for the Saga demo

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 4.0.x**
- **Apache Kafka** - Event-driven communication
- **Spring Web MVC** - RESTful APIs
- **Spring Actuator** - Health checks and monitoring
- **Maven** - Build and dependency management

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Apache Kafka (for event-driven communication)
- Docker (optional, for running Kafka)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/aahan14jain/EventCart-Event-Driven-Checkout-Microservices-.git
cd EventCart-Event-Driven-Checkout-Microservices-
```

### 2. Start Kafka (if not already running)

```bash
# Using Docker
docker-compose up -d

# Or start Kafka manually
# Follow Kafka installation guide for your OS
```

### 3. Build the Project

```bash
# Build all services
cd EventCart/services
mvn clean install

# Or build individual services
cd order-service
mvn clean install
```

### 4. Run the Services

Each service can be run independently:

```bash
# Order Service
cd EventCart/services/order-service
mvn spring-boot:run

# Payment Service
cd EventCart/services/payment-service
mvn spring-boot:run

# Inventory Service
cd EventCart/services/inventory-service
mvn spring-boot:run

# Notification Service
cd EventCart/services/notification-service
mvn spring-boot:run
```

### 5. Verify Services

Health check endpoints:

- Order Service: `http://localhost:8080/orders/hello`
- Payment Service: `http://localhost:8082/payments/hello`
- Inventory Service: `http://localhost:8081/inventory/hello`
- Notification Service: `http://localhost:8083/notifications/hello`

## 📁 Project Structure

```
EventCart/
├── services/
│   ├── order-service/          # Order management microservice
│   ├── payment-service/        # Payment processing microservice
│   ├── inventory-service/      # Inventory management microservice
│   └── notification-service/   # Notification microservice
└── infra/                       # Infrastructure configurations
```

## 🔄 Event-Driven Communication

The microservices communicate asynchronously using Kafka topics:

- **Order Events**: Order creation, updates, and status changes
- **Payment Events**: Payment processing and status updates
- **Inventory Events**: Stock updates and reservations
- **Notification Events**: Notification triggers and delivery status

## 🧪 Testing

Run tests for each service:

```bash
cd EventCart/services/<service-name>
mvn test
```

## 📝 Demo Flow (Phase 2 Saga)

1. **Create an order**

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"BOOK-123","quantity":1}],"totalAmount":49.99}'
```

2. **Saga event chain**

- `order-service` → `order.created`
- `inventory-service` → `inventory.reserved` or `inventory.failed`
- `payment-service` → `payment.succeeded` or `payment.failed`
- `inventory-service` (compensation) → `inventory.released` on `payment.failed`
- `order-service` updates in-memory status (PENDING / RESERVED / CONFIRMED / FAILED)
- `notification-service` logs final outcome

3. **Check order status**

```bash
curl http://localhost:8080/orders/<orderId>
```

The response reflects the latest Saga status from the in-memory `OrderStore`.

## 🔧 Configuration

Each service has its own `application.properties` file in `src/main/resources/`. Configure Kafka brokers, ports, and other settings as needed.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is open source and available under the MIT License.

## 👤 Author

**Aahan Jain**
- GitHub: [@aahan14jain](https://github.com/aahan14jain)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for event streaming capabilities
