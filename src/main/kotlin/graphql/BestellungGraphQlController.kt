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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.bestellung.graphql

import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.entity.BestellungId
import com.acme.bestellung.service.BestellungReadService
import com.acme.bestellung.service.FindByIdResult
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

/**
 * Eine _Controller_-Klasse für die GraphQL-Schnittstelle mit Abbildung auf die Typen aus dem GraphQL-Schema.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Einen BestellungGraphQlController mit einem injizierten [BestellungReadService] erzeugen.
 *
 * @property service Injiziertes Objekt von [BestellungReadService]
 */
@Controller
@Suppress("unused")
class BestellungGraphQlController(val service: BestellungReadService) {
    private val logger = LoggerFactory.getLogger(BestellungGraphQlController::class.java)

    /**
     * Suche anhand der Bestellung-ID
     * @param id ID der zu suchenden Bestellung
     * @return Die gefundene Bestellung
     * @throws NotFoundException falls keine Bestellung gefunden wurde
     */
    @QueryMapping("bestellung")
    suspend fun findById(@Argument id: BestellungId): Bestellung {
        logger.debug("findById: id={}", id)

        return when (val result = service.findById(id)) {
            is FindByIdResult.Success -> result.bestellung
            is FindByIdResult.NotFound -> throw NotFoundException()
        }
    }
}
