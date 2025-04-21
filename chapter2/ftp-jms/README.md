# FTP to JMS Example

This module demonstrates how to use Apache Camel to read files from an FTP server and process them through a JMS message broker (ActiveMQ Artemis).

## Prerequisites

- Java 17 or later
- Maven 3.5+
- Docker and Docker Compose (for running ActiveMQ Artemis)
- Python 3.x (for running the FTP server)

## Building the Project

### 1. Build the JAR file

```bash
mvn clean package
```

This will create a JAR file in the `target` directory. The project includes necessary dependencies for:
- Camel Core and Main
- FTP and JMS components
- ActiveMQ Artemis client
- CSV data format for processing CSV files

### 2. Generate Dependencies

To copy all dependencies to the `target/dependency` directory:

```bash
mvn dependency:copy-dependencies
```

## Running the Example

### 1. Start ActiveMQ Artemis

Start the ActiveMQ Artemis message broker using Docker:

```bash
cd ../../docker
docker-compose up -d
```

This will start ActiveMQ Artemis on port 61616 (for JMS) and 8161 (for the web console).

You can access the web console at: http://localhost:8161/console with:
- Username: artemis 
- Password: artemis

### 2. Start the FTP Server

Start the test FTP server:

```bash
cd ../chapter2/ftp-jms/scripts
python3 -m pip install pyftpdlib
python3 ftpserver.py
```

This will start an FTP server on localhost:2121 with:
- Username: rider
- Password: secret

### 3. Configure Properties

Create a `config.properties` file in the `chapter2/ftp-jms` directory:

```bash
cd ../chapter2/ftp-jms
cp src/main/resources/config.properties .
```

The file should contain:

```properties
broker.url=tcp://localhost:61616?retryInterval=1000&retryIntervalMultiplier=2.0&maxRetryInterval=10000&reconnectAttempts=-1
broker.user=artemis
broker.password=artemis
ftp.server=localhost
ftp.port=2121
ftp.user=rider
ftp.password=secret
log.level=DEBUG
```

### 4. Run the Camel Application

In a new terminal, run the compiled application with the current directory added to the classpath:

```bash
cd ../chapter2/ftp-jms
java -cp .:target/chapter2-ftp-jms-1.0-SNAPSHOT.jar:target/dependency/* com.tutorial.camel.FtpToJMSExample
```

This ensures that the `config.properties` file in the current directory is found by the application.

### 5. Test the Flow

In a new terminal, copy the test files to the FTP server's monitored directory:

```bash
cd chapter2/ftp-jms/scripts
cp message1.xml message2.xml order.csv tmp/orders/
```

The Camel application will:
1. Detect the files in the FTP server
2. Process them based on their file type
3. Route them to different JMS queues:
   - XML files to `xmlOrders` queue
   - CSV files to `csvOrders` queue, then convert each entry to XML format and send to `orders` queue
   - Other files to `badOrders` queue

## Stopping the Services

1. Stop the Camel application with Ctrl+C
2. Stop the FTP server with Ctrl+C
3. Stop the ActiveMQ Artemis container:
   ```bash
   cd ../../docker
   docker-compose down
   ```

## Troubleshooting

### Missing Dependencies

If you encounter errors like:
```
Cannot find class: csv
Data format 'csv' could not be created
```

Make sure you've built the project with the latest pom.xml that includes the required dependencies, especially the CSV data format:
```bash
mvn clean package
mvn dependency:copy-dependencies
```

### Classpath Issues

If the application cannot find your config.properties file, ensure that:
1. You have copied the config.properties file to the chapter2/ftp-jms directory
2. You are including the current directory in the classpath with the "." entry:
   ```bash
   java -cp .:target/chapter2-ftp-jms-1.0-SNAPSHOT.jar:target/dependency/* com.tutorial.camel.FtpToJMSExample
   ```