---
marp: true
theme: default
paginate: true
backgroundColor: #fff
header: 'Apache Camel: JMS to PostgreSQL Integration'
footer: 'Camel Tutorial'
style: |
  section {
    font-size: 1.5rem;
  }
  code {
    background-color: #f0f0f0;
  }
  h1 {
    color: #369;
  }
  h2 {
    color: #369;
  }
---

# Camel Integration: JMS to PostgreSQL 

![bg right:40% 70%](https://camel.apache.org/img/logo-d.svg)

---

## Recommended Resources

### Books
- **Camel in Action** (2nd Edition) by Claus Ibsen and Jonathan Anstey 
  - Manning Publications, 2018
  - The definitive guide to Apache Camel

- **Enterprise Integration Patterns** by Gregor Hohpe, Bobby Woolf 
  - Addison-Wesley, 2003
  - Foundation of integration patterns used in Camel

### Source Code
- Examples based on code from: 
  https://github.com/camelinaction/camelinaction2

![bg right:30% 90%](https://images.manning.com/360/480/resize/book/d/22ddb18-a91c-4f1b-95cc-b74113d7284a/Ibsen-Camel-2ed-HI.png)

---

## Overview

- Consume XML messages from ActiveMQ Artemis JMS queue
- Parse XML order data
- Store order information in PostgreSQL database
- Complete the end-to-end integration flow
- Demonstrate enterprise integration patterns

---

## The Challenge

- Need to persist order data from messaging system
- XML messages contain structured order information
- Must reliably store orders in relational database
- Ensure proper data mapping between formats

**XML Message Format**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<order name="Camel in Action" amount="2" customer="joe's books"/>
```

---

## Solution Architecture

![bg right:40% 85%](https://mermaid.ink/img/pako:eNptkL0OwjAMhF8l8tQuSGVjYGGGZ6rKoYmjVhDbxC5UCPXdyQGJIcrdffe5sx-RnEWEoXIdie3UnFWtzFDS8co-Br9Nh7wkB5Oxy21pDUsfS68RrNzDXfpxfHJuF0u2q5jsIVlN0XQls_4GZymeJgX-oSxVsjOduYE8-kpGEr7tQ1JMFtQ3AQ3cnGkhahstqjsuhsLqNBWqg0OEjbN6waQe4FOgoTjXTHCq8wvRCzwa)

---

## JMS to PostgreSQL Route

```java
from("jms:xmlOrders")
  .routeId("jmsToPostgresRoute")
  .log("Received XML order: ${body}")
  .process(exchange -> {
    // Parse XML message and extract order details
    String xml = exchange.getIn().getBody(String.class);
    
    // Simple XML parsing (in production, use proper XML parsing)
    String name = extractXmlAttribute(xml, "name");
    String amount = extractXmlAttribute(xml, "amount");
    String customer = extractXmlAttribute(xml, "customer");
    
    // Create parameters map for SQL insert
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("order_name", name);
    parameters.put("amount", Integer.parseInt(amount));
    parameters.put("customer", customer);
    
    // Set the parameters as body
    exchange.getIn().setBody(parameters);
  })
  .to("sql:INSERT INTO purchase_orders(order_name, amount, customer) VALUES (:#order_name, :#amount, :#customer)")
  .log("Order inserted into database: ${body}");
```

---

## Database Structure

```sql
CREATE TABLE IF NOT EXISTS purchase_orders (
    id SERIAL PRIMARY KEY,
    order_name VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    customer VARCHAR(255) NOT NULL,
    received_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS message_log (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255),
    source_queue VARCHAR(100),
    processing_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message_content TEXT,
    processing_status VARCHAR(50)
);
```

---

## Key Components Used

- **JMS Component**: Consumes messages from ActiveMQ Artemis
  - Connection pooling and transaction support

- **Processor**: Transforms XML to database parameters
  - Parses XML attributes
  - Creates parameter map for SQL component

- **SQL Component**: Executes database operations
  - Named parameter support
  - Connection pooling with DBCP2

---

## Implementation Setup

1. **Configure PostgreSQL datasource**
```java
BasicDataSource dataSource = new BasicDataSource();
dataSource.setDriverClassName("org.postgresql.Driver");
dataSource.setUrl(dbUrl);
dataSource.setUsername(dbUser);
dataSource.setPassword(dbPassword);
```

2. **Configure SQL component**
```java
SqlComponent sqlComponent = new SqlComponent();
sqlComponent.setDataSource(dataSource);
context.addComponent("sql", sqlComponent);
```

---

## Key Dependencies

```xml
<!-- JMS Component -->
<dependency>
  <groupId>org.apache.camel</groupId>
  <artifactId>camel-jms</artifactId>
  <version>${camel.version}</version>
</dependency>

<!-- SQL Component -->
<dependency>
  <groupId>org.apache.camel</groupId>
  <artifactId>camel-sql</artifactId>
  <version>${camel.version}</version>
</dependency>

<!-- PostgreSQL JDBC Driver -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <version>${postgresql.version}</version>
</dependency>
```

---

## Testing the Integration

### Complete Flow Testing

1. Start infrastructure components
   ```bash
   cd docker && docker-compose up -d
   ```

2. Start the FTP-JMS application
   ```bash
   cd chapter2/ftp-jms
   java -cp .:target/chapter2-ftp-jms-1.0-SNAPSHOT.jar:target/dependency/* com.tutorial.camel.FtpToJMSExample
   ```

3. Start the JMS-PostgreSQL application
   ```bash
   cd ../jms-postgresql
   java -cp .:target/chapter2-jms-postgresql-1.0-SNAPSHOT.jar:target/dependency/* com.tutorial.camel.JmsToPostgresqlExample
   ```

---

## Verifying Results

### Using pgAdmin

1. Open http://localhost:5050 in your browser
2. Login with admin@camel.tutorial / admin
3. Navigate to: Servers > postgres > Databases > camel_tutorial
4. View purchase_orders table data

### Using Command Line

```bash
docker exec -it postgres psql -U postgres -d camel_tutorial -c "SELECT * FROM purchase_orders;"
```

---

## Benefits and Extensions

- **Decoupled Services**: JMS provides loose coupling between components
- **Reliable Persistence**: Database transaction support
- **Scalability**: Components can scale independently
- **Monitoring**: Message and database activity can be tracked

### Future Extensions

- Add error handling with error queue
- Implement idempotent consumer pattern
- Add audit logging for message processing
- Implement database connection retry strategies