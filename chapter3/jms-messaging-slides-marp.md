---
marp: true
theme: default
paginate: true
style: |
  section {
    font-size: 28px;
  }
  code {
    font-size: 90%;
  }
  img {
    display: block;
    margin: auto;
  }
---

# Enterprise Messaging with Apache Camel and JMS
## Understanding Messaging Patterns and Implementation

---

# 1. Messaging Systems: The Basics

## What is Messaging?
- **Asynchronous communication** between distributed systems or components
- **Decoupling** of message producers and consumers
- **Reliable delivery** of messages with persistence and transactions
- **Scalability** through load balancing and parallel processing

## Benefits
- Loose coupling between systems
- Temporal decoupling (systems don't need to be online simultaneously)
- Resilience to failures
- Scalability and flexibility

---

# Messaging Architecture

![height:500px](https://miro.medium.com/v2/resize:fit:1400/1*zr1vRXKY11gB3Jm3PcRxRA.png)

---

# 2. Messaging Implementations

## JMS (Java Message Service)
- Java API standard for message-oriented middleware
- Implementations: **ActiveMQ Artemis**, WebSphere MQ, etc.
- Features: queues, topics, transactions, persistence
- Best for: Java-based enterprise applications

## Apache Kafka
- Distributed streaming platform
- High throughput, fault-tolerant, scalable
- Persistent, immutable log of events
- Best for: Big data streaming, event sourcing, real-time analytics

---

# Messaging Implementations (Cont.)

## MQTT (Message Queuing Telemetry Transport)
- Lightweight protocol for IoT and mobile devices
- Implementations: **Eclipse Mosquitto**, HiveMQ, etc.
- Low bandwidth, minimal footprint
- Best for: IoT devices, mobile apps, constrained environments

## RabbitMQ
- Implements AMQP protocol
- Rich routing capabilities (exchanges, bindings)
- Multi-language support
- Best for: Complex routing scenarios, microservices

---

# 3. Typical Use Cases

## Enterprise Integration
- Connect disparate systems without direct coupling
- Legacy system integration
- API gateways with message translation

## Microservices Communication
- Asynchronous service-to-service communication
- Event-driven architectures
- Command and event distribution

---

# Typical Use Cases (Cont.)

## Workload Distribution
- Task queues for job processing
- Load balancing across worker nodes
- Scheduling and prioritization

## IoT and Telemetry
- Collecting sensor data from devices
- Command and control channels
- Managing intermittent connectivity

## Event Sourcing
- Capturing state changes as events
- Event stream processing
- Replaying history for system recovery

---

# 4. Publish-Subscribe Pattern (Topic)

![width:800px](https://www.enterpriseintegrationpatterns.com/img/PublishSubscribeSolution.gif)

- One-to-many communication
- Publishers send messages to topics
- Multiple subscribers receive copies of messages
- Subscribers can filter messages by criteria
- **Temporal coupling**: subscribers must be active to receive messages (unless durable)

---

# 5. Send-Receive Pattern (Queue)

![width:800px](https://www.enterpriseintegrationpatterns.com/img/PointToPointSolution.gif)

- One-to-one communication
- Senders send messages to queues
- Only one consumer receives each message
- Messages persist until processed
- Enables load balancing across multiple consumers
- **Temporal decoupling**: receivers can process messages at their own pace

---

# Request-Reply Pattern

![width:800px](https://www.enterpriseintegrationpatterns.com/img/RequestReply.gif)

- Bidirectional communication
- Requester sends message and waits for response
- Responder processes request and returns result
- Can be implemented using correlation IDs and temporary queues
- Creates synchronous behavior in an asynchronous system

---

# JMS Implementation

## Message Structure
- **Header**: Metadata (ID, timestamps, priority)
- **Properties**: Application-specific properties
- **Body**: Actual message content (text, bytes, objects, etc.)

## Delivery Guarantees
- **At-most-once**: Message might be lost, never delivered twice
- **At-least-once**: Message guaranteed to be delivered, might be duplicated
- **Exactly-once**: Message guaranteed to be delivered exactly once

---

# 6. Building and Running the JMS Receiver

1. Make sure ActiveMQ Artemis is running:
```bash
cd ../../docker
docker-compose up -d
```

2. Build the project:
```bash
cd ../chapter3/jms-receiver
mvn clean package dependency:copy-dependencies
```

3. Run the application:
```bash
java -cp ".:target/chapter3-jms-receiver-1.0-SNAPSHOT.jar:target/dependency/*" \
  com.tutorial.camel.JmsReceiverExample
```

---

# JMS Receiver Implementation

The JMS Receiver application:
- Consumes messages from the `demoQueue` queue
- Subscribes to the `demoTopic` topic
- Uses Apache Camel with YAML route definitions

```yaml
# Route to receive messages from a queue
- route:
    id: "queueReceiverRoute"
    from:
      uri: "jms:queue:{{jms.queue.name}}"
      steps:
        - log:
            message: "Received message from queue: ${body}"

# Route to receive messages from a topic
- route:
    id: "topicSubscriberRoute"
    from:
      uri: "jms:topic:{{jms.topic.name}}"
      steps:
        - log:
            message: "Received message from topic: ${body}"
```

---

# 7. Building and Running the JMS Sender (Queue Only)

1. Build the project:
```bash
cd ../jms-sender
mvn clean package dependency:copy-dependencies
```

2. Run with the `--queue-only` flag:
```bash
java -cp ".:target/chapter3-jms-sender-1.0-SNAPSHOT.jar:target/dependency/*" \
  com.tutorial.camel.JmsSenderExample --queue-only
```

3. Observe queue messages in the receiver output:
```
Received message from queue: [Queue] Message #1: Hello from JMS Queue Sender!...
```

---

# JMS Sender (Queue) Implementation

Queue messages are sent using a timer trigger every 5 seconds:

```java
// Queue Producer route
from("timer:queueSender?period=5000")
    .process(exchange -> {
        int count = exchange.getContext()
            .getRegistry()
            .lookupByNameAndType("queueCounter", AtomicInteger.class)
            .incrementAndGet();
        exchange.setProperty("counter", count);
    })
    .setBody(simple("[Queue] Message #${exchangeProperty.counter}: Hello from JMS Queue Sender!"))
    .log("Sending to queue: ${body}")
    .to("jms:queue:" + queueName);
```

---

# 8. Running the JMS Sender (Topic Only)

1. Run with the `--topic-only` flag:
```bash
java -cp ".:target/chapter3-jms-sender-1.0-SNAPSHOT.jar:target/dependency/*" \
  com.tutorial.camel.JmsSenderExample --topic-only
```

2. Observe topic messages in the receiver output:
```
Received message from topic: [Topic] Message #1: Hello from JMS Topic Publisher!...
```

---

# JMS Sender (Topic) Implementation

Topic messages are published using a timer trigger every 7 seconds:

```java
// Topic Publisher route
from("timer:topicPublisher?period=7000")
    .process(exchange -> {
        int count = exchange.getContext()
            .getRegistry()
            .lookupByNameAndType("topicCounter", AtomicInteger.class)
            .incrementAndGet();
        exchange.setProperty("counter", count);
    })
    .setBody(simple("[Topic] Message #${exchangeProperty.counter}: Hello from JMS Topic Publisher!"))
    .log("Publishing to topic: ${body}")
    .to("jms:topic:" + topicName);
```

---

# Observing Messaging Patterns

## Point-to-Point (Queue) Pattern
- Run multiple JMS Receiver instances
- Send messages with `--queue-only` flag
- **Observe**: Each message is delivered to only one consumer
- Load balancing happens automatically

## Publish-Subscribe (Topic) Pattern
- Run multiple JMS Receiver instances
- Send messages with `--topic-only` flag  
- **Observe**: Each message is delivered to all consumers
- Perfect for broadcasting information

---

# Demonstration Setup

1. Open three terminal windows
2. Terminal 1: Start Receiver #1
```bash
cd chapter3/jms-receiver
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsReceiverExample"
```

3. Terminal 2: Start Receiver #2  
```bash
cd chapter3/jms-receiver
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsReceiverExample"
```

4. Terminal 3: Run both sender modes
```bash
cd chapter3/jms-sender
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsSenderExample" -Dexec.args="--queue-only"
# Then run with --topic-only
```

---

# Summary

## Enterprise Messaging with Apache Camel and JMS
- **Decoupled communication** between distributed systems
- **Point-to-Point (Queue)** for work distribution
- **Publish-Subscribe (Topic)** for broadcasting
- **Apache Camel** simplifies messaging integration
- **ActiveMQ Artemis** provides robust messaging infrastructure

## Next Steps
- Implement Request-Reply patterns
- Add message filtering and transformation
- Explore message persistence and transactions
- Implement error handling and dead letter queues