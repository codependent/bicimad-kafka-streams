package com.codependent.bicimad.serdes

import com.codependent.bicimad.dto.BiciMadStation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonPojoDeserializerTest {
    @Test
    fun deserialize() {
        val station = BiciMadStation(1, "40.416896", "-3.7024255", "Puerta del Sol A", 1, "1a", "Puerta del Sol nº 1", 1, 0, 24, 16, 5, 1)
        val jsonPojoDeserializer = JsonPojoDeserializer(BiciMadStation::class.java)
        val biciMadStation = jsonPojoDeserializer.deserialize("", "{\"id\":1,\"latitude\":\"40.416896\",\"longitude\":\"-3.7024255\",\"name\":\"Puerta del Sol A\",\"light\":1,\"number\":\"1a\",\"address\":\"Puerta del Sol nº 1\",\"activate\":1,\"no_available\":0,\"total_bases\":24,\"dock_bikes\":16,\"free_bases\":5,\"reservations_count\":1}".toByteArray())

        Assertions.assertEquals(station, biciMadStation)
    }
}