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


public class BankRouterStream {

	public static void main(String[] args) {

		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "bankrouter10");
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
		
		
		KStream<String, Long> transactionStreamParsedICICI =   banks[0].map( new TransactionParseMapper ()) ;
	    KGroupedStream<String, Long> transactionGroupedStreamICICI =  transactionStreamParsedICICI.groupByKey(Grouped.with(
	        	      Serdes.String(), /* key */
	        	      Serdes.Long()) );
	    KTable<String, Long> balanceICICI =   transactionGroupedStreamICICI.reduce(new BalanceReducer() );
	    KStream<String, Long> balanceStreamICICI = balanceICICI.toStream();
	    KStream<String, String> streamICICI =   balanceStreamICICI.mapValues(value -> "Balance: " + value);
	    
		KStream<String, Long> transactionStreamParsedHDFC =   banks[1].map( new TransactionParseMapper ()) ;
	    KGroupedStream<String, Long> transactionGroupedStreamHDFC =  transactionStreamParsedHDFC.groupByKey(Grouped.with(
	        	      Serdes.String(), /* key */
	        	      Serdes.Long()) );
	    KTable<String, Long> balanceHDFC =   transactionGroupedStreamHDFC.reduce(new BalanceReducer() );
	    KStream<String, Long> balanceStreamHDFC = balanceHDFC.toStream();
	    KStream<String, String> streamHDFC =   balanceStreamHDFC.mapValues(value -> "Balance: " + value);

		KStream<String, Long> transactionStreamParsedYesBank =   banks[2].map( new TransactionParseMapper ()) ;
	    KGroupedStream<String, Long> transactionGroupedStreamYesBank =  transactionStreamParsedYesBank.groupByKey(Grouped.with(
	        	      Serdes.String(), /* key */
	        	      Serdes.Long()) );
	    KTable<String, Long> balanceYesBank =   transactionGroupedStreamYesBank.reduce(new BalanceReducer() );
	    KStream<String, Long> balanceStreamYesBank = balanceYesBank.toStream();
	    KStream<String, String> streamYesBank =   balanceStreamYesBank.mapValues(value -> "Balance: " + value);


	    
	    
	    
	    streamICICI.to("icici_transactions");
		streamHDFC.to("hdfc_transactions");
		streamYesBank.to("yesbank_transactions");
		
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