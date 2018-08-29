package com.codependent.bicimad.service

import com.codependent.bicimad.dto.BiciMadStation
import com.codependent.bicimad.dto.BiciMadStationStats
import com.codependent.bicimad.serdes.JsonPojoDeserializer
import com.codependent.bicimad.serdes.JsonPojoSerializer
import com.codependent.bicimad.streams.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TopologyUnitTest {

    private val config = Properties()
    private val streamsConfiguration = StreamsConfiguration("test", "dummy:1234")
    private val recordFactory: ConsumerRecordFactory<Int, BiciMadStation>
    private lateinit var testDriver: TopologyTestDriver
    private lateinit var stationsStore: KeyValueStore<Int, BiciMadStation>
    private lateinit var stationsByNameStore: KeyValueStore<String, BiciMadStation>

    init {
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
        config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        recordFactory = ConsumerRecordFactory<Int, BiciMadStation>(STATIONS_TOPIC, IntegerSerializer(), JsonPojoSerializer<BiciMadStation>())

    }

    @BeforeEach
    fun initializeTestDriver() {
        testDriver = TopologyTestDriver(streamsConfiguration.topology(), config)
        stationsStore = testDriver.getKeyValueStore(STATIONS_STORE)
        stationsByNameStore = testDriver.getKeyValueStore(STATIONS_BY_NAME_STORE)
    }

    @AfterEach
    fun tearDown() {
        testDriver.close()
    }

    @Test
    fun testShouldAddStationsToStore() {
        val station = BiciMadStation(1, "40.416896", "-3.7024255", "Puerta del Sol A", 1, "1a", "Puerta del Sol nº 1", 1, 0, 24, 16, 5, 1)
        testDriver.pipeInput(recordFactory.create(STATIONS_TOPIC, 1, station))
        assertEquals(station, stationsStore.get(1))
        assertEquals(station, stationsByNameStore.get("Puerta del Sol A"))
    }

    @Test
    fun testShouldAlertLowCapacityStation() {
        val station = BiciMadStation(1, "40.416896", "-3.7024255", "Puerta del Sol A", 1, "1a", "Puerta del Sol nº 1", 1, 0, 24, 1, 23, 1)
        testDriver.pipeInput(recordFactory.create(STATIONS_TOPIC, 1, station))
        assertEquals(station, stationsStore.get(1))
        assertEquals(station, stationsByNameStore.get("Puerta del Sol A"))

        val station2 = BiciMadStation(2, "40.416896", "-4.7024255", "Puerta del Sol B", 1, "1a", "Puerta del Sol nº 20", 1, 0, 24, 22, 2, 1)
        testDriver.pipeInput(recordFactory.create(STATIONS_TOPIC, 1, station2))

        val output: ProducerRecord<Int, BiciMadStationStats> = testDriver.readOutput(STATIONS_LOW_CAPACITY_TOPIC, IntegerDeserializer(), JsonPojoDeserializer(BiciMadStationStats::class.java))
        assertEquals(station.id, output.value().id)

        val output2: ProducerRecord<Int, BiciMadStationStats> = testDriver.readOutput(STATIONS_LOW_CAPACITY_TOPIC, IntegerDeserializer(), JsonPojoDeserializer(BiciMadStationStats::class.java))
        assertNull(output2.value())
    }
}