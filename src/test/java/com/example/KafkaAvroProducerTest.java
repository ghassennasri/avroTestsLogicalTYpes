package com.example;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class KafkaAvroProducerTest {
    private static final Logger log = LoggerFactory.getLogger(KafkaAvroProducerTest.class);
    private Properties props;

    @BeforeEach
    void setUp() {
        props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://schema-registry:8081");
    }

    @AfterEach
    void tearDown() {
        // Cleanup resources if necessary
    }

    /* production should fail with message
    /Caused by: org.apache.avro.AvroTypeException: Cannot encode decimal with precision 7 as max precision 4
     */
    @Test
    void testProduceSpecificRecordWithLogicalTypesConverterDisabled_fail() {
        log.info("test produce specific record with logical type converter disabled");
        // Disable logical type converters for this test
        props.remove(KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG);

        KafkaProducer<String, Customer> producer = new KafkaProducer<>(props);

        Customer customer = Customer.newBuilder()
                .setId(1)
                .setName("John Doe")
                .setEmail("johndoe@example.com")
                .setBalance(new BigDecimal("12345.67"))
                .setLastLoginTime(Instant.now())
                //LocalDate
                .setRegistrationDate(java.time.LocalDate.now())
                .build();

        ProducerRecord<String, Customer> record = new ProducerRecord<>("customer-topic", customer.getName().toString(), customer);

        //assertDoesNotThrow(() -> producer.send(record).get());
        Exception exception = assertThrows(org.apache.kafka.common.errors.SerializationException.class,() -> producer.send(record).get());
        log.error("Serialization exception: {}", exception.getMessage(),exception);
        producer.close();
    }
    /* production will succeed

     */
    @Test
    void testProduceSpecificRecordWithLogicalTypesConverterDisabled_succeed() {
        // Disable logical type converters for this test

        props.remove(KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG);
        log.info("test produce specific record with logical type converter disabled");

        KafkaProducer<String, Customer> producer = new KafkaProducer<>(props);

        Customer customer = Customer.newBuilder()
                .setId(1)
                .setName("John Doe")
                .setEmail("johndoe@example.com")
                .setBalance(new BigDecimal("45.67"))
                .setLastLoginTime(Instant.now())
                //LocalDate
                .setRegistrationDate(java.time.LocalDate.now())
                .build();

        ProducerRecord<String, Customer> record = new ProducerRecord<>("customer-topic", customer.getName().toString(), customer);

        RecordMetadata metadata= assertDoesNotThrow(() -> producer.send(record).get());

        log.info("Record produced successfully to partition {} at offset {}", metadata.partition(),metadata.offset());
        producer.close();
    }

    @Test
    void testProduceGenericRecordWithLogicalTypesConvertersEnabled() throws Exception {
        // Enable logical type converters for this test
        props.put(KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG, true);

        KafkaProducer<String, Object> producer = new KafkaProducer<>(props);
        Schema schema;
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("avro/customer.avsc")) {
            schema = new Schema.Parser().parse(schemaStream);
        }

        GenericRecord genericRecord = new GenericData.Record(schema);
        genericRecord.put("id", 1);
        genericRecord.put("name", "Generic Jane Doe");
        genericRecord.put("email", "generic.janedoe@example.com");
        genericRecord.put("balance",new BigDecimal("23.45")); // This should succeed with logical type converters
        genericRecord.put("registrationDate", (int) LocalDate.now().toEpochDay());
        genericRecord.put("lastLoginTime", Instant.now().toEpochMilli());

        ProducerRecord<String, Object> record = new ProducerRecord<>("customer-topic", genericRecord.get("name").toString(), genericRecord);

        assertDoesNotThrow(() -> producer.send(record).get());
        producer.close();
    }

    @Test
    void testProduceGenericRecordWithLogicalTypesConvertersDisabled() throws Exception {
        // Enable logical type converters for this test
        props.put(KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG, false);

        KafkaProducer<String, Object> producer = new KafkaProducer<>(props);
        Schema schema;
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("avro/customer.avsc")) {
            schema = new Schema.Parser().parse(schemaStream);
        }

        GenericRecord genericRecord = new GenericData.Record(schema);
        genericRecord.put("id", 1);
        genericRecord.put("name", "Generic Jane Doe");
        genericRecord.put("email", "generic.janedoe@example.com");
        genericRecord.put("balance", ByteBuffer.wrap(new BigDecimal("23.458").unscaledValue().toByteArray())); // This should succeed with logical type converters
        genericRecord.put("registrationDate", (int) LocalDate.now().toEpochDay());
        genericRecord.put("lastLoginTime", Instant.now().toEpochMilli());

        ProducerRecord<String, Object> record = new ProducerRecord<>("customer-topic", genericRecord.get("name").toString(), genericRecord);

        assertDoesNotThrow(() -> producer.send(record).get());
        producer.close();
    }
}
