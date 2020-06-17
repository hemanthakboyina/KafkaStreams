package com.example;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class SimpleProducer {
 
 public static void main(String[] args) throws Exception{
    
    String topicName = "sample5";
    
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.105:9092");
	props.put(ProducerConfig.ACKS_CONFIG, "all");
	props.put(ProducerConfig.RETRIES_CONFIG, 0);
	props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
	props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    
	KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
    Future<RecordMetadata> future = null;
    RecordMetadata metadata = null;
    for(int i = 0; i < 10; i++)
		
    	future = producer.send(new ProducerRecord<String, String>(topicName,"kewName"+Integer.toString(i),"keyValue"+Integer.toString(i)));
	metadata = future.get();
	
	/*
	 * producer.send(new ProducerRecord<String, String>(topicName,
	 * "kewName"+Integer.toString(i), "keyValue"+Integer.toString(i))).get();
	 */
		  System.out.println("Message sent successfully");
		 
			/*
			 * producer.send(new ProducerRecord<String, String>(topicName, "Blue Shirt",
			 * "Shirt"));
			 */
             producer.close();
 }
}