package com.codependent.bicimad.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BiciMadStationStats(val id: Int, val latitude: String, val longitude: String, val name: String, @JsonProperty("dock_bikes") val dockBikes: Int,
                               @JsonProperty("free_bases") val freeBases: Int, val availabilityPercentage: Int)