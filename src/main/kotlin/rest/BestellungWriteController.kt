/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.bestellung.rest

import am.ik.yavi.core.ConstraintViolation
import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.rest.BestellungGetController.Companion.API_PATH
import com.acme.bestellung.service.BestellungReadService
import com.acme.bestellung.service.BestellungWriteService
import com.acme.bestellung.service.CreateResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.unprocessableEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Funktionen der Klasse abgebildet werden
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Einen BestellungController mit einem injizierten [BestellungReadService] erzeugen.
 *
 * @property service Injiziertes Objekt von [BestellungReadService]
 */
@RestController
@RequestMapping(API_PATH)
@Tag(name = "Bestellungen API")
@Suppress("RegExpUnexpectedAnchor")
class BestellungWriteController(private val service: BestellungWriteService) {
    private val logger = LoggerFactory.getLogger(BestellungWriteController::class.java)

    /**
     * Einen neuen Bestellung-Datensatz anlegen.
     * @param bestellungDTO Das Bestellungsobjekt aus dem eingegangenen Request-Body.
     * @param request Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 400 falls Constraints verletzt
     *      sind oder der JSON-Datensatz syntaktisch nicht korrekt ist.
     */
    @Suppress("RegExpUnexpectedAnchor", "RegExpUnexpectedAnchor")
    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    @Operation(summary = "Eine neue Bestellung anlegen")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Bestellung neu angelegt"),
        ApiResponse(responseCode = "400", description = "Ungültige Werte vorhanden"),
    )
    suspend fun create(
        @RequestBody bestellungDTO: BestellungDTO,
        request: ServerHttpRequest,
    ): ResponseEntity<GenericBody> {
        logger.debug("create(): {}", bestellungDTO)

        return when (val result = service.create(bestellungDTO.toBestellung())) {
            is CreateResult.Success -> handleCreated(result.bestellung, request)
            is CreateResult.ConstraintViolations -> handleConstraintViolations(result.violations)
        }
    }

    private fun handleCreated(bestellung: Bestellung, request: ServerHttpRequest): ResponseEntity<GenericBody> {
        logger.debug("handleCreated: {}", bestellung)
        val baseUri = getBaseUri(request.headers, request.uri)
        val location = URI("$baseUri/${bestellung.id}")
        logger.debug("handleCreated: {}", location)
        return created(location).build()
    }

    private fun handleConstraintViolations(violations: Collection<ConstraintViolation>): ResponseEntity<GenericBody> {
        if (violations.isEmpty()) {
            return unprocessableEntity().build()
        }

        val bestellungViolations = violations.associate { violation ->
            violation.messageKey() to violation.message()
        }
        logger.debug("handleConstraintViolations(): {}", bestellungViolations)

        return unprocessableEntity().body(GenericBody.Values(bestellungViolations))
    }

    /**
     * Konstante für die REST-Schnittstelle
     */
    companion object {
        /**
         * Basis-Pfad der REST-Schnittstelle.
         * const: "compile time constant"
         */
        const val API_PATH = "/api"

        private const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster bzw. regulärer Ausdruck für eine UUID.
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"
    }
}
