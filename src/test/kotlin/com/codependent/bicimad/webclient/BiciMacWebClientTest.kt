package com.codependent.bicimad.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

@Disabled
class BiciMacWebClientTest {

    private val biciMadWebClient = BiciMadWebClient(
            "https://rbdata.emtmadrid.es:8443/BiciMad",
            "",
            "",
            ObjectMapper().registerModule(KotlinModule()))

    @Test
    fun testShouldGetStations() {
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