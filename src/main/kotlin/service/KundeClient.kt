/*
 * Copyright (C) 2021 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("HttpUrlsUsage")

package com.acme.bestellung.service

import com.acme.bestellung.entity.KundeId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.awaitBody

/**
 * Anwendungslogik für Bestellungen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class KundeClient(
    // siehe org.springframework.web.reactive.function.client.DefaultWebClientBuilder
    // siehe org.springframework.web.reactive.function.client.DefaultWebClient
    @Lazy private val clientBuilder: WebClient.Builder,
) {
    // org.springframework.web.reactive.function.client.DefaultWebClient
    private val client = clientBuilder
        .baseUrl(baseUrl)
        .filter(basicAuthentication(username, password))
        .build()

    private val logger = LoggerFactory.getLogger(KundeClient::class.java)

    /**
     * Kunde anhand der Kunde-ID suchen.
     * @param kundeId Die Id des gesuchten Kunden.
     * @return Der gefundene Kunde oder null.
     */
    suspend fun findById(kundeId: KundeId): FindKundeResult {
        logger.debug("findById: kundeId={}, baseUrl={}", kundeId, baseUrl)

        val kunde = try {
            client
                .get()
                .uri("/api/$kundeId")
                .retrieve()
                .awaitBody<Kunde>()
        } catch (e: WebClientException) {
            // WebClientRequestException oder WebClientResponseException (z.B. NotFound, ServiceUnavailable)
            logger.error("findById: {}", e.javaClass.name)
            return FindKundeResult.NotFound(e)
        }

        logger.debug("findById: {}", kunde)
        return FindKundeResult.Found(kunde)
    }

    private companion object {
        // https://github.com/istio/istio/blob/master/samples/bookinfo/src/reviews/reviews-application/src/main/java/application/rest/LibertyRestEndpoint.java#L43
        val kundeHost = System.getenv("KUNDE_SERVICE_HOST") ?: "localhost"
        val kundePort = System.getenv("KUNDE_SERVICE_PORT") ?: "8080"
        val baseUrl = "https://$kundeHost:$kundePort"

        const val username = "admin"
        const val password = "p"
    }
}

/**
 * Resultat-Typ für [KundeClient.findById]
 */
sealed interface FindKundeResult {
    /**
     * Resultat-Typ, wenn ein Kunde gefunden wurde.
     * @property kunde Der gefundene Kunde
     */
    data class Found(val kunde: Kunde) : FindKundeResult

    /**
     * Resultat-Typ, wenn bei der Suche nach einem Kunden ein Fehler eingetreten ist.
     * @property exception Die Exception vom Typ WebClientResponseException, z.B. von der abgeleiteten Klasse
     *  WebClientResponseException.NotFound
     */
    data class NotFound(val exception: WebClientException) : FindKundeResult
}
