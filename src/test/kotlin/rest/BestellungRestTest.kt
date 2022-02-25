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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.bestellung.rest

import com.acme.bestellung.config.ProfilesBanner.DEV
import com.acme.bestellung.entity.ArtikelId
import com.acme.bestellung.entity.Bestellposition
import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.entity.KundeId
import com.acme.bestellung.rest.BestellungGetController.Companion.API_PATH
import com.acme.bestellung.rest.BestellungGetController.Companion.ID_PATTERN
import com.jayway.jsonpath.JsonPath
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.beBlank
import io.kotest.matchers.string.shouldMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE.JAVA_17
import org.junit.jupiter.api.condition.JRE.JAVA_18
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange
import java.math.BigDecimal
import java.math.BigDecimal.TEN

@Tag("rest")
@DisplayName("REST-Schnittstelle fuer Bestellungen testen")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_17, max = JAVA_18)
@Suppress("ClassName", "UseDataClass")
@ExperimentalCoroutinesApi
class BestellungRestTest(@LocalServerPort private val port: Int, ctx: ApplicationContext) {
    private val baseUrl = "$SCHEMA://$HOST:$port$API_PATH"
    private val client = WebClient.builder()
        .filter(basicAuthentication(USER_ADMIN, PASSWORD))
        .baseUrl(baseUrl)
        .build()

    init {
        ctx.getBean<BestellungGetController>() shouldNotBe null
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @ExperimentalCoroutinesApi
    @Nested
    inner class Lesen {
        @ParameterizedTest
        @ValueSource(strings = [ID_VORHANDEN])
        fun `Suche mit vorhandener ID`(id: String) = runTest {
            // when
            val response = client.get()
                .uri(ID_PATH, id)
                .accept(HAL_JSON)
                .awaitExchange { response -> response.awaitEntity<String>() }

            // then
            assertSoftly(response) {
                statusCode shouldBe OK

                body shouldNotBe null
                val kundeNachname: String = JsonPath.read(body, "$.kundeNachname")
                kundeNachname shouldNot beBlank()
                kundeNachname shouldNotBe "Fallback"
                val linkDiscoverer = HalLinkDiscoverer()
                val selfLink = linkDiscoverer.findLinkWithRel("self", body ?: "").get().href
                selfLink shouldBe "$baseUrl/$id"
            }
        }

        @ParameterizedTest
        @ValueSource(strings = [ID_INVALID, ID_NICHT_VORHANDEN])
        fun `Suche mit syntaktisch ungueltiger oder nicht-vorhandener ID`() = runTest {
            // given
            val id = ID_INVALID

            // when
            val statusCode = client.get()
                .uri(ID_PATH, id)
                .accept(HAL_JSON)
                .awaitExchange { response -> response.statusCode() }

            // then
            statusCode shouldBe NOT_FOUND
        }

        @Test
        fun `Suche nach allen Bestellungen`() = runTest {
            // when
            val bestellungenModel = client.get()
                .accept(HAL_JSON)
                .retrieve()
                .awaitBody<BestellungenModel>()

            bestellungenModel._embedded.bestellungen shouldNot beEmpty()
        }

        @ParameterizedTest
        @ValueSource(strings = [KUNDE_ID])
        fun `Suche mit vorhandener Kunde-ID`(kundeId: String) = runTest {
            // when
            val bestellungenModel = client.get()
                .uri { builder ->
                    builder
                        .path(BESTELLUNG_PATH)
                        .queryParam(KUNDE_ID_PARAM, kundeId)
                        .build()
                }
                .accept(HAL_JSON)
                .retrieve()
                .awaitBody<BestellungenModel>()

            // then
            assertSoftly {
                val bestellungen = bestellungenModel._embedded.bestellungen
                bestellungen shouldNot beEmpty()
                bestellungen.onEach { bestellung ->
                    bestellung.content?.kundeId.toString().lowercase() shouldBe kundeId.lowercase()
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Schreiben {
        @ParameterizedTest
        @CsvSource("$KUNDE_ID, $ARTIKEL_ID")
        fun `Abspeichern einer neuen Bestellung`(kundeId: String, artikelId: String) = runTest {
            // given
            val bestellposition = Bestellposition(
                artikelId = ArtikelId.fromString(artikelId),
                anzahl = 1,
                einzelpreis = TEN,
            )
            val bestellpositionen = listOfNotNull(bestellposition)
            val neueBestellung = BestellungDTO(
                kundeId = KundeId.fromString(kundeId),
                bestellpositionen = bestellpositionen,
            )

            // when
            val response = client.post()
                .bodyValue(neueBestellung)
                .awaitExchange { response -> response.awaitBodilessEntity() }

            // then
            assertSoftly(response) {
                statusCode shouldBe CREATED
                val location = headers.location
                val id = location.toString().substringAfterLast('/')
                id shouldMatch ID_PATTERN
            }
        }

        @ParameterizedTest
        @ValueSource(strings = [KUNDE_ID])
        fun `Abspeichern einer leeren Bestellung`(kundeId: String) = runTest {
            // given
            val neueBestellung = Bestellung(
                kundeId = KundeId.fromString(kundeId),
                bestellpositionen = emptyList(),
            )
            val violationKeys = listOf("bestellung.bestellpositionen.notEmpty")

            // when
            val response = client.post()
                .bodyValue(neueBestellung)
                .awaitExchange { response -> response.awaitEntity<GenericBody.Values>() }

            // then
            assertSoftly(response) {
                statusCode shouldBe UNPROCESSABLE_ENTITY
                body shouldNotBe null
                checkNotNull(body) { "Der Rumpf darf nicht leer sein" }
                val violations = body!!.values
                val keys = violations.keys
                keys shouldNot beEmpty()
                keys shouldHaveSize 1
                keys shouldContainExactly violationKeys
            }
        }

        @ParameterizedTest
        @CsvSource("$KUNDE_ID, $ARTIKEL_ID")
        fun `Abspeichern einer ungueltigen Bestellung`(kundeId: String, artikelId: String) = runTest {
            // given
            val bestellposition = Bestellposition(
                artikelId = ArtikelId.fromString(artikelId),
                anzahl = 0,
                einzelpreis = BigDecimal("-1"),
            )
            val neueBestellung = Bestellung(
                kundeId = KundeId.fromString(kundeId),
                bestellpositionen = listOfNotNull(bestellposition),
            )
            val violationKeys = listOf("bestellposition.anzahl.min", "bestellposition.einzelpreis.min")

            // when
            val response = client.post()
                .bodyValue(neueBestellung)
                .awaitExchange { response -> response.awaitEntity<GenericBody.Values>() }

            // then
            assertSoftly(response) {
                statusCode shouldBe UNPROCESSABLE_ENTITY

                body shouldNotBe null
                checkNotNull(body) { "Der Rumpf darf nicht leer sein" }
                val violations = body!!.values
                val keys = violations.keys
                keys shouldNot beEmpty()
                keys shouldHaveSize 2
                keys shouldContainExactlyInAnyOrder violationKeys
            }
        }
    }

    private companion object {
        const val SCHEMA = "http"
        const val HOST: String = "localhost"
        const val BESTELLUNG_PATH = "/"
        const val ID_PATH = "/{id}"
        const val KUNDE_ID_PARAM = "kundeId"

        const val USER_ADMIN = "admin"
        const val PASSWORD = "p"

        const val ID_VORHANDEN = "10000000-0000-0000-0000-000000000001"
        const val ID_INVALID = "YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
        const val KUNDE_ID = "00000000-0000-0000-0000-000000000001"
        const val ARTIKEL_ID = "20000000-0000-0000-0000-000000000001"
    }
}
