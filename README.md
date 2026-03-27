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

Most saga topics are created when services subscribe or produce. **`inventory.released`** is only produced (compensation after `payment.failed`) and has no consumer in this repo, so **inventory-service** declares it at startup via a Spring **`NewTopic`** bean (`KafkaTopicConfig`)—matching **3 partitions / RF 1** with `docker-compose` defaults—so publishes do not hit **`UNKNOWN_TOPIC_OR_PARTITION`**.

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

- Order Service: `http://localhost:8084/orders/hello`
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
curl -X POST http://localhost:8084/orders \
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
curl http://localhost:8084/orders/<orderId>
```

The response reflects the latest Saga status from the in-memory `OrderStore`.

### Demo: forced payment failure (`forcePaymentFailure`)

Optional **demo-only** flag on **`POST /orders`**: include **`"forcePaymentFailure": true`** to drive a **guaranteed `payment.failed`** after inventory reserves, without relying on `orderId` patterns. Omitted or `false` keeps the default success path (or legacy `fail-pay` / `PAYFAIL` behavior).

```bash
curl -s -X POST http://localhost:8084/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"BOOK-123","quantity":1}],"totalAmount":49.99,"forcePaymentFailure":true}'
```

**Expected end-to-end**

- Final **`GET /orders/{orderId}`** shows **`"status":"FAILED"`** (order-service applies `payment.failed`).
- **`inventory.released`** is published once (compensation after payment failure).
- **Replay duplicate protection:** send the same **`payment.failed`** JSON twice (e.g. via `kafka-console-producer`) and confirm **inventory-service** logs **`Duplicate event ignored: payment.failed …`** on the second delivery—no second **`inventory.released`**.

The flag is carried on **`order.created`** and forwarded on **`inventory.reserved`**; topic names are unchanged.

## Phase 3 Verification Checklist

Phase 3 is the **reliability** story: **Redis idempotency** (saga consumers do not double-apply events), **Redis order-status cache** (fast, observable GETs), and **consistent log lines** you can grep in a live demo or interview walkthrough. Kafka topics and JSON event shapes are unchanged; duplicates are skipped after the first successful handling.

### What Phase 3 guarantees

- **Duplicate Kafka deliveries** do not cause **double reserve**, **double charge**, or **double compensation**—consumers claim work with Redis (`SETNX` + TTL) before publishing or mutating state.
- **Order reads** are **accelerated** with **Redis status caching** on `GET /orders/{id}` (mirrors status; refreshes from the store on miss).
- **`OrderStore` remains the source of truth** for order status and payloads; Redis holds a short-lived cache of status strings, not the authoritative record.

### Known limitations

- **`OrderStore` is in-memory only**—data is lost when the order-service process restarts.
- **No persistent database yet**—this demo prioritizes the event flow over durability.
- **Idempotency is claim-first**, not full **exactly-once** semantics: the pattern prevents duplicate *effects* after a successful first claim, but it is not a Kafka transactional end-to-end guarantee.
- **Redis TTL expiration** (`event.idempotency.ttl-hours`, `order.cache.ttl-seconds`, etc.) means **replay protection and cache freshness are time-bounded**—after keys expire, a replay could be processed again unless stronger storage is added.

**Prerequisites**

- **Kafka** running (e.g. `docker compose up -d` from the repo root — broker is `eventcart-kafka`).
- **Redis** on `localhost:6379` (compose Redis may be commented out; run Redis locally if needed).
- Services: **order** `8084`, **inventory** `8081`, **payment** `8082`, **notification** `8083`.
- Kafka clients on the host use **`localhost:29092`**; **inside** the Kafka container use **`kafka:9092`**.

**Kafka producer helper (host)**

```bash
# Set once after you have an orderId
export ORDER_ID="<paste-from-POST-response>"
```

```bash
echo "{\"orderId\":\"$ORDER_ID\"}" | kafka-console-producer \
  --bootstrap-server localhost:29092 \
  --topic inventory.reserved
```

**Kafka producer helper (inside Docker — same broker as other containers)**

```bash
docker exec eventcart-kafka kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic inventory.reserved
# Then paste: {"orderId":"<uuid>"}  and press Enter (twice for replay demo)
```

One-shot from the host without an interactive shell:

```bash
docker exec eventcart-kafka bash -c 'echo "{\"orderId\":\"'"$ORDER_ID"'\"}" | kafka-console-producer --bootstrap-server kafka:9092 --topic inventory.reserved'
```

---

### 1) Normal success flow → `CONFIRMED`

1. Create an order (valid SKU — not `FAIL`):

```bash
curl -s -X POST http://localhost:8084/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"BOOK-123","quantity":1}],"totalAmount":49.99}'
```

2. Copy **`orderId`** from the JSON response into `ORDER_ID`.

3. Poll until the status is **`CONFIRMED`** (saga completes asynchronously):

```bash
curl -s "http://localhost:8084/orders/$ORDER_ID"
```

**Pass criteria:** response body shows `"status":"CONFIRMED"` after inventory reserves and payment succeeds.

---

### 2) Cache behavior (`GET /orders/{id}`)

1. Use the same **`ORDER_ID`** after it reaches at least `PENDING` or `RESERVED`/`CONFIRMED`.

2. Call **`GET` twice** in a row:

```bash
curl -s "http://localhost:8084/orders/$ORDER_ID"
curl -s "http://localhost:8084/orders/$ORDER_ID"
```

3. Watch **order-service** logs: second request should show **`Cache hit: orderId=…`** from `RedisOrderCacheService` (single log line per GET; no duplicate controller cache lines).

**Pass criteria:** repeated GETs avoid reloading from `OrderStore` when Redis still holds `order:status:<orderId>` (until `order.cache.ttl-seconds` expires).

---

### 3) Duplicate order status replay (`inventory.reserved` → order-service)

After the **success flow** has run, `order-service` has already applied **`inventory.reserved`** once (idempotency key is set). Send the **same** payload **again** to the topic:

```bash
echo "{\"orderId\":\"$ORDER_ID\"}" | kafka-console-producer \
  --bootstrap-server localhost:29092 \
  --topic inventory.reserved
```

**Pass criteria:** order-service logs **`Duplicate event ignored: inventory.reserved for order <orderId>`** and does **not** treat it as a new status transition (no duplicate `OrderStore` / cache writes for that replay).

---

### 4) Duplicate payment replay (`inventory.reserved` → payment-service)

With the same **`ORDER_ID`** after the saga has already charged once, produce to **`inventory.reserved`** again (same command as §3).

**Pass criteria:** payment-service logs **`Duplicate event ignored: inventory.reserved for order <orderId>`** — **no second** publish to `payment.succeeded` / `payment.failed`.

---

### 5) Duplicate compensation replay (`payment.failed` → inventory-service)

Use a **fresh id** if you only want to exercise compensation without the full saga (any string is fine for idempotency). Example: first and second delivery of the **same** JSON.

```bash
export COMP_ORDER_ID="comp-replay-demo-1"

echo "{\"orderId\":\"$COMP_ORDER_ID\"}" | kafka-console-producer \
  --bootstrap-server localhost:29092 \
  --topic payment.failed

echo "{\"orderId\":\"$COMP_ORDER_ID\"}" | kafka-console-producer \
  --bootstrap-server localhost:29092 \
  --topic payment.failed
```

**Pass criteria:** **First** message: **`Processing payment.failed for order …: publishing inventory.released compensation`** and one **`inventory.released`**. **Second** message: **`Duplicate event ignored: payment.failed for order … — skip inventory.released (compensation already applied)`** — no second `inventory.released`.

---

### Observability quick reference (grep-friendly)

| Area | What to grep / watch |
|------|----------------------|
| Order saga consumer | `Processing <eventType> for order`, `Duplicate event ignored:` |
| Order GET cache | `Cache hit: orderId=`, `Cache miss: orderId=` |
| Payment | `charging payment`, `Duplicate event ignored: inventory.reserved` |
| Inventory compensation | `publishing inventory.released compensation`, `Duplicate event ignored: payment.failed` |

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
