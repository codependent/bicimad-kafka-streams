package com.codependent.bicimad.timer

import com.codependent.bicimad.dto.BiciMadStation
import com.codependent.bicimad.dto.BiciMadStations
import com.codependent.bicimad.streams.STATIONS_TOPIC
import com.codependent.bicimad.webclient.BiciMadWebClient
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import org.mockito.Mockito

import org.mockito.BDDMockito.given
import reactor.core.publisher.Mono

class BiciMadStationInfoTimerTest {
    private val biciMadWebClient = Mockito.mock(BiciMadWebClient::class.java)
    private val stationProducer = Mockito.mock(Producer::class.java) as Producer<Int, BiciMadStation>
    private val biciMadStationInfoTimer = BiciMadStationInfoTimer(biciMadWebClient, stationProducer)

    @Test
    fun scheduleGetStations() {
        val station = BiciMadStation(1, "40.416896", "-3.7024255", "Puerta del Sol A", 1, "1a", "Puerta del Sol nº 1", 1, 0, 24, 16, 5, 1)
        val stations = arrayOf(station)
        val biciMadStations = Mono.just(BiciMadStations(stations))
        given(biciMadWebClient.getStations()).willReturn(biciMadStations)
        biciMadStationInfoTimer.scheduleGetStations()

        Mockito.verify(stationProducer).send(ProducerRecord(STATIONS_TOPIC, station.id, station))
    }
}
