---
marp: true
theme: default
paginate: true
backgroundColor: #fff
header: 'Apache Camel: CSV to XML Integration'
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

# Camel Integration: Converting CSV to XML Messages

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

- Enhanced `FtpToJMSExample.java` with CSV to XML transformation
- Added route from `jms:csvOrders` to `jms:xmlOrders` queue
- Implemented per-line processing for CSV data
- Used standard Camel components and processors

---

## The Challenge

- Need to process CSV files from FTP server
- Each CSV line represents a separate order
- Orders must be converted to standard XML format
- Must integrate with existing XML processing systems

```
Camel in Action,joe's books,1
Activemq in Action,joe's books,2
```

→ Convert to XML messages →

```xml
<?xml version="1.0" encoding="UTF-8"?>
<order name="Camel in Action" amount="2" customer="joe's books"/>
```

---

## Solution Architecture

![bg right:40% 85%](https://mermaid.ink/img/pako:eNpdkLEOwjAMRH8l8tQuSGXLwsIMYqrKoYmjRhA7xAYqhPru5IBgiLzd-84n-xrRGkQYKtdRv07NWdXK9CUdr-xj8Nt0PJTk_NSxc1v08Hgbx9EgG8t2gkyyj6bPyepv-yxL8dPf8YuylJEzKe9VDp7USMy3eYjFWdA8BdRwc6aF2NtoNHdYLITVkRfVwCHC2hldMLMH6BVoKE6dZ-h1fgBxADsH)

---

## CSV Processing Route

```java
from("jms:csvOrders")
  .log("Received CSV order: ${header.CamelFileName}")
  .unmarshal().csv()
  .split(body())
  .process(exchange -> {
    // Extract CSV values & create XML
    String[] columns = exchange.getIn().getBody(String[].class);
    if (columns != null && columns.length >= 3) {
      String name = columns[0];
      String amount = columns[1];
      String customer = columns[2];
      
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<order name=\"" + name + "\" amount=\"" + 
                  amount + "\" customer=\"" + customer + "\"/>";
      
      exchange.getIn().setBody(xml);
    }
  })
  .to("jms:xmlOrders");
```

---

## Key Components Used

- **Unmarshal().csv()**: Converts raw CSV data to structured format
  - Requires `camel-csv` dependency

- **Split(body())**: Processes each CSV line separately 
  - Each line becomes an individual exchange

- **Processor**: Transforms data format
  - Extracts fields from CSV
  - Constructs XML document

- **JMS Destination**: Routes messages to target queue

---

## Implementation Steps

1. **Add CSV dependency**
   ```xml
   <dependency>
     <groupId>org.apache.camel</groupId>
     <artifactId>camel-csv</artifactId>
     <version>4.4.0</version>
   </dependency>
   ```

2. **Uncomment CSV route in `FtpToJMSExample.java`**

3. **Add CSV processing logic**
   - Parse CSV
   - Transform to XML
   - Route to `jms:xmlOrders` queue

---

## Testing the Integration

1. Start ActiveMQ Artemis
   ```bash
   cd docker && docker-compose up -d
   ```

2. Start test FTP server
   ```bash
   cd scripts && python3 ftpserver.py
   ```

3. Upload CSV file to FTP server
   ```bash
   cp order.csv tmp/orders/
   ```

4. Verify messages appear in `xmlOrders` queue via Artemis console
   - http://localhost:8161/console

---

## Benefits and Extensions

- **Unified Processing**: CSV and XML share same destination queue
- **Format Normalization**: All orders handled as XML regardless of source
- **Simplified Downstream**: Consumers only need to handle XML format 
- **Future Extensions**:
  - Add data validation
  - Include error handling
  - Support additional input formats (JSON, EDI, etc.)