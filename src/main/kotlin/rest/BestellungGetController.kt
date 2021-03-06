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

import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.entity.BestellungId
import com.acme.bestellung.entity.KundeId
import com.acme.bestellung.rest.BestellungGetController.Companion.API_PATH
import com.acme.bestellung.service.BestellungReadService
import com.acme.bestellung.service.FindByIdResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Funktionen der Klasse abgebildet werden
 *
 * @author [J??rgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Einen BestellungController mit einem injizierten [BestellungReadService] erzeugen.
 *
 * @property service Injiziertes Objekt von [BestellungReadService]
 */
@RestController
@RequestMapping(API_PATH)
@Tag(name = "Bestellungen API")
@Suppress("RegExpUnexpectedAnchor")
class BestellungGetController(private val service: BestellungReadService) {
    private val logger = LoggerFactory.getLogger(BestellungGetController::class.java)

    /**
     * Suche anhand der Bestellung-ID
     * @param id ID der zu suchenden Bestellung
     * @param request Das Request-Objekt, um Links f??r HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und der gefundenen Bestellung einschlie??lich Atom-Links,
     *      oder aber Statuscode 204.
     */
    @GetMapping(path = ["/{id:$ID_PATTERN}"], produces = [HAL_JSON_VALUE])
    @Operation(summary = "Suche mit der Bestellung-ID", tags = ["Suchen"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Bestellung gefunden"),
        ApiResponse(responseCode = "304", description = "Bestellung unver??ndert"),
        ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden"),
        ApiResponse(responseCode = "406", description = "Falsche Versionsnummer im ETag"),
    )
    suspend fun findById(
        @PathVariable id: BestellungId,
        @RequestHeader("If-None-Match") version: String?,
        request: ServerHttpRequest,
    ): ResponseEntity<Any> {
        logger.debug("findById: id={}", id)
        logger.trace("findById: version={}", version)

        return when (val result = service.findById(id)) {
            is FindByIdResult.Success -> handleFound(result.bestellung, version, request)
            is FindByIdResult.NotFound -> notFound().build()
        }
    }

    private fun handleFound(
        bestellung: Bestellung,
        version: String?,
        request: ServerHttpRequest,
    ): ResponseEntity<Any> {
        logger.trace("handleFound: {}", bestellung)

        if (version != null && version.first() != '"' && version.last() != '"') {
            return status(NOT_ACCEPTABLE).body(GenericBody.Text("Ungueltige Versionsnummer $version"))
        }
        val currentVersion = "\"${bestellung.version}\""
        if (version == currentVersion) {
            return status(NOT_MODIFIED).build()
        }

        val bestellungModel = BestellungModel(bestellung)
        addLinks(bestellungModel, bestellung.id, request)

        return ok().eTag("\"$currentVersion\"").body(bestellungModel)
    }

    private fun addLinks(model: BestellungModel, id: BestellungId?, request: ServerHttpRequest) = with(model) {
        val baseUri = getBaseUri(request.headers, request.uri, id)
        logger.trace("addHateoas: baseUri={}", baseUri)
        val idUri = "$baseUri/$id"
        val selfLink = Link.of(idUri)
        val listLink = Link.of(baseUri, LinkRelation.of("list"))
        val addLink = Link.of(baseUri, LinkRelation.of("add"))
        add(selfLink, listLink, addLink)
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter. Es wird eine Liste zur??ckgeliefert, damit auch der
     * Statuscode 204 m??glich ist.
     * @param queryParams Query-Parameter als Map.
     * @param request Das Request-Objekt, um Links f??r HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und einer Liste mit den gefundenen Bestellungen einschlie??lich
     *      Atom-Links, oder aber Statuscode 204.
     */
    @GetMapping(produces = [HAL_JSON_VALUE])
    @Operation(summary = "Suche mit Suchkriterien", tags = ["Suchen"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "CollectionModel mid den Bestellungen"),
        ApiResponse(responseCode = "404", description = "Keine Bestellungen gefunden"),
    )
    @Suppress("ReturnCount", "LongMethod")
    suspend fun find(
        @RequestParam queryParams: Map<String, String>,
        request: ServerHttpRequest,
    ): ResponseEntity<CollectionModel<BestellungModel>> {
        logger.debug("find: queryParams={}", queryParams)
        if (queryParams.size > 1) {
            return notFound().build()
        }

        val bestellungen = if (queryParams.isEmpty()) {
            service.findAll()
        } else {
            val kundeIdStr = queryParams["kundeId"] ?: return notFound().build()
            val kundeId = KundeId.fromString(kundeIdStr)
            service.findByKundeId(kundeId)
        }

        val baseUri = getBaseUri(request.headers, request.uri)

        val modelList = bestellungen
            ?.map { bestellung ->
                logger.trace("find: bestellung={}", bestellung)
                val model = BestellungModel(bestellung)
                val selfLink = Link.of("$baseUri/${bestellung.id}")
                model.add(selfLink)
            }

        logger.trace("find: modelList={}", modelList)

        if (modelList == null) {
            return notFound().build()
        }

        return ok(CollectionModel.of(modelList))
    }

    /**
     * Konstante f??r die REST-Schnittstelle
     */
    companion object {
        /**
         * Basis-Pfad der REST-Schnittstelle.
         * const: "compile time constant"
         */
        const val API_PATH = "/api"

        private const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster bzw. regul??rer Ausdruck f??r eine UUID.
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"
    }
}
