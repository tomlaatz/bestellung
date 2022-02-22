/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.bestellung.service

import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.entity.BestellungId
import com.acme.bestellung.entity.KundeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations
import org.springframework.data.mongodb.core.awaitOneOrNull
import org.springframework.data.mongodb.core.flow
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.oneAndAwait
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 * Anwendungslogik für Bestellungen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class BestellungService(
    private val validator: BestellungValidator,
    private val mongo: ReactiveFluentMongoOperations,
    // siehe org.springframework.web.reactive.function.client.DefaultWebClientBuilder
    // siehe org.springframework.web.reactive.function.client.DefaultWebClient
    @Lazy private val kundeClient: KundeClient,
) {
    private val logger = LoggerFactory.getLogger(BestellungService::class.java)

    /**
     * Alle Bestellungen ermitteln.
     * @return Alle Bestellungen.
     */
    suspend fun findAll(): Flow<Bestellung> = mongo.query<Bestellung>()
        .flow()
        .onEach { bestellung ->
            logger.debug("findAll: {}", bestellung)
            val (nachname) = findKundeById(bestellung.kundeId)
            bestellung.kundeNachname = nachname
        }

    /**
     * Eine Bestellung anhand der ID suchen.
     * @param id Die Id der gesuchten Bestellung.
     * @return Die gefundene Bestellung oder null.
     */
    suspend fun findById(id: BestellungId): FindByIdResult {
        logger.debug("findById: id={}", id)
        val bestellung = mongo.query<Bestellung>()
            .matching(Bestellung::id isEqualTo id)
            .awaitOneOrNull()
        logger.debug("findById: {}", bestellung)
        if (bestellung == null) {
            return FindByIdResult.NotFound
        }

        // Destructuring
        val (nachname) = findKundeById(bestellung.kundeId)
        return FindByIdResult.Success(bestellung.apply { kundeNachname = nachname })
    }

    /**
     * Bestellungen zur Kunde-ID suchen.
     * @param kundeId Die Id des gegebenen Kunden.
     * @return Die gefundenen Bestellungen oder ein leeres Flux-Objekt.
     */
    suspend fun findByKundeId(kundeId: KundeId): Flow<Bestellung> {
        val (nachname) = findKundeById(kundeId)

        return mongo.query<Bestellung>()
            .matching(Bestellung::kundeId isEqualTo kundeId)
            .flow()
            .onEach { bestellung ->
                logger.debug("findByKundeId: {}", bestellung)
                bestellung.kundeNachname = nachname
                logger.debug("findByKundeId: kundeNachname={}", bestellung.kundeNachname)
            }
    }

    private suspend fun findKundeById(kundeId: KundeId): Kunde {
        logger.debug("findKundeById: kundeId={}", kundeId)
        return when (val result = kundeClient.findById(kundeId)) {
            is FindKundeResult.Found -> {
                logger.debug("findKundeById: kunde={}", result.kunde)
                result.kunde
            }
            is FindKundeResult.NotFound -> {
                logger.debug("findKundeById: {}", result.exception.javaClass.name)
                if (result.exception is WebClientResponseException.NotFound) {
                    Kunde()
                } else {
                    // Unauthorized (401), Forbidden (403), ...
                    Kunde(nachname = "Exception", email = "exception@acme.com")
                }
            }
        }
    }

    /**
     * Eine neue Bestellung anlegen.
     * @param bestellung Das Objekt der neu anzulegenden Bestellung.
     * @return Die neu angelegte Bestellung mit generierter ID.
     */
    suspend fun create(bestellung: Bestellung): CreateResult {
        logger.debug("create: {}", bestellung)
        val violations = validator.validate(bestellung)
        if (violations.isNotEmpty()) {
            return CreateResult.ConstraintViolations(violations)
        }

        val neueBestellung = mongo.insert<Bestellung>().oneAndAwait(bestellung)
        return CreateResult.Success(neueBestellung)
    }
}
