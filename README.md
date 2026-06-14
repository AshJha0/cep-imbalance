# CEP Imbalance – Java (Maven) Project

## Requirements
- JDK 17+
- Maven 3.8+

## Build & Run
```bash
mvn -q -DskipTests package
java -jar target/cep-imbalance-1.0.0-shaded.jar
```

## Run Unit Tests
```bash
mvn -q test
```

## Run JMH Benchmark
```bash
mvn -q -DskipTests test
# Or directly run via Maven Surefire if you set it up; simplest is:
java -jar target/benchmarks.jar   # if you package a JMH fat-jar separately
```

For this project, benchmarks are under `src/test/java` with JMH; execute via your IDE or use the JMH plugin of choice.

## Disruptor Pipeline Demo
The app demonstrates:
- `ImbalanceProcessor` (core CEP)
- `DisruptorPipeline` (SPSC ring)
- `MultiSymbolLoadGen` (N-symbol synthetic publisher)

Run the shaded jar and watch alerts and clears printed to console.
