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
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.annotation.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import org.hibernate.Hibernate
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClientResponseException
import javax.persistence.NoResultException

/**
 * Anwendungslogik für Bestellungen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class BestellungReadService(
    private val validator: BestellungValidator,
    private val factory: SessionFactory,
    @Lazy private val kundeClient: KundeClient,
) {
    private val logger = LoggerFactory.getLogger(BestellungReadService::class.java)

    /**
     * Alle Bestellungen ermitteln.
     * @return Alle Bestellungen.
     */
    suspend fun findAll(): List<Bestellung> {
        val query = factory.criteriaBuilder.createQuery<Bestellung>()
        query.from(Bestellung::class)

        return withTimeout(timeoutLong) {
            factory.withSession { session ->
                session.createQuery(query).resultList
            }.awaitSuspending()
        }
    }


    /**
     * Eine Bestellung anhand der ID suchen.
     * @param id Die Id der gesuchten Bestellung.
     * @return Die gefundene Bestellung oder null.
     */
    suspend fun findById(id: BestellungId): Bestellung? {
        logger.debug("findById: id={}", id)

        val bestellung = withTimeout(timeoutShort) {
            factory.withSession { session ->
                session.find<Bestellung>(id)
            }.awaitSuspending()
        }
        logger.debug("findById: {}", bestellung)
        return bestellung
    }


    /**
     * Bestellungen zur Kunde-ID suchen.
     * @param kundeId Die Id des gegebenen Kunden.
     * @return Die gefundenen Bestellungen oder ein leeres Flux-Objekt.
     */
    suspend fun findByKundeId(kundeId: KundeId): List<Bestellung>? = try {
        val (nachname) = findKundeById(kundeId)

        factory.withSession { session ->
            session.createNamedQuery<Bestellung>(Bestellung.BY_KUNDEID)
                .setParameter(Bestellung.PARAM_KUNDEID, kundeId.toString())
                .resultList
        }.awaitSuspending().onEach { bestellung ->
            logger.debug("findByKundeId: {}", bestellung)
            bestellung.kundeNachname = nachname
            logger.debug("findByKundeId: kundeNachname={}", bestellung.kundeNachname) }
    } catch (e: NoResultException) {
        logger.debug("Keine Bestellung mit der KundenId '{}'", kundeId)
        null
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

    private companion object {
        const val timeoutShort = 500L
        const val timeoutLong = 2000L
    }
}
