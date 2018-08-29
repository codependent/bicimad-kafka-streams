package com.codependent.bicimad.service

import com.codependent.bicimad.dto.BiciMadStation
import com.codependent.bicimad.streams.STATIONS_BY_NAME_STORE
import com.codependent.bicimad.streams.STATIONS_STORE
import com.codependent.bicimad.streams.STATIONS_TOPIC
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StationsService(private val streams: KafkaStreams,
                      private val bicimadStationProducer: Producer<Int, BiciMadStation>) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getStation(id: Int): BiciMadStation? {
        var keyValueStore: ReadOnlyKeyValueStore<Int, BiciMadStation>? = null
        while (keyValueStore == null) {
            try {
                keyValueStore = streams.store(STATIONS_STORE, QueryableStoreTypes.keyValueStore<Int, BiciMadStation>())
            } catch (ex: InvalidStateStoreException) {
                ex.printStackTrace()
                Thread.sleep(500)
            }
        }
        return keyValueStore.get(id)
    }

    fun getStation(name: String): BiciMadStation? {
        var keyValueStore: ReadOnlyKeyValueStore<String, BiciMadStation>? = null
        while (keyValueStore == null) {
            try {
                keyValueStore = streams.store(STATIONS_BY_NAME_STORE, QueryableStoreTypes.keyValueStore<String, BiciMadStation>())
            } catch (ex: InvalidStateStoreException) {
                ex.printStackTrace()
                Thread.sleep(500)
            }
        }
        return keyValueStore.get(name)
    }

    fun addStation(station: BiciMadStation) {
        val record = ProducerRecord<Int, BiciMadStation>(STATIONS_TOPIC, station.id, station)
        val metadata = bicimadStationProducer.send(record).get()
        logger.info("{}", metadata)
        bicimadStationProducer.flush()
    }
/*
    fun deleteProduct(id: String) {
        val record = ProducerRecord<String, BiciMadStation>(STATIONS_TOPIC, id, BiciMadStation(0, "", ProductType.ELECTRONICS, "", -1))
        val metadata = bicimadStationProducer.send(record).get()
        logger.info("{}", metadata)
        bicimadStationProducer.flush()
    }
*/
}