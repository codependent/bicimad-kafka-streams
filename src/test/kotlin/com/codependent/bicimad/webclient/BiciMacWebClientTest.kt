package com.codependent.bicimad.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class BiciMacWebClientTest {


    private val biciMadWebClient = BiciMadWebClient(
            "https://rbdata.emtmadrid.es:8443/BiciMad",
            "WEB.SERV.joseantonio.inigo@gmail.com",
            "A28B9528-6795-4C9F-993D-2B8AF39C899C",
            ObjectMapper().registerModule(KotlinModule()))

    @Test
    fun shouldGetStations() {
        val latch = CountDownLatch(1)
        val stations = biciMadWebClient.getStations().log()
        stations
                .doOnError {
                    latch.countDown()
                    fail("Error")
                }
                .subscribe {
                    latch.countDown()
                }
        latch.await()
    }

}