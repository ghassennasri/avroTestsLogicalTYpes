
# Kafka Avro Producer logical types tests

This playground demonstrates the implementation of a Kafka producer using Avro serialization. It includes a  setup with Zookeeper, Kafka Broker, Schema Registry, Confluent Control Center, and AKHQ for Kafka management and monitoring, showcasing both specific and generic Avro record production using logical types.


## Prerequisites

Before you begin, ensure you have met the following requirements:
- Docker and Docker Compose
- Java Development Kit (JDK) version 17

## Getting Started

To get a local copy up and running, follow these simple steps:

```bash
git clone <repository-url>
cd <project-directory>
```

Replace `<repository-url>` and `<project-directory>` with your repository's URL and your desired local directory name, respectively.



## Setup and Running



Run the following script to start all required services in the correct order:

```bash
./start.sh
```

This script ensures that Zookeeper, Kafka Broker, Schema Registry, Control Center, and AKHQ are fully up before starting the `app-test` service.

### Accessing Control Center and AKHQ

- **Confluent Control Center**: Visit `http://localhost:9021` for Kafka cluster management.
- **AKHQ**: Navigate to `http://localhost:9000` for an alternative Kafka management interface.

## Running Avro record produce Tests

Execute the following command to run a specific test scenario (replace testProduceSpecificRecordWithLogicalTypesConverterDisabled_fail by the test method name):

```bash

CONTAINER=$(docker ps | grep app-test | awk '{print $NF}')

docker exec -it $CONTAINER ./gradlew test --info --tests "com.example.KafkaAvroProducerTest.testProduceSpecificRecordWithLogicalTypesConverterDisabled_fail"

```
or the following to run all test scenarios
```bash

CONTAINER=$(docker ps | grep app-test | awk '{print $NF}')

docker exec -it $CONTAINER ./gradlew test --info 

```
This will start the `app-test` service, which executes the Kafka Avro serialization tests.

Tests results are output to the console and could also be accessed from the container

```bash
docker exec -u -0 -it $CONTAINER  cat /app/build/test-results/test/binary/output.bin

```

## Test Summaries

### `KafkaAvroProducerTest` Class

This class contains tests designed to validate the behavior of a Kafka Avro producer, specifically focusing on Avro serialization with logical types.

#### Tests

1. **testProduceSpecificRecordWithLogicalTypesConverterDisabled_fail**
    - *Purpose*: Verifies failure in producing a specific Avro record when a decimal field exceeds the schema's precision, with logical type converters disabled. This test ensures that improper decimal precision leads to a serialization exception, as expected.

2. **testProduceSpecificRecordWithLogicalTypesConverterDisabled_succeed**
    - *Purpose*: Ensures successful production of a specific Avro record with a decimal field that meets the schema's precision requirements, even when logical type converters are disabled. This confirms correct handling of records that adhere to schema constraints.

3. **testProduceGenericRecordWithLogicalTypesConvertersEnabled**
    - *Purpose*: Tests the production of a generic Avro record with logical type converters enabled (**KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG= true**), focusing on the correct serialization of fields like decimals and dates. The test validates that enabling logical type converters facilitates proper serialization of logical types.

4. **testProduceGenericRecordWithLogicalTypesConvertersDisabled**
    - *Purpose*: Examines the production of a generic Avro record with logical type converters disabled (**KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG= false**), particularly testing manual serialization of decimal fields. It demonstrates that records can still be successfully produced without automatic logical type conversion even though they break the schema contract.

