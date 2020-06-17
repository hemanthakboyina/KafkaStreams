package com.example;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class SimpleProducer2 {
 
 public static void main(String[] args) throws Exception{
    
    String topicName = "sample2";
    
    Properties props = new Properties();
    props.put("bootstrap.servers", "192.168.0.105:9092");
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", 
       "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", 
       "org.apache.kafka.common.serialization.StringSerializer");
    
    Producer<String, String> producer = new KafkaProducer
       <String, String>(props);
    Future<RecordMetadata> future = null;
    RecordMetadata metadata = null;
    for(int i = 0; i < 10; i++)
		
    	future = producer.send(new ProducerRecord<String, String>(topicName,
    			  "kewName"+Integer.toString(i), "keyValue"+Integer.toString(i)));
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