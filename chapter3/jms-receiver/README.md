# JMS Receiver Example

This project demonstrates how to receive messages from JMS destinations (queues and topics) using Apache Camel and ActiveMQ Artemis.

## Overview

The JMS Receiver application:

- Consumes messages from a JMS queue (`demoQueue`)
- Subscribes to messages from a JMS topic (`demoTopic`)
- Uses Apache Camel with ActiveMQ Artemis as the message broker
- Demonstrates both point-to-point (queue) and publish-subscribe (topic) messaging patterns

## Prerequisites

- Java 17 or higher
- Maven
- ActiveMQ Artemis broker running (via Docker or standalone)
- JMS Sender application (optional, for testing)

## Configuration

The application configuration is stored in `src/main/resources/config.properties`:

> **Note:** You can also place a `config.properties` file in the current directory when running the application with the classpath that includes `.` (current directory). This allows you to override configuration without modifying the JAR.

```properties
broker.url=tcp://localhost:61616?retryInterval=1000&retryIntervalMultiplier=2.0&maxRetryInterval=10000&reconnectAttempts=-1
broker.user=artemis
broker.password=artemis
jms.queue.name=demoQueue
jms.topic.name=demoTopic
log.level=DEBUG
```

Modify these settings as needed to match your environment.

## Project Structure

- `src/main/java/com/tutorial/camel/JmsReceiverExample.java` - Main application class
- `src/main/resources/routes/jms-receiver-route.yaml` - Camel route definition in YAML
- `src/main/resources/config.properties` - Configuration properties
- `src/main/resources/logback.xml` - Logging configuration

## Running the Application

1. Make sure your ActiveMQ Artemis broker is running. You can start it using Docker:

```bash
cd ../../docker
docker-compose up -d
```

2. Build the project and copy dependencies:

```bash
mvn clean package dependency:copy-dependencies
```

This ensures all required dependencies are available locally.

3. Run the application:

Using Maven:
```bash
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsReceiverExample"
```

Or directly with Java (if you've copied dependencies):
```bash
java -cp ".:target/chapter2-jms-receiver-1.0-SNAPSHOT.jar:target/dependency/*" com.tutorial.camel.JmsReceiverExample
```

The classpath includes:
- `.` - Current directory (for loading config.properties from current directory)
- `target/chapter2-jms-receiver-1.0-SNAPSHOT.jar` - The compiled application
- `target/dependency/*` - All dependencies

## Route Explanation

The application defines two Camel routes:

1. **Queue Receiver Route** - Consumes messages from a queue:
   ```yaml
   from("jms:queue:demoQueue")
     .log("Received message from queue: ${body}")
   ```

2. **Topic Subscriber Route** - Subscribes to messages from a topic:
   ```yaml
   from("jms:topic:demoTopic")
     .log("Received message from topic: ${body}")
   ```

## Testing

To fully test the JMS functionality:

1. Start the ActiveMQ Artemis broker
2. Start this JMS Receiver application
3. Start the JMS Sender application

You should see messages being:
- Produced by the JMS Sender
- Consumed and logged by this JMS Receiver

### Queue vs. Topic Behavior

Observe the different behaviors:

- **Queue (Point-to-Point)**: Each message is consumed by only one receiver. If you run multiple instances of the JMS Receiver, each message will be delivered to only one of them (load-balancing).

- **Topic (Publish-Subscribe)**: Each message is delivered to all active subscribers. If you run multiple instances of the JMS Receiver, all instances will receive copies of each published message.