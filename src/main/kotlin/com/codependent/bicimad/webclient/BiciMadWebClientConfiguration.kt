package com.codependent.bicimad.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BiciMadWebClientConfiguration(@Value("\${bicimad.url}") private val bicimadUrl: String,
                                    @Value("\${bicimad.code}") private val bicimadCode: String,
                                    @Value("\${bicimad.passkey}") private val bicimadPasskey: String,
                                    private val objectMapper: ObjectMapper) {
    @Bean
    fun biciMadWebClient(): BiciMadWebClient {
        return BiciMadWebClient(bicimadUrl, bicimadCode, bicimadPasskey, objectMapper)
    }

}