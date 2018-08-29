package com.codependent.bicimad.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BiciMadStation(val id: Int, val latitude: String, val longitude: String, val name: String, val light: Int,
                          val number: String, val address: String, val activate: Int, @JsonProperty("no_available") val noAvailable: Int,
                          @JsonProperty("total_bases") val totalBases: Int, @JsonProperty("dock_bikes") val dockBikes: Int,
                          @JsonProperty("free_bases") val freeBases: Int, @JsonProperty("reservations_count") val reservationsCount: Int)