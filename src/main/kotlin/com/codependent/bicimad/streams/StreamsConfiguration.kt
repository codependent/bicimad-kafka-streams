package com.codependent.bicimad.streams

import com.codependent.bicimad.dto.BiciMadStation
import com.codependent.bicimad.serdes.JsonPojoDeserializer
import com.codependent.bicimad.serdes.JsonPojoSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

const val STATIONS_TOPIC = "bicimad-stations"
const val STATIONS_STORE = "bicimad-stations-store"
const val STATIONS_BY_NAME_STORE = "bicimad-stations-by-name-store"

@Configuration
class StreamsConfiguration(@Value("\${spring.application.name}") private val applicationName: String,
                           @Value("\${kafka.boostrap-servers}") private val kafkaBootstrapServers: String) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun startStreams(kafkaStreams: KafkaStreams) {
        kafkaStreams.start()
    }

    @PreDestroy
    fun stopStreams(kafkaStreams: KafkaStreams, inventoryProducer: Producer<String, BiciMadStation>) {
        logger.info("*********** Closing streams ***********")
        inventoryProducer.close()
        kafkaStreams.close()
    }

    @Bean
    fun topology(): Topology {
        val stationSerde: Serde<BiciMadStation> = Serdes.serdeFrom(JsonPojoSerializer<BiciMadStation>(), JsonPojoDeserializer(BiciMadStation::class.java))
        val builder = StreamsBuilder()


        builder.table(STATIONS_TOPIC, Consumed.with(Serdes.Integer(), stationSerde),
                Materialized.`as`<Int, BiciMadStation, KeyValueStore<Bytes, ByteArray>>(STATIONS_STORE)
                        .withKeySerde(Serdes.Integer())
                        .withValueSerde(stationSerde))
/*
        builder.stream(STATIONS_TOPIC, Consumed.with(Serdes.Integer(), stationSerde))
                .selectKey { _, value -> value.name }
                .groupByKey().reduce({ agg, newValue -> newValue },
                        Materialized.`as`<String, BiciMadStation, KeyValueStore<Bytes, ByteArray>>(STATIONS_BY_NAME_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(stationSerde))
*/
        return builder.build()
    }

    @Bean
    fun kafkaStreams(): KafkaStreams {
        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = applicationName
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
        return KafkaStreams(topology(), props)
    }

    @Bean
    fun stationProducer(): Producer<Int, BiciMadStation> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
        props[ProducerConfig.CLIENT_ID_CONFIG] = "InventoryService"
        return KafkaProducer(props, IntegerSerializer(), JsonPojoSerializer<BiciMadStation>())
    }

}