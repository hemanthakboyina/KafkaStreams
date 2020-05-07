package com.example;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import com.google.gson.JsonParser;

import com.mapper.*;


public class BankRouterStreamTimeStamp {

	public static void main(String[] args) {

		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "bankrouter9");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "http://192.168.0.102:9092");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		// create a topology
		StreamsBuilder streamsBuilder = new StreamsBuilder();

		// input topic
		KStream<String, String> inputTopic = streamsBuilder.stream("global_bank_transactions");

		@SuppressWarnings("unchecked")
		KStream<String, String>[] banks = inputTopic.branch(
			    (key, value) -> key.contains("icici"), 
			    (key, value) -> key.contains("hdfc"), 
			    (key, value) -> key.contains("yesbank")
			  );
		
	    
		
		  KStream<String, String> transformedICICI = banks[0].mapValues(value ->
		  value.concat("timeStamp: " + Long.toString(System.currentTimeMillis())));
		  KStream<String, String> transformedHDFC = banks[1].mapValues(value ->
		  value.concat("timeStamp: " + Long.toString(System.currentTimeMillis())));
		  KStream<String, String> transformedYesBank = banks[2].mapValues(value ->
		  value.concat("timeStamp: " + Long.toString(System.currentTimeMillis())));
		 
		
		transformedICICI.to("icici_transactions1");
		transformedHDFC.to("hdfc_transactions1");
		transformedYesBank.to("yesbank_transactions1");
		
		// build the topology
		
		//KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), props);

		final Topology topology = streamsBuilder.build();

		System.out.println(topology.describe());

		final KafkaStreams streams = new KafkaStreams(topology, props);

		final CountDownLatch latch = new CountDownLatch(1);

		// attach shutdown handler to catch control-c
		Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
			@Override
			public void run() {
				streams.close();
				latch.countDown();
			}
		});

		try {
			streams.start();
			latch.await();
		} catch (Throwable e) {
			System.exit(1);
		}
		System.exit(0);

	}

}