package com.codependent.bicimad.streams

import com.codependent.bicimad.dto.BiciMadStation
import com.codependent.bicimad.dto.BiciMadStationStats
import com.codependent.bicimad.dto.BikeAvailabilityAggregation
import com.codependent.bicimad.serdes.JsonPojoDeserializer
import com.codependent.bicimad.serdes.JsonPojoSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.Serialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.concurrent.TimeUnit

const val STATIONS_TOPIC = "bicimad-stations"
const val STATIONS_LOW_CAPACITY_TOPIC = "bicimad-low-capacity-stations"
const val STATIONS_TURNOVER_TOPIC = "bicimad-station-turnover"
const val STATIONS_STORE = "bicimad-stations-store"
const val STATIONS_CAPACITY_STORE = "bicimad-stations-capacity-store"
const val STATIONS_BY_NAME_STORE = "bicimad-stations-by-name-store"

@Configuration
class StreamsConfiguration(@Value("\${spring.application.name}") private val applicationName: String,
                           @Value("\${kafka.boostrap-servers}") private val kafkaBootstrapServers: String) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun topology(): Topology {
        val stationSerde: Serde<BiciMadStation> = Serdes.serdeFrom(JsonPojoSerializer<BiciMadStation>(), JsonPojoDeserializer(BiciMadStation::class.java))
        val stationStatsSerde: Serde<BiciMadStationStats> = Serdes.serdeFrom(JsonPojoSerializer<BiciMadStationStats>(), JsonPojoDeserializer(BiciMadStationStats::class.java))
        val bikeAvailabilityAggregation: Serde<BikeAvailabilityAggregation> = Serdes.serdeFrom(JsonPojoSerializer<BikeAvailabilityAggregation>(), JsonPojoDeserializer(BikeAvailabilityAggregation::class.java))

        val builder = StreamsBuilder()

        val kStream = builder.stream(STATIONS_TOPIC, Consumed.with(Serdes.Integer(), stationSerde))

        kStream.groupByKey().reduce({ _, newValue -> newValue },
                Materialized.`as`<Int, BiciMadStation, KeyValueStore<Bytes, ByteArray>>(STATIONS_STORE)
                        .withKeySerde(Serdes.Integer())
                        .withValueSerde(stationSerde))
                .toStream()
                .filter { _, value -> (value.dockBikes * 100.0) / value.totalBases < 10.0 }
                .mapValues { station ->
                    BiciMadStationStats(station.id, station.latitude, station.longitude, station.name, station.dockBikes,
                            station.freeBases, (station.dockBikes * 100.0) / station.totalBases)
                }
                .peek { key, value -> logger.info("Low capacity station: $key - $value") }
                .to(STATIONS_LOW_CAPACITY_TOPIC, Produced.with(Serdes.Integer(), stationStatsSerde))

        kStream.mapValues { station -> station.dockBikes * 100.0 / station.totalBases }
                .groupByKey(Serialized.with(Serdes.Integer(), Serdes.Double()))
                .reduce({ _, newValue -> newValue },
                        Materialized.`as`<Int, Double, KeyValueStore<Bytes, ByteArray>>(STATIONS_CAPACITY_STORE)
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(Serdes.Double()))

        kStream.selectKey { _, value -> value.name }
                .groupByKey(Serialized.with(Serdes.String(), stationSerde))
                .reduce({ _, newValue -> newValue },
                        Materialized.`as`<String, BiciMadStation, KeyValueStore<Bytes, ByteArray>>(STATIONS_BY_NAME_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(stationSerde))


        kStream.groupByKey(Serialized.with(Serdes.Integer(), stationSerde))
                .windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(60)))
                .aggregate({ BikeAvailabilityAggregation(-1, 0, 0) },
                        { _: Int, station: BiciMadStation, aggregate: BikeAvailabilityAggregation ->
                            val availableBikes = station.dockBikes
                            val previouslyAvailableBikes = if (aggregate.previousValue == -1) availableBikes else aggregate.previousValue
                            val difference = Math.abs(availableBikes - previouslyAvailableBikes)
                            BikeAvailabilityAggregation(station.dockBikes, aggregate.netChange + difference, station.totalBases)
                        }, Materialized.with(Serdes.Integer(), bikeAvailabilityAggregation))
                .mapValues { key, agg ->
                    val totalCapacity = agg.totalCapacity
                    val netChange = agg.netChange
                    (netChange / totalCapacity.toDouble()) * 100.0
                }.toStream { key, _ -> key.key() }.to(STATIONS_TURNOVER_TOPIC, Produced.with(Serdes.Integer(), Serdes.Double()))

        val topology = builder.build()
        logger.info("{}", topology.describe())
        return topology
    }

    @Bean
    fun kafkaStreams(topology: Topology): KafkaStreams {
        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = applicationName
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
        return KafkaStreams(topology, props)
    }

    @Bean
    fun stationProducer(): KafkaProducer<Int, BiciMadStation> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
        props[ProducerConfig.CLIENT_ID_CONFIG] = "InventoryService"
        return KafkaProducer(props, IntegerSerializer(), JsonPojoSerializer<BiciMadStation>())
    }

}