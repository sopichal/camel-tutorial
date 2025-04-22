# JMS to PostgreSQL Example

This module demonstrates how to use Apache Camel to consume messages from an ActiveMQ Artemis JMS queue and store them in a PostgreSQL database.

## Prerequisites

- Java 17 or later
- Maven 3.5+
- Docker and Docker Compose (for running ActiveMQ Artemis)
- PostgreSQL database

## Building the Project

### 1. Build the JAR file

```bash
mvn clean package
```

This will create a JAR file in the `target` directory. The project includes necessary dependencies for:
- Camel Core and Main
- JMS component
- SQL component
- PostgreSQL JDBC driver
- ActiveMQ Artemis client

### 2. Generate Dependencies

To copy all dependencies to the `target/dependency` directory:

```bash
mvn dependency:copy-dependencies
```

## Database Setup

You can either use a local PostgreSQL installation or use the provided Docker setup.

### Option 1: Using Docker (Recommended)

The project includes PostgreSQL and pgAdmin in the Docker Compose setup:

```bash
cd ../../docker
docker-compose up -d
```

This will:
- Start PostgreSQL on port 5432
- Start pgAdmin web interface on port 5050
- Automatically run the initialization SQL script

To access pgAdmin:
1. Open http://localhost:5050 in your browser
2. Login with:
   - Email: admin@camel.tutorial
   - Password: admin
3. Add a new server connection:
   - Host: postgres
   - Port: 5432
   - Database: camel_tutorial
   - Username: postgres
   - Password: postgres

### Option 2: Using Local PostgreSQL

If you prefer to use a local PostgreSQL installation, run the provided SQL script:

```bash
psql -U postgres -f createdb.sql
```

The script will:
- Create a new database called `camel_tutorial`
- Create the `purchase_orders` table
- Create an audit table called `message_log`
- Create useful views and indexes

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

### 2. Configure Properties

Create a `config.properties` file in the `chapter2/jms-postgresql` directory:

```bash
cd ../chapter2/jms-postgresql
cp src/main/resources/config.properties .
```

Modify the PostgreSQL connection settings if needed:

```properties
# PostgreSQL Configuration
db.url=jdbc:postgresql://localhost:5432/camel_tutorial
db.user=postgres
db.password=postgres
```

### 3. Run the Camel Application

In a terminal, run the compiled application with the current directory added to the classpath:

```bash
cd ../chapter2/jms-postgresql
java -cp .:target/chapter2-jms-postgresql-1.0-SNAPSHOT.jar:target/dependency/* com.tutorial.camel.JmsToPostgresqlExample
```

### 4. Generate Test Messages

This application consumes from the `jms:xmlOrders` queue, which is populated by the `ftp-jms` project. To test the complete flow:

1. Start the FTP server and the `ftp-jms` application as described in the [FTP to JMS README](../ftp-jms/README.md)
2. Copy XML files to the FTP server
3. The `ftp-jms` application will process these files and send them to the `jms:xmlOrders` queue
4. This application will consume from the `jms:xmlOrders` queue and insert the data into PostgreSQL

## Checking Results

### Using pgAdmin

The easiest way to check the results is through pgAdmin:
1. Open http://localhost:5050 in your browser
2. Navigate to Servers > postgres > Databases > camel_tutorial > Schemas > public > Tables > purchase_orders
3. Right-click on purchase_orders and select "View/Edit Data" > "All Rows"

### Using Command Line

To verify that the orders have been inserted into the database using CLI:

```bash
# If using Docker PostgreSQL:
docker exec -it postgres psql -U postgres -d camel_tutorial -c "SELECT * FROM purchase_orders;"

# If using local PostgreSQL:
psql -U postgres -d camel_tutorial -c "SELECT * FROM purchase_orders;"
```

## Stopping the Services

1. Stop the Camel application with Ctrl+C
2. Stop all Docker containers:
   ```bash
   cd ../../docker
   docker-compose down
   ```

This will stop and remove:
- ActiveMQ Artemis
- PostgreSQL 
- pgAdmin

## Troubleshooting

### Database Connection Issues

If you encounter database connection issues:

1. Ensure PostgreSQL is running and accessible
2. Check that the database user has proper permissions
3. Verify the connection details in config.properties match your PostgreSQL setup

### Message Processing Issues

If you're not seeing messages processed:

1. Check that the `ftp-jms` project is running and processing XML files correctly
2. Use the ActiveMQ Artemis web console to verify messages are in the `xmlOrders` queue
3. Inspect the application logs for any errors in message processing