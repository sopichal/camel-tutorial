# JMS Sender Example

This project demonstrates how to send messages to JMS destinations (queues and topics) using Apache Camel and ActiveMQ Artemis.

## Overview

The JMS Sender application:

- Sends messages to a JMS queue (`demoQueue`) every 5 seconds
- Publishes messages to a JMS topic (`demoTopic`) every 7 seconds
- Uses Apache Camel with ActiveMQ Artemis as the message broker
- Demonstrates both point-to-point (queue) and publish-subscribe (topic) messaging patterns

## Prerequisites

- Java 17 or higher
- Maven
- ActiveMQ Artemis broker running (via Docker or standalone)

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

- `src/main/java/com/tutorial/camel/JmsSenderExample.java` - Main application class
- `src/main/resources/routes/jms-sender-route.yaml` - Camel route definition in YAML
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
# Run both queue and topic producers
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsSenderExample"

# Run only the queue producer
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsSenderExample" -Dexec.args="--queue-only"

# Run only the topic producer
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsSenderExample" -Dexec.args="--topic-only"

# Display help
mvn exec:java -Dexec.mainClass="com.tutorial.camel.JmsSenderExample" -Dexec.args="--help"
```

Or directly with Java (if you've copied dependencies):
```bash
# Run both queue and topic producers
java -cp ".:target/chapter2-jms-sender-1.0-SNAPSHOT.jar:target/dependency/*" com.tutorial.camel.JmsSenderExample

# Run only the queue producer
java -cp ".:target/chapter2-jms-sender-1.0-SNAPSHOT.jar:target/dependency/*" com.tutorial.camel.JmsSenderExample --queue-only

# Run only the topic producer
java -cp ".:target/chapter2-jms-sender-1.0-SNAPSHOT.jar:target/dependency/*" com.tutorial.camel.JmsSenderExample --topic-only

# Display help
java -cp ".:target/chapter2-jms-sender-1.0-SNAPSHOT.jar:target/dependency/*" com.tutorial.camel.JmsSenderExample --help
```

The classpath includes:
- `.` - Current directory (for loading config.properties from current directory)
- `target/chapter2-jms-sender-1.0-SNAPSHOT.jar` - The compiled application
- `target/dependency/*` - All dependencies

## Route Explanation

The application defines two Camel routes:

1. **Queue Sender Route** - Sends messages to a queue at fixed intervals:
   ```yaml
   from("timer:queue-sender?period=5000")
     .setBody(constant("Hello from JMS Queue Sender!"))
     .to("jms:queue:demoQueue")
   ```

2. **Topic Publisher Route** - Publishes messages to a topic at fixed intervals:
   ```yaml
   from("timer:topic-publisher?period=7000")
     .setBody(constant("Hello from JMS Topic Publisher!"))
     .to("jms:topic:demoTopic")
   ```

## Testing

To verify the application is working correctly, run the JMS Receiver application alongside this one. You should see messages being sent to the queue and topic, and then received by the JMS Receiver.