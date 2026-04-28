# Messaging Module — TIBCO EMS & Apache Camel JMS Patterns

This module demonstrates fundamental JMS messaging patterns using Apache Camel and TIBCO EMS (Enterprise Message Service) 8.5.1 as the message broker.

## Technical Notes

**Camel Version**: This module uses **Camel 3.21.4 (LTS)** instead of 4.4.0 (used elsewhere in the tutorial) because:
- TIBCO EMS 8.5.1 uses the `javax.jms` namespace (older)
- Camel 4.4.0 requires `jakarta.jms` namespace (Spring 6.x)
- These namespaces are incompatible without a bridge layer
- Camel 3.21.4 is LTS and uses `javax.jms`, making it fully compatible with TIBCO EMS 8.5.1

This is a specific configuration for messaging with legacy TIBCO EMS. Modern projects should use Camel 4.x with newer TIBCO EMS or other Jakarta-compatible brokers.

## Overview

Six standalone Maven projects showcase core JMS patterns:

| Project | Pattern | Description |
|---|---|---|
| **jms-sender** | Point-to-Point (Queue) | Producer sends XML order messages to a queue on a timer |
| **jms-receiver** | Point-to-Point (Queue) | Consumer listens to queue, receives and logs order messages |
| **jms-request-reply-requester** | Request-Reply (Client) | Requester sends requests with UUID correlation IDs, waits for replies (run multiple instances) |
| **jms-request-reply-responder** | Request-Reply (Server) | Responder listens for requests, processes them, sends replies with matching correlation IDs |
| **jms-topic-publisher** | Publish-Subscribe (Topic) | Publisher sends events to a topic every 6 seconds |
| **jms-topic-subscriber** | Publish-Subscribe (Topic) | Subscriber listens to topic, receives and logs events (run multiple instances) |

## Prerequisites

- TIBCO EMS 8.5.1 running in Docker (see `docker/README.md`)
- Maven 3.5.0+
- Java 17+

## Setup: TIBCO JMS JAR Installation

The TIBCO EMS client JAR files must be extracted from the running container and installed to your local Maven repository. **This has already been done if you ran the initial setup commands**, but here's how to verify or repeat:

### Verify Installation

```bash
ls ~/.m2/repository/com/tibco/tibjms/8.5.1/
ls ~/.m2/repository/javax/jms/jms/2.0/
```

If the files exist, setup is complete.

### Manual Installation (if needed)

```bash
cd messaging

# Extract jars from running container
docker cp ems:/opt/tibco/ems/8.5/lib/tibjms.jar lib/
docker cp ems:/opt/tibco/ems/8.5/lib/jms-2.0.jar lib/

# Install to local Maven repository
mvn install:install-file \
  -Dfile=lib/tibjms.jar \
  -DgroupId=com.tibco -DartifactId=tibjms -Dversion=8.5.1 -Dpackaging=jar

mvn install:install-file \
  -Dfile=lib/jms-2.0.jar \
  -DgroupId=javax.jms -DartifactId=jms -Dversion=2.0 -Dpackaging=jar
```

## Building

Build all three projects:

```bash
cd messaging
mvn clean package
```

Build a single project:

```bash
cd messaging/jms-sender
mvn clean package
```

## Running the Demos

### Demo 1: Point-to-Point (Sender & Receiver)

**Terminal 1 — Start the receiver** (listens on `CAMEL.ORDER.QUEUE`):

```bash
cd messaging/jms-receiver
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsReceiverExample"
```

**Terminal 2 — Start the sender** (sends XML orders every 5 seconds):

```bash
cd messaging/jms-sender
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsSenderExample"
```

**Expected output:**
- Sender logs: `Sending to queue [order-1.xml]: <order>...</order>`
- Receiver logs: `Received message from queue... Order ID: ORD-001...`

Messages cycle through `order-1.xml`, `order-2.xml`, `order-3.xml`.

### Demo 2: Request-Reply Pattern (Independent Requester & Responder)

This demo runs requester and responder as **separate processes** with **UUID correlation IDs** for proper request-response matching. Multiple requesters can run concurrently, each receiving only their own responses.

**Terminal 1 — Start the responder** (listens on `CAMEL.REQUEST.QUEUE`):

```bash
cd messaging/jms-request-reply-responder
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsRequestReplyResponderExample"
```

**Terminal 2 — Start a requester** (sends requests every 8 seconds):

```bash
cd messaging/jms-request-reply-requester
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsRequestReplyRequesterExample"
```

**Terminal 3 — (Optional) Start another requester** (each gets its own responses):

```bash
cd messaging/jms-request-reply-requester
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsRequestReplyRequesterExample"
```

**Expected output:**

Requester:
```
Requester sending request with CorrelationId: 32ed77eb-079c-43a6-8c07-5ee9a4d37616 | Body: <request><orderNumber>ORD-123</orderNumber>...
Requester received reply for CorrelationId: 32ed77eb-079c-43a6-8c07-5ee9a4d37616 | Response: <response>...
```

Responder:
```
Responder received request with CorrelationId: 32ed77eb-079c-43a6-8c07-5ee9a4d37616 | Body: <request><orderNumber>ORD-123</orderNumber>...
Responder sending reply for CorrelationId: 32ed77eb-079c-43a6-8c07-5ee9a4d37616 | Response: <response>...
```

**Key feature**: Each request is assigned a unique UUID as the `JMSCorrelationID`. The responder extracts this ID from incoming requests and includes it in the response, ensuring each requester receives only replies to its own requests. This enables multiple concurrent clients to safely share the same request and reply queues.

### Demo 3: Publish-Subscribe (Topic) Pattern

**Terminal 1 — Start first subscriber**:

```bash
cd messaging/jms-topic-subscriber
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsTopicSubscriberExample"
```

**Terminal 2 — Start second subscriber** (in another terminal, same command):

```bash
cd messaging/jms-topic-subscriber
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsTopicSubscriberExample"
```

**Terminal 3 — Start the publisher**:

```bash
cd messaging/jms-topic-publisher
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsTopicPublisherExample"
```

**Expected output:**
```
[Publisher] Publishing to topic: Event: OrderCreated - ...
[Subscriber #1] Received event at 2026-04-27 22:29:22: Event: OrderCreated - ...
[Subscriber #2] Received event at 2026-04-27 22:29:22: Event: OrderCreated - ...
[Publisher] Publishing to topic: Event: PaymentProcessed - ...
[Subscriber #1] Received event at 2026-04-27 22:29:28: Event: PaymentProcessed - ...
[Subscriber #2] Received event at 2026-04-27 22:29:28: Event: PaymentProcessed - ...
```

**Key observation**: Each published event is received by **all** subscribers. This is the fundamental difference from point-to-point queues where each message goes to only one consumer.

You can run as many subscriber instances as needed — each will receive a copy of every published event.

## Message Formats

### Order Message (jms-sender)

Sample XML order from `jms-sender/src/main/resources/data/order-1.xml`:

```xml
<order xmlns="http://tutorial.camel.com/orders">
  <orderId>ORD-001</orderId>
  <customer>
    <id>C001</id>
    <name>Acme Corporation</name>
  </customer>
  <items>
    <item>
      <sku>WIDGET-A</sku>
      <quantity>10</quantity>
      <unitPrice>29.99</unitPrice>
    </item>
  </items>
  <total>329.89</total>
  <currency>USD</currency>
</order>
```

Three order files demonstrate varied payloads (different customer, items, totals).

### Request-Reply Messages

**Request** (sent by requester with UUID correlation ID):
```xml
<request>
  <orderNumber>ORD-456</orderNumber>
  <status>PENDING</status>
</request>
```
JMS Header: `JMSCorrelationID: 32ed77eb-079c-43a6-8c07-5ee9a4d37616`

**Reply** (sent by responder with matching correlation ID):
```xml
<response>
  <orderNumber>ORD-APPROVED</orderNumber>
  <status>APPROVED</status>
  <processedAt>1715632245000</processedAt>
  <correlationId>32ed77eb-079c-43a6-8c07-5ee9a4d37616</correlationId>
</response>
```
JMS Header: `JMSCorrelationID: 32ed77eb-079c-43a6-8c07-5ee9a4d37616`

The correlation ID is:
1. Generated as a UUID by the requester before sending the request
2. Set as the `JMSCorrelationID` header (required by JMS spec)
3. Extracted by the responder from incoming requests
4. Embedded in the response body (for logging/debugging)
5. Set as the `JMSCorrelationID` header in the reply
6. Matched by the JMS component to route replies back to the correct requester

## Queue & Topic Names

**Queues (Point-to-Point):**
- `CAMEL.ORDER.QUEUE` — main queue for sender/receiver demo
- `CAMEL.REQUEST.QUEUE` — request queue for request-reply pattern
- `CAMEL.REPLY.QUEUE` — reply queue for request-reply pattern

**Topics (Publish-Subscribe):**
- `CAMEL.EVENTS.TOPIC` — topic for publisher/subscriber demo

All queues and topics are created dynamically by TIBCO EMS (via the `">"` wildcard in `tibemsd.json`). No manual configuration required.

## Configuration

Each project reads `config.properties`:

```properties
broker.url=tcp://localhost:7222
broker.user=admin
broker.password=
jms.queue.name=CAMEL.ORDER.QUEUE
log.level=DEBUG
```

Edit the queue names or broker URL in each project's `src/main/resources/config.properties` as needed.

## Architecture

All six projects follow the same core pattern:

1. **Main class** (e.g., `JmsSenderExample`, `JmsReceiverExample`, `JmsRequestReplyRequesterExample`)
   - Creates a `Main` instance and registers the route builder
   - Configures the TIBCO JMS component with `TibjmsConnectionFactory`
   - Loads properties from `config.properties`

2. **Route builder** (e.g., `OrderSenderRoute`, `OrderReceiverRoute`, `JmsRequestReplyRequesterRoute`)
   - Defines the Camel route (DSL) that processes messages
   - Sender: timer → load XML file → send to JMS queue
   - Receiver: consume from JMS queue → log message
   - Topic Publisher: timer → generate event → publish to JMS topic
   - Topic Subscriber: consume from JMS topic → log event
   - **Requester**: timer → generate UUID correlation ID → send request to queue with replyTo header → wait for reply → log response
   - **Responder**: consume request from queue → extract correlation ID → process → send response to reply queue with same correlation ID → log reply

3. **Configuration** (`config.properties`)
   - Broker URL: `tcp://localhost:7222`
   - Broker credentials: `admin` / empty password
   - Queue/topic names specific to each pattern

4. **Logging** (`logback.xml`)
   - Console output with timestamp, thread, level, logger name, message
   - Camel and TIBCO debug logging at DEBUG level

### Request-Reply Pattern: Correlation ID Flow

```
Requester Process                          JMS Broker                     Responder Process
───────────────────────────────────────────────────────────────────────────────────────────
1. Generate UUID
   correlationId = "32ed77eb-..."
                                ├─ Set JMSCorrelationID
                                ├─ Send to CAMEL.REQUEST.QUEUE ──→
                                │                                  2. Consume from queue
                                │                                  3. Extract JMSCorrelationID
                                │                                  4. Process request
                                │                                  5. Create response with same ID
   6. Wait for reply            ├─ Set JMSCorrelationID
                                ├─ Send to CAMEL.REPLY.QUEUE ←──
                                │
   7. Receive reply
   8. Verify CorrelationId matches
   9. Log response
```

Multiple requesters can run concurrently — each gets its own UUID, and the JMS component automatically routes replies based on the `JMSCorrelationID` header.

## Troubleshooting

### Cannot connect to TIBCO EMS

- Verify EMS is running: `docker-compose ps` (in `messaging/docker/`)
- Check broker URL: should be `tcp://localhost:7222`
- Ensure Docker network is accessible from your host

### Messages not appearing on receiver

- Ensure sender is running and logging "Sending to queue..."
- Check EMS logs: `docker-compose logs ems`
- Verify queue name matches in both projects

### "ClassNotFoundException: com.tibco.tibjms.TibjmsConnectionFactory"

- Jars not installed to local Maven repo
- Run the `mvn install:install-file` commands above
- Verify files in `~/.m2/repository/com/tibco/` and `~/.m2/repository/javax/jms/`

## Pattern Comparison

| Aspect | Queue (P2P) | Topic (Pub-Sub) | Request-Reply (Bidirectional) |
|---|---|---|---|
| **Destinations** | `CAMEL.ORDER.QUEUE` | `CAMEL.EVENTS.TOPIC` | Request: `CAMEL.REQUEST.QUEUE`, Reply: `CAMEL.REPLY.QUEUE` |
| **Delivery** | One consumer per message | All subscribers get a copy | Requester receives reply matching its request (via correlation ID) |
| **Use Case** | Tasks, orders, reliable delivery | Events, notifications, broadcasts | RPC-style request-response, API calls, service-to-service |
| **Persistence** | Stored until consumed | Lost if no subscribers listening | Request stored until processed; reply stored until retrieved |
| **Concurrency** | Multiple consumers load-balance | All subscribers process independently | Multiple requesters each get their own replies (UUID correlation) |
| **Projects** | jms-sender, jms-receiver | jms-topic-publisher, jms-topic-subscriber | jms-request-reply-requester, jms-request-reply-responder |

## Next Steps

- Modify message payloads in `jms-sender/src/main/resources/data/` to test different order structures
- Add message transformation (e.g., XML → JSON) in receiver/subscriber routes
- Run multiple subscriber instances for the pub-sub demo to see all receive copies
- Add error handling and dead-letter queue configuration
- Monitor message throughput via TIBCO EMS admin console

## References

- [Apache Camel JMS Component](https://camel.apache.org/components/4.4.x/jms-component.html)
- [TIBCO EMS Documentation](https://docs.tibco.com/pub/ems/latest/)
- [Docker TIBCO EMS Setup](docker/README.md)
- Parent repository: [Camel in Action Tutorial](../)
