package com.codependent.bicimad.timer

import com.codependent.bicimad.dto.BiciMadStation
import com.codependent.bicimad.streams.STATIONS_TOPIC
import com.codependent.bicimad.webclient.BiciMadWebClient
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BiciMadStationInfoTimer(private val biciMadWebClient: BiciMadWebClient, private val stationProducer: Producer<Int, BiciMadStation>) {

    @Scheduled(cron = "0 * * * * *")
    fun scheduleGetStations() {
        biciMadWebClient.getStations()
                .subscribe {
                    it.stations.forEach { station ->
                        stationProducer.send(ProducerRecord(STATIONS_TOPIC, station.id, station))
                    }
                }
    }

}
