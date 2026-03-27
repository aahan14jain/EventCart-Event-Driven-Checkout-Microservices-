# order-service

## Redis order status cache

For reads, the service uses **`OrderStore` as the source of truth** and Redis only as a **short-lived cache** of the status string so `GET /orders/{orderId}` can be fast.

### Key format

| | |
|---|---|
| Redis key | `order:status:<orderId>` |
| Value | Plain status string (`PENDING`, `RESERVED`, `CONFIRMED`, `FAILED`, etc.) |

### TTL behavior

- Property: **`order.cache.ttl-seconds`** (default **60** in `application.properties`).
- Each write to Redis (`saveOrderStatus`) sets the key with that TTL. When the key expires, the next GET behaves like there is no cache entry.

### Cache hit / miss flow

1. **GET** `/orders/{orderId}` calls Redis first.
2. **Cache hit** — key exists and is non-empty → return status immediately (no `OrderStore` read for status).
3. **Cache miss** — key missing or expired → load **`OrderStore`**. If the order exists, **repopulate Redis** with the current status and the configured TTL, then return. If not found, **404**.

Writes (POST create, saga listeners) still update **`OrderStore` first**, then mirror the latest status into Redis.

### Demo-friendly: show miss → hit → miss after expiry

1. Start Redis and the service. For a **15s** cache TTL (easier to show expiry on stage), run with the **`demo`** profile:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
   ```

2. Create an order (POST `/orders`), note `orderId`.

3. **Flush** the cache key for that order (or wait for TTL), or use an order **never** written to Redis yet — e.g. skip POST cache by restarting Redis and not recreating *or* delete the key manually:

   ```bash
   redis-cli DEL "order:status:<your-order-id>"
   ```

   Alternatively: first GET after a long idle time where the key expired.

4. **First GET** `/orders/{orderId}` → **cache miss** (logs: `Cache miss: orderId=…`) → loads store, writes Redis.

5. **Second GET** (same id, before TTL) → **cache hit** (logs: `Cache hit: orderId=…`).

6. **Wait** longer than the TTL (e.g. **61s** with default profile, or **16s** with `demo`).

7. **GET again** → **cache miss** again, then repopulates from `OrderStore`.

With `demo` profile you do not need to wait a full minute between steps 5 and 7.

## Redis saga idempotency (Kafka consumers)

`SagaStatusListener` calls **`markIfNotProcessed(eventType, orderId)`** in Redis **before** any **`OrderStore`** update. Keys look like `processed:event:<eventType>:<orderId>` (see `IdempotencyService`).

**Logs to grep during a demo or replay:**

| Situation | Log line |
|-----------|----------|
| First time this event is applied | `Processing <eventType> for order <orderId>` |
| Duplicate delivery (skip store/cache update) | `Duplicate event ignored: <eventType> for order <orderId>` |

Replay the same JSON to a topic (or let the consumer redeliver): first delivery shows **Processing**, second shows **Duplicate event ignored**—no duplicate status transition.

### Create an order

`POST` **`/orders`** (default base URL: `http://localhost:8084` unless you set `server.port`).

Example body (matches `CreateOrderRequest` / `OrderItem`):

```json
{
  "items": [{ "sku": "SKU-1", "quantity": 1 }],
  "totalAmount": 19.99
}
```

Example:

```bash
curl -s -X POST http://localhost:8084/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"SKU-1","quantity":1}],"totalAmount":19.99}'
```

The JSON response includes **`orderId`** — use it in saga payloads and when replaying messages.

### How to identify an emitted event

| What | Where to look |
|------|----------------|
| **order.created** | Kafka topic **`order.created`**. Message key is **`orderId`**; payload is `OrderCreatedEvent` (orderId, status, totalAmount). Produced by `OrderEventPublisher` after a successful POST. |
| **Saga listener inputs** | Topics such as **`inventory.reserved`**, **`inventory.failed`**, **`payment.succeeded`**, **`payment.failed`**. Payloads must be JSON with an **`orderId`** field (see `SagaStatusListener` logs: `SagaStatusListener raw message [...]`). |

Use your Kafka UI, `kafka-console-consumer`, or order-service logs to match **topic + orderId + payload**.

### Replay the same Kafka message manually

Use the **same** JSON twice against a topic this service consumes (pick one that fits your flow; example: **`inventory.reserved`**). Bootstrap server defaults to **`localhost:29092`** (see `application.properties`).

```bash
export ORDER_ID="<paste-orderId-from-create-order-response>"

# Send the same payload twice (replay duplicate)
for i in 1 2; do
  echo "{\"orderId\":\"$ORDER_ID\"}" | kafka-console-producer \
    --bootstrap-server localhost:29092 \
    --topic inventory.reserved
done
```

Or run the `echo | kafka-console-producer` line **twice** by hand with identical JSON.

To demo a **first** processing again after a successful run, delete the idempotency key (or wait for its TTL, default 24h):

```bash
redis-cli DEL "processed:event:inventory.reserved:$ORDER_ID"
```

### Expected result: first delivery vs replay

| Delivery | What you should see | OrderStore / Redis status cache |
|----------|---------------------|-----------------------------------|
| **First** | `Processing <eventType> for order <orderId>` then normal saga logs (store save, `Cache write` if order found) | Updated on success path |
| **Replayed duplicate** | `Duplicate event ignored: <eventType> for order <orderId>` | **No** store update, **no** status cache write, **no** extra saga side effects |

Duplicates are filtered **before** `OrderStore` and **before** `RedisOrderCacheService.saveOrderStatus`.
