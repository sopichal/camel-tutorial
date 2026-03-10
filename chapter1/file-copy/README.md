# File Copy

This project demonstrates two approaches to copying files from `data/inbox` to `data/outbox`:

1. **FileCopier** — plain Java using `FileInputStream`/`FileOutputStream`
2. **FileCopierWithCamel** — Apache Camel route using the File component (Java DSL)
3. **FileCopierWithYamlDSL** — Apache Camel route using YAML DSL

## Prerequisites

- Java 17+
- Maven

## Build

```bash
mvn clean package
```

## Prepare test data

Place one or more files into the `data/inbox` directory:

```bash
mkdir -p data/inbox
echo "Hello, World!" > data/inbox/test.txt
```

## Run

### FileCopier (plain Java)

Copies all files from `data/inbox` to `data/outbox` once and exits.

```bash
mvn exec:java -Dexec.mainClass="com.tutorial.camel.FileCopier"
```

### FileCopierWithCamel (Apache Camel)

Polls `data/inbox` every second and copies new files to `data/outbox`. Runs for ~100 seconds, then stops. The `noop=true` option means source files are not moved or deleted.

```bash
mvn exec:java -Dexec.mainClass="com.tutorial.camel.FileCopierWithCamel"
```

### FileCopierWithYamlDSL (Apache Camel — YAML DSL)

Same behavior as `FileCopierWithCamel`, but the route is defined in `src/main/resources/routes/file-copy-route.yaml` instead of Java code. Uses `camel-main` to load the YAML route automatically. Runs until stopped with Ctrl+C.

```bash
mvn exec:java -Dexec.mainClass="com.tutorial.camel.FileCopierWithYamlDSL"
```
