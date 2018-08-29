package com.codependent.bicimad.webclient

import com.codependent.bicimad.dto.BiciMadApiResponse
import com.codependent.bicimad.dto.BiciMadStations
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class BiciMadWebClient(@Value("\${bicimad.url}") private val bicimadUrl: String,
                       @Value("\${bicimad.code}") private val bicimadCode: String,
                       @Value("\${bicimad.passkey}") private val bicimadPasskey: String,
                       private val objectMapper: ObjectMapper) {

    private val webClient: WebClient = WebClient.create(bicimadUrl)

    fun getStations(): Mono<BiciMadStations> {
        return webClient.get().uri { it.path("/get_stations").pathSegment(bicimadCode).pathSegment(bicimadPasskey).build() }
                .retrieve().bodyToMono(BiciMadApiResponse::class.java)
                .map { objectMapper.readValue(it.data, BiciMadStations::class.java) }
    }

}