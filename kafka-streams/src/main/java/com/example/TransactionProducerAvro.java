package com.example;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class TransactionProducerAvro {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		String topic = "global_bank_transactions";
		
		final Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.101:9092");
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.setProperty("value.serializer",  KafkaAvroSerializer.class.getName());
		props.setProperty("schema.registry.url", "http://192.168.0.101:8081");

		KafkaProducer<String, GlobalBank> kafkaProducer = new KafkaProducer<String, GlobalBank>(props);

		
		GlobalBank txn = GlobalBank.newBuilder()
				.setBankname("icici")
				.setAccountname("vineet")
				.setAmount(100)
				.build();
		ProducerRecord<String, GlobalBank> producerRecord1 = new ProducerRecord<String, GlobalBank>(topic, txn.getBankname().toString(), txn);

		Future<RecordMetadata> future = kafkaProducer.send(producerRecord1);
		RecordMetadata metadata = future.get();
		System.out.println(metadata.offset() + " " + metadata.partition() + " " + metadata.topic());

		GlobalBank txn2 = GlobalBank.newBuilder()
				.setBankname("hdfc")
				.setAccountname("chiru")
				.setAmount(200)
				.build();
		ProducerRecord<String, GlobalBank> producerRecord2 = new ProducerRecord<String, GlobalBank>(topic, txn2.getBankname().toString(), txn2);

		future = kafkaProducer.send(producerRecord2);
		metadata = future.get();
		System.out.println(metadata.offset() + " " + metadata.partition() + " " + metadata.topic());
		
		GlobalBank txn3 = GlobalBank.newBuilder()
				.setBankname("yesbank")
				.setAccountname("vineet")
				.setAmount(100)
				.build();
		ProducerRecord<String, GlobalBank> producerRecord3 = new ProducerRecord<String, GlobalBank>(topic, txn3.getBankname().toString(), txn3);

		future = kafkaProducer.send(producerRecord3);
		metadata = future.get();
		System.out.println(metadata.offset() + " " + metadata.partition() + " " + metadata.topic());
		
		GlobalBank txn4 = GlobalBank.newBuilder()
				.setBankname("icici")
				.setAccountname("vineet")
				.setAmount(100)
				.build();
		ProducerRecord<String, GlobalBank> producerRecord = new ProducerRecord<String, GlobalBank>(topic, txn4.getBankname().toString(), txn4);

		future = kafkaProducer.send(producerRecord);
		 metadata = future.get();
		System.out.println(metadata.offset() + " " + metadata.partition() + " " + metadata.topic());
		
		kafkaProducer.flush();
		kafkaProducer.close();
	}

}
