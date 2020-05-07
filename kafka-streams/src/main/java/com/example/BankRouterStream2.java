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

public class BankRouterStream2 {

	public static void main(String[] args) {

		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "bankrouter");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "http://192.168.0.101:9092");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		// create a topology
		StreamsBuilder streamsBuilder = new StreamsBuilder();

		// input topic
		KStream<String, String> inputTopic = streamsBuilder.stream("global_bank_transactions1");
		KStream<String, String> filterICICI = inputTopic.filter((k, v) -> k.contains("icici"));
		filterICICI.to("icici_transactions");

		KStream<String, String> filterHDFC = inputTopic.filter((k, v) -> k.contains("hdfc"));
		filterHDFC.to("hdfc_transactions");

		KStream<String, String> filterYesBank = inputTopic.filter((k, v) -> k.contains("yesbank"));
		filterYesBank.to("yesbank_transactions");
		
		
		// build the topology
		
		KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), props);

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