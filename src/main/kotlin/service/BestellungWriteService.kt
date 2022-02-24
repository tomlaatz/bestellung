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
class BestellungWriteService(
    @Lazy private val validator: BestellungValidator,
    private val factory: SessionFactory,
) {
    private val logger = LoggerFactory.getLogger(BestellungWriteService::class.java)

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
        logger.trace("create: Keine \"Constraint Violations\"")

        withTimeout(timeoutLong) {
            factory.withTransaction { session, _ ->
                session.persist(bestellung)
            }.awaitSuspending()
        }

        //val neueBestellung = mongo.insert<Bestellung>().oneAndAwait(bestellung)
        return CreateResult.Success(bestellung)
    }

    private companion object {
        const val timeoutLong = 2000L
    }
}
