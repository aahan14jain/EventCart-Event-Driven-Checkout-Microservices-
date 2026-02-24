# EventCart - Event-Driven Checkout Microservices

An event-driven microservices architecture for handling event checkout processes, built with Spring Boot and Apache Kafka.

## 🏗️ Architecture

This project implements a microservices-based event-driven architecture for managing event checkouts. The system consists of four independent microservices that communicate asynchronously via Kafka events.

### Microservices

1. **Order Service** (`order-service`)
   - Handles order creation and management
   - Manages order lifecycle (PENDING, PROCESSING, COMPLETED, etc.)
   - Endpoint: `/orders`

2. **Payment Service** (`payment-service`)
   - Processes payment transactions
   - Handles payment validation and processing
   - Endpoint: `/payments`

3. **Inventory Service** (`inventory-service`)
   - Manages event inventory and availability
   - Tracks stock levels and reservations
   - Endpoint: `/inventory`

4. **Notification Service** (`notification-service`)
   - Sends notifications to users
   - Handles email, SMS, and push notifications
   - Endpoint: `/notifications`

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

Each service has a health check endpoint:

- Order Service: `http://localhost:8080/orders/hello`
- Payment Service: `http://localhost:8081/payments/hello`
- Inventory Service: `http://localhost:8082/inventory/hello`
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

## 📝 API Endpoints

### Order Service
- `GET /orders/hello` - Health check
- `POST /orders/` - Create a new order

### Payment Service
- `GET /payments/hello` - Health check

### Inventory Service
- `GET /inventory/hello` - Health check

### Notification Service
- `GET /notifications/hello` - Health check

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
