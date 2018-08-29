package com.codependent.bicimad.streams

import com.codependent.bicimad.dto.BiciMadStation
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class StreamsLifecycleManager(private val kafkaStreams: KafkaStreams, private val stationProducer: KafkaProducer<Int, BiciMadStation>) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun startStreams() {
        kafkaStreams.start()
    }

    @PreDestroy
    fun stopStreams() {
        logger.info("*********** Closing streams ***********")
        stationProducer.close()
        kafkaStreams.close()
    }

}